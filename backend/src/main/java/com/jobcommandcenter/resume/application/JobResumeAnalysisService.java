package com.jobcommandcenter.resume.application;

import com.jobcommandcenter.ai.application.JobAiAnalysisService;
import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.AIProviderFactory;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.resume.domain.*;
import com.jobcommandcenter.skill.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core analysis engine connecting Resume, Job, Verified Skills, and AI Provider.
 * Performs deterministic matching layered with qualitative AI assessment.
 */
@Service
@Transactional(readOnly = true)
public class JobResumeAnalysisService {

    private final ResumeService resumeService;
    private final JobRepository jobRepository;
    private final JobAiAnalysisRepository jobAiAnalysisRepository;
    private final JobAiAnalysisService jobAiAnalysisService;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final AIProviderFactory aiProviderFactory;

    public JobResumeAnalysisService(ResumeService resumeService,
                                    JobRepository jobRepository,
                                    JobAiAnalysisRepository jobAiAnalysisRepository,
                                    JobAiAnalysisService jobAiAnalysisService,
                                    UserSkillRepository userSkillRepository,
                                    SkillRepository skillRepository,
                                    AIProviderFactory aiProviderFactory) {
        this.resumeService = resumeService;
        this.jobRepository = jobRepository;
        this.jobAiAnalysisRepository = jobAiAnalysisRepository;
        this.jobAiAnalysisService = jobAiAnalysisService;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
        this.aiProviderFactory = aiProviderFactory;
    }

    @Transactional
    public JobResumeAnalysisResult analyzeResumeForJob(UUID userId, UUID resumeId, UUID jobId) {
        // 1. Load and verify ownership of Resume
        Resume resume = resumeService.getOwnedResume(userId, resumeId);

        // 2. Load Job
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // 3. Load or trigger latest AI Job Analysis (from Phase 4)
        JobAiAnalysis aiJobAnalysis = jobAiAnalysisRepository.findLatestByJobId(jobId)
            .filter(a -> a.getStatus() == AIAnalysisStatus.COMPLETED)
            .orElseGet(() -> jobAiAnalysisService.analyzeJob(jobId, null));

        // 4. Resolve Skill Catalog
        Map<UUID, String> skillCatalog = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        // 5. Build required and preferred job skills
        Set<String> requiredJobSkills = new LinkedHashSet<>();
        Set<String> preferredJobSkills = new LinkedHashSet<>();

        if (job.getJobSkills() != null) {
            for (JobSkill js : job.getJobSkills()) {
                String name = skillCatalog.get(js.getSkillId());
                if (name != null) {
                    if (js.isRequired()) {
                        requiredJobSkills.add(name);
                    } else {
                        preferredJobSkills.add(name);
                    }
                }
            }
        }

        // Augment with AI analyzed technologies
        if (aiJobAnalysis != null && aiJobAnalysis.getTechnologies() != null) {
            for (AnalyzedTechnology tech : aiJobAnalysis.getTechnologies()) {
                if (tech.required()) {
                    requiredJobSkills.add(tech.technology());
                } else {
                    preferredJobSkills.add(tech.technology());
                }
            }
        }

        // 6. Collect skills and technologies represented on the Resume
        Set<String> resumeSkills = new LinkedHashSet<>();
        for (ResumeSkill rs : resume.getSkills()) {
            String name = skillCatalog.getOrDefault(rs.getSkillId(), rs.getSkillName());
            if (name != null && !name.isBlank()) {
                resumeSkills.add(name);
            }
        }

        for (ResumeExperience exp : resume.getExperiences()) {
            resumeSkills.addAll(exp.getTechnologies());
        }

        for (ResumeProject proj : resume.getProjects()) {
            resumeSkills.addAll(proj.getTechnologies());
        }

        // 7. Load Candidate's Verified Skills (Strict Anti-Hallucination Grounding)
        List<UserSkill> verifiedUserSkills = userSkillRepository.findByUserId(userId).stream()
            .filter(UserSkill::isVerified)
            .collect(Collectors.toList());

        Set<String> candidateVerifiedSkillNames = verifiedUserSkills.stream()
            .map(us -> skillCatalog.get(us.getSkillId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // 8. Deterministic Skill Comparisons
        List<String> strongMatches = new ArrayList<>();
        List<String> missingRequiredSkills = new ArrayList<>();
        List<String> missingPreferredSkills = new ArrayList<>();

        for (String req : requiredJobSkills) {
            if (matchesAny(req, resumeSkills)) {
                strongMatches.add(req);
            } else {
                missingRequiredSkills.add(req);
            }
        }

        for (String pref : preferredJobSkills) {
            if (matchesAny(pref, resumeSkills)) {
                if (!strongMatches.contains(pref)) {
                    strongMatches.add(pref);
                }
            } else {
                if (!missingRequiredSkills.contains(pref)) {
                    missingPreferredSkills.add(pref);
                }
            }
        }

        // Verified skills missing from resume:
        // Candidate has proven proficiency in the skill, but current resume does not mention it!
        List<String> verifiedSkillsMissingFromResume = new ArrayList<>();
        for (String verifiedSkill : candidateVerifiedSkillNames) {
            if (!matchesAny(verifiedSkill, resumeSkills)) {
                verifiedSkillsMissingFromResume.add(verifiedSkill);
            }
        }

        // 9. Extract concrete evidence from Resume
        List<ResumeEvidenceItem> resumeEvidence = extractResumeEvidence(resume, strongMatches);

        // 10. Experience Alignment
        ExperienceAlignment experienceAlignment = evaluateExperienceAlignment(resume, job);

        // 11. Qualitative AI Assessment via AIProvider
        List<String> expSummaries = resume.getExperiences().stream()
            .map(e -> e.getJobTitle() + " at " + e.getCompany() + ": " + (e.getDescription() != null ? e.getDescription() : ""))
            .collect(Collectors.toList());

        List<String> projSummaries = resume.getProjects().stream()
            .map(p -> p.getProjectName() + " (" + p.getRole() + "): " + (p.getDescription() != null ? p.getDescription() : ""))
            .collect(Collectors.toList());

        AIResumeAnalysisRequest aiRequest = new AIResumeAnalysisRequest(
            resume.getId(),
            job.getId(),
            resume.getTitle(),
            resume.getSummary(),
            new ArrayList<>(resumeSkills),
            expSummaries,
            projSummaries,
            job.getTitle(),
            job.getCompanyName(),
            new ArrayList<>(requiredJobSkills),
            new ArrayList<>(preferredJobSkills),
            new ArrayList<>(candidateVerifiedSkillNames)
        );

        AIProvider provider = aiProviderFactory.getActiveProvider();
        AIResumeAnalysisResponse aiResponse = provider.analyzeResumeFit(aiRequest);

        return new JobResumeAnalysisResult(
            resume.getId(),
            job.getId(),
            resume.getName(),
            job.getTitle(),
            job.getCompanyName(),
            strongMatches,
            missingRequiredSkills,
            missingPreferredSkills,
            verifiedSkillsMissingFromResume,
            resumeEvidence,
            experienceAlignment,
            aiResponse.qualitativeAssessment(),
            aiResponse.improvementSuggestions(),
            aiResponse.confidence()
        );
    }

    private boolean matchesAny(String query, Set<String> candidates) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        return candidates.stream().anyMatch(c -> {
            String cand = c.trim().toLowerCase(Locale.ROOT);
            return cand.equals(q) || cand.contains(q) || q.contains(cand);
        });
    }

    private List<ResumeEvidenceItem> extractResumeEvidence(Resume resume, List<String> matchedSkills) {
        List<ResumeEvidenceItem> evidence = new ArrayList<>();

        for (String skill : matchedSkills) {
            String skillLower = skill.toLowerCase(Locale.ROOT);
            boolean found = false;

            // 1. Check Skills section
            for (ResumeSkill rs : resume.getSkills()) {
                if (rs.getSkillName() != null && rs.getSkillName().toLowerCase(Locale.ROOT).contains(skillLower)) {
                    evidence.add(new ResumeEvidenceItem(
                        skill,
                        "SKILL",
                        "Skills Section",
                        "Listed skill with proficiency: " + rs.getProficiency()
                    ));
                    found = true;
                    break;
                }
            }

            // 2. Check Experience
            for (ResumeExperience exp : resume.getExperiences()) {
                String expText = (exp.getCompany() + " " + exp.getJobTitle() + " " +
                    exp.getDescription() + " " + String.join(" ", exp.getTechnologies()) + " " +
                    String.join(" ", exp.getAchievements())).toLowerCase(Locale.ROOT);

                if (expText.contains(skillLower)) {
                    String excerpt = !exp.getTechnologies().isEmpty() && exp.getTechnologies().stream().anyMatch(t -> t.toLowerCase().contains(skillLower))
                        ? "Technologies used: " + String.join(", ", exp.getTechnologies())
                        : (exp.getDescription() != null ? exp.getDescription() : "Role held at " + exp.getCompany());
                    evidence.add(new ResumeEvidenceItem(
                        skill,
                        "EXPERIENCE",
                        exp.getJobTitle() + " at " + exp.getCompany(),
                        excerpt
                    ));
                    found = true;
                    break;
                }
            }

            // 3. Check Projects
            for (ResumeProject proj : resume.getProjects()) {
                String projText = (proj.getProjectName() + " " + proj.getRole() + " " +
                    proj.getDescription() + " " + String.join(" ", proj.getTechnologies())).toLowerCase(Locale.ROOT);

                if (projText.contains(skillLower)) {
                    String excerpt = proj.getDescription() != null ? proj.getDescription() : "Project involving " + skill;
                    evidence.add(new ResumeEvidenceItem(
                        skill,
                        "PROJECT",
                        "Project: " + proj.getProjectName(),
                        excerpt
                    ));
                    found = true;
                    break;
                }
            }

            // Fallback if not found in specific section but matched
            if (!found) {
                evidence.add(new ResumeEvidenceItem(
                    skill,
                    "RESUME",
                    "Resume Profile",
                    "Referenced in candidate resume profile."
                ));
            }
        }
        return evidence;
    }

    private ExperienceAlignment evaluateExperienceAlignment(Resume resume, Job job) {
        BigDecimal candidateYears = resume.getYearsOfExperience() != null ? resume.getYearsOfExperience() : BigDecimal.ZERO;
        BigDecimal minYears = job.getExperienceMinYears();
        BigDecimal maxYears = job.getExperienceMaxYears();

        boolean meetsRequirement = true;
        String notes;

        if (minYears != null && candidateYears.compareTo(minYears) < 0) {
            meetsRequirement = false;
            notes = "Candidate has " + candidateYears + " years of experience, which is below the minimum required " + minYears + " years.";
        } else if (minYears != null) {
            notes = "Candidate meets or exceeds minimum requirement of " + minYears + " years (candidate has " + candidateYears + " years).";
        } else {
            notes = "No explicit minimum years of experience specified by the job posting.";
        }

        return new ExperienceAlignment(
            candidateYears,
            minYears,
            maxYears,
            meetsRequirement,
            notes
        );
    }
}
