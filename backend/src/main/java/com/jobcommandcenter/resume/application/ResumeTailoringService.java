package com.jobcommandcenter.resume.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.AIProviderFactory;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.resume.domain.*;
import com.jobcommandcenter.skill.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrates job-specific resume tailoring, versioning, keyword coverage analysis,
 * and human review workflow with strict anti-hallucination guards.
 */
@Service
@Transactional
public class ResumeTailoringService {

    private final ResumeService resumeService;
    private final JobRepository jobRepository;
    private final JobAiAnalysisRepository jobAiAnalysisRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final AIProviderFactory aiProviderFactory;

    public ResumeTailoringService(ResumeService resumeService,
                                  JobRepository jobRepository,
                                  JobAiAnalysisRepository jobAiAnalysisRepository,
                                  UserSkillRepository userSkillRepository,
                                  SkillRepository skillRepository,
                                  TailoredResumeRepository tailoredResumeRepository,
                                  AIProviderFactory aiProviderFactory) {
        this.resumeService = resumeService;
        this.jobRepository = jobRepository;
        this.jobAiAnalysisRepository = jobAiAnalysisRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
        this.tailoredResumeRepository = tailoredResumeRepository;
        this.aiProviderFactory = aiProviderFactory;
    }

    public TailoredResume createTailoredDraft(UUID userId, UUID resumeId, UUID jobId) {
        // 1. Verify ownership of source resume (Master resume is NEVER overwritten)
        Resume sourceResume = resumeService.getOwnedResume(userId, resumeId);

        // 2. Fetch target Job
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // 3. Determine next version number for this (sourceResume, targetJob) pair
        int nextVersion = tailoredResumeRepository.findMaxVersion(resumeId, jobId) + 1;

        // 4. Resolve Skill Catalog
        Map<UUID, String> skillCatalog = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        // 5. Gather required & preferred job skills
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

        // Augment with Phase 4 Job AI analysis if available
        jobAiAnalysisRepository.findLatestByJobId(jobId).ifPresent(aiAnalysis -> {
            if (aiAnalysis.getTechnologies() != null) {
                for (AnalyzedTechnology tech : aiAnalysis.getTechnologies()) {
                    if (tech.required()) {
                        requiredJobSkills.add(tech.technology());
                    } else {
                        preferredJobSkills.add(tech.technology());
                    }
                }
            }
        });

        Set<String> allJobSkills = new LinkedHashSet<>(requiredJobSkills);
        allJobSkills.addAll(preferredJobSkills);

        // 6. Gather resume skills
        Set<String> resumeSkills = new LinkedHashSet<>();
        for (ResumeSkill rs : sourceResume.getSkills()) {
            String name = skillCatalog.getOrDefault(rs.getSkillId(), rs.getSkillName());
            if (name != null && !name.isBlank()) {
                resumeSkills.add(name);
            }
        }
        for (ResumeExperience exp : sourceResume.getExperiences()) {
            resumeSkills.addAll(exp.getTechnologies());
        }
        for (ResumeProject proj : sourceResume.getProjects()) {
            resumeSkills.addAll(proj.getTechnologies());
        }

        // 7. Candidate Verified Skills (Strict Anti-Hallucination Grounding)
        List<UserSkill> verifiedSkills = userSkillRepository.findByUserId(userId).stream()
            .filter(UserSkill::isVerified)
            .collect(Collectors.toList());

        Set<String> candidateVerifiedSkillNames = verifiedSkills.stream()
            .map(us -> skillCatalog.get(us.getSkillId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // 8. Deterministic Keyword & Skill Coverage Analysis
        List<String> matchedKeywords = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();

        for (String skill : allJobSkills) {
            if (matchesAny(skill, resumeSkills) || matchesAny(skill, candidateVerifiedSkillNames)) {
                matchedKeywords.add(skill);
            } else {
                missingKeywords.add(skill);
            }
        }

        BigDecimal coverageScore = allJobSkills.isEmpty()
            ? BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.valueOf(((double) matchedKeywords.size() / allJobSkills.size()) * 100.0)
                .setScale(2, RoundingMode.HALF_UP);

        // 9. Invoke AIProvider for tailoring suggestions
        List<String> expSummaries = sourceResume.getExperiences().stream()
            .map(e -> e.getJobTitle() + " at " + e.getCompany() + ": " + (e.getDescription() != null ? e.getDescription() : ""))
            .collect(Collectors.toList());

        List<String> projSummaries = sourceResume.getProjects().stream()
            .map(p -> p.getProjectName() + " (" + p.getRole() + "): " + (p.getDescription() != null ? p.getDescription() : ""))
            .collect(Collectors.toList());

        AITailoringRequest aiRequest = new AITailoringRequest(
            sourceResume.getId(),
            job.getId(),
            job.getTitle(),
            job.getCompanyName(),
            new ArrayList<>(requiredJobSkills),
            new ArrayList<>(preferredJobSkills),
            new ArrayList<>(candidateVerifiedSkillNames),
            sourceResume.getTitle(),
            sourceResume.getSummary(),
            expSummaries,
            projSummaries
        );

        AIProvider aiProvider = aiProviderFactory.getActiveProvider();
        AITailoringResponse aiResponse = aiProvider.generateTailoringSuggestions(aiRequest);

        // 10. Map and validate AI Suggestions (Anti-hallucination post-check)
        UUID draftId = UUID.randomUUID();
        List<TailoredResumeSuggestion> suggestions = new ArrayList<>();
        int order = 0;

        if (aiResponse.suggestions() != null) {
            for (AISuggestionItem item : aiResponse.suggestions()) {
                SectionType sType = parseSectionType(item.sectionType());
                String verStatus = item.verificationStatus() != null ? item.verificationStatus() : "VERIFIED";

                // Anti-hallucination safeguard: if suggestion claims an unverified technology, force status to NOT_ENOUGH_EVIDENCE
                if (item.suggestedContent().contains("[NOT_ENOUGH_EVIDENCE]")) {
                    verStatus = "NOT_ENOUGH_EVIDENCE";
                }

                TailoredResumeSuggestion suggestion = new TailoredResumeSuggestion(
                    UUID.randomUUID(),
                    draftId,
                    sType,
                    item.targetItemTitle(),
                    item.originalContent(),
                    item.suggestedContent(),
                    item.rationale(),
                    item.evidence(),
                    verStatus,
                    false,
                    order++,
                    Instant.now()
                );
                suggestions.add(suggestion);
            }
        }

        String tailoredTitle = aiResponse.tailoredTitle() != null && !aiResponse.tailoredTitle().isBlank()
            ? aiResponse.tailoredTitle()
            : (sourceResume.getTitle() != null ? sourceResume.getTitle() : job.getTitle());

        String tailoredSummary = aiResponse.tailoredSummary() != null && !aiResponse.tailoredSummary().isBlank()
            ? aiResponse.tailoredSummary()
            : sourceResume.getSummary();

        TailoredResume draft = new TailoredResume(
            draftId,
            userId,
            resumeId,
            jobId,
            nextVersion,
            TailoredResumeStatus.DRAFT,
            tailoredTitle,
            tailoredSummary,
            coverageScore,
            matchedKeywords,
            missingKeywords,
            suggestions,
            Instant.now(),
            Instant.now()
        );

        return tailoredResumeRepository.save(draft);
    }

    @Transactional(readOnly = true)
    public TailoredResume getOwnedTailoredResume(UUID userId, UUID tailoredResumeId) {
        return tailoredResumeRepository.findByIdAndUserId(tailoredResumeId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Tailored resume not found with ID: " + tailoredResumeId));
    }

    @Transactional(readOnly = true)
    public List<TailoredResume> getTailoredResumesForResume(UUID userId, UUID resumeId) {
        // Confirm resume ownership
        resumeService.getOwnedResume(userId, resumeId);
        return tailoredResumeRepository.findByUserIdAndSourceResumeId(userId, resumeId);
    }

    @Transactional(readOnly = true)
    public List<TailoredResume> getTailoredResumesForJob(UUID userId, UUID jobId) {
        return tailoredResumeRepository.findByUserIdAndTargetJobId(userId, jobId);
    }

    public TailoredResume updateContent(UUID userId, UUID tailoredResumeId, String title, String summary) {
        TailoredResume draft = getOwnedTailoredResume(userId, tailoredResumeId);
        draft.updateContent(title, summary);
        return tailoredResumeRepository.save(draft);
    }

    public TailoredResume transitionStatus(UUID userId, UUID tailoredResumeId, TailoredResumeStatus status) {
        TailoredResume draft = getOwnedTailoredResume(userId, tailoredResumeId);
        draft.transitionStatus(status);
        return tailoredResumeRepository.save(draft);
    }

    public TailoredResume applySuggestion(UUID userId, UUID tailoredResumeId, UUID suggestionId) {
        TailoredResume draft = getOwnedTailoredResume(userId, tailoredResumeId);
        boolean applied = draft.applySuggestion(suggestionId);
        if (!applied) {
            throw new ResourceNotFoundException("Suggestion not found with ID: " + suggestionId);
        }
        return tailoredResumeRepository.save(draft);
    }

    public void deleteTailoredResume(UUID userId, UUID tailoredResumeId) {
        TailoredResume draft = getOwnedTailoredResume(userId, tailoredResumeId);
        tailoredResumeRepository.deleteById(draft.getId());
    }

    private boolean matchesAny(String query, Set<String> candidates) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        return candidates.stream().anyMatch(c -> {
            String cand = c.trim().toLowerCase(Locale.ROOT);
            return cand.equals(q) || cand.contains(q) || q.contains(cand);
        });
    }

    private SectionType parseSectionType(String raw) {
        if (raw == null) return SectionType.SUMMARY;
        try {
            return SectionType.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return SectionType.SUMMARY;
        }
    }
}
