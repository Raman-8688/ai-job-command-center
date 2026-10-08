package com.jobcommandcenter.ai.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.application.JobMatchingService;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobMatchResult;
import com.jobcommandcenter.job.domain.JobRepository;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillRepository;
import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service combining deterministic match scoring with AI qualitative evaluation.
 * Preserves deterministic scoring and verified user skills as immutable source truth.
 */
@Service
@Transactional(readOnly = true)
public class JobAiFitService {

    private final JobRepository jobRepository;
    private final JobMatchingService jobMatchingService;
    private final JobAiAnalysisService jobAiAnalysisService;
    private final JobAiAnalysisRepository analysisRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;

    public JobAiFitService(JobRepository jobRepository,
                           JobMatchingService jobMatchingService,
                           JobAiAnalysisService jobAiAnalysisService,
                           JobAiAnalysisRepository analysisRepository,
                           UserSkillRepository userSkillRepository,
                           SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.jobMatchingService = jobMatchingService;
        this.jobAiAnalysisService = jobAiAnalysisService;
        this.analysisRepository = analysisRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public JobAiFitEvaluation evaluateFit(UUID userId, UUID jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // 1. Deterministic match result (Phase 3 truth)
        JobMatchResult matchResult = jobMatchingService.calculateMatch(userId, jobId);

        // 2. Fetch or trigger AI analysis
        JobAiAnalysis analysis = analysisRepository.findLatestByJobId(jobId)
            .filter(a -> a.getStatus() == AIAnalysisStatus.COMPLETED)
            .orElseGet(() -> jobAiAnalysisService.analyzeJob(jobId, null));

        // 3. Candidate verified skills (Grounding: only isVerified() == true)
        List<UserSkill> verifiedUserSkills = userSkillRepository.findByUserId(userId).stream()
            .filter(UserSkill::isVerified)
            .collect(Collectors.toList());

        Set<UUID> verifiedSkillIds = verifiedUserSkills.stream()
            .map(UserSkill::getSkillId)
            .collect(Collectors.toSet());

        Map<UUID, String> skillCatalog = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        Set<String> verifiedSkillNames = verifiedSkillIds.stream()
            .map(skillCatalog::get)
            .filter(Objects::nonNull)
            .map(s -> s.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        // 4. Map analyzed technologies against verified candidate skills
        List<String> matchedTech = new ArrayList<>();
        List<String> missingReqTech = new ArrayList<>();
        List<String> missingPrefTech = new ArrayList<>();

        for (AnalyzedTechnology tech : analysis.getTechnologies()) {
            String techName = tech.technology().trim();
            String techLower = techName.toLowerCase(Locale.ROOT);

            boolean isMatched = verifiedSkillNames.contains(techLower) ||
                verifiedSkillNames.stream().anyMatch(s -> s.contains(techLower) || techLower.contains(s));

            if (isMatched) {
                matchedTech.add(techName);
            } else if (tech.required()) {
                missingReqTech.add(techName);
            } else {
                missingPrefTech.add(techName);
            }
        }

        // 5. Qualitative fit tier
        String fitTier;
        if (matchResult.overallScore() >= 80 && missingReqTech.isEmpty()) {
            fitTier = "STRONG_MATCH";
        } else if (matchResult.overallScore() >= 50 && missingReqTech.size() <= 2) {
            fitTier = "MODERATE_MATCH";
        } else {
            fitTier = "WEAK_MATCH";
        }

        // 6. Qualitative summary
        String qualitativeSummary = buildQualitativeSummary(job, analysis, matchResult.overallScore(), matchedTech, missingReqTech);

        // 7. Interview preparation notes
        List<String> prepNotes = buildInterviewPrepNotes(job, analysis, matchedTech, missingReqTech);

        // 8. Score breakdown
        Map<String, Integer> scoreBreakdown = Map.of(
            "overall", matchResult.overallScore(),
            "requiredSkills", matchResult.requiredSkillScore(),
            "preferredSkills", matchResult.preferredSkillScore(),
            "title", matchResult.titleScore(),
            "experience", matchResult.experienceScore(),
            "location", matchResult.locationScore(),
            "workMode", matchResult.workModeScore()
        );

        return new JobAiFitEvaluation(
            job.getId(),
            job.getTitle(),
            job.getCompanyName(),
            fitTier,
            matchResult.overallScore(),
            scoreBreakdown,
            matchedTech,
            missingReqTech,
            missingPrefTech,
            qualitativeSummary,
            prepNotes,
            analysis.getPotentialRedFlags(),
            analysis.getConfidence(),
            analysis.getVersion()
        );
    }

    private String buildQualitativeSummary(Job job,
                                           JobAiAnalysis analysis,
                                           int score,
                                           List<String> matchedTech,
                                           List<String> missingReqTech) {
        StringBuilder sb = new StringBuilder();
        sb.append("Deterministic fit score is ").append(score).append("/100. ");
        sb.append("Analysis normalized role to '").append(analysis.getNormalizedTitle())
          .append("' (seniority: ").append(analysis.getSeniorityLevel()).append("). ");

        if (!matchedTech.isEmpty()) {
            sb.append("Candidate demonstrates verified mastery in ").append(matchedTech.size())
              .append(" key technologies (").append(String.join(", ", matchedTech)).append("). ");
        }

        if (!missingReqTech.isEmpty()) {
            sb.append("Gaps identified in required stack: ").append(String.join(", ", missingReqTech)).append(". ");
        } else {
            sb.append("No critical required skill gaps detected against verified profile. ");
        }

        return sb.toString().trim();
    }

    private List<String> buildInterviewPrepNotes(Job job,
                                                JobAiAnalysis analysis,
                                                List<String> matchedTech,
                                                List<String> missingReqTech) {
        List<String> notes = new ArrayList<>();

        if (!matchedTech.isEmpty()) {
            notes.add("Highlight production design patterns and deep operational experience with: " +
                String.join(", ", matchedTech));
        }

        if (!analysis.getCoreResponsibilities().isEmpty()) {
            notes.add("Prepare STAR stories demonstrating: " + analysis.getCoreResponsibilities().get(0));
        }

        if (!missingReqTech.isEmpty()) {
            notes.add("Formulate transferable skill bridging strategies for: " +
                String.join(", ", missingReqTech));
        } else {
            notes.add("Emphasize software quality, automated testing, and CI/CD delivery practices.");
        }

        return notes;
    }
}
