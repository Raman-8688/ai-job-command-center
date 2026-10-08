package com.jobcommandcenter.resume.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Result of job-specific resume analysis.
 * Combines deterministic skill & experience mapping with qualitative AI assessment.
 */
public record JobResumeAnalysisResult(
    UUID resumeId,
    UUID jobId,
    String resumeName,
    String jobTitle,
    String companyName,
    List<String> strongMatches,
    List<String> missingRequiredSkills,
    List<String> missingPreferredSkills,
    List<String> verifiedSkillsMissingFromResume,
    List<ResumeEvidenceItem> resumeEvidence,
    ExperienceAlignment experienceAlignment,
    String aiAssessment,
    List<String> improvementSuggestions,
    BigDecimal confidence
) {}
