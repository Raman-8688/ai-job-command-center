package com.jobcommandcenter.ai.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Request payload dispatched to AIProvider for job-specific resume analysis.
 */
public record AIResumeAnalysisRequest(
    UUID resumeId,
    UUID jobId,
    String resumeTitle,
    String resumeSummary,
    List<String> resumeSkills,
    List<String> resumeExperienceSummaries,
    List<String> resumeProjectSummaries,
    String jobTitle,
    String jobCompany,
    List<String> jobRequiredSkills,
    List<String> jobPreferredSkills,
    List<String> candidateVerifiedSkills
) {
    public AIResumeAnalysisRequest {
        Objects.requireNonNull(resumeId, "Resume ID cannot be null");
        Objects.requireNonNull(jobId, "Job ID cannot be null");
        resumeSkills = resumeSkills != null ? List.copyOf(resumeSkills) : List.of();
        resumeExperienceSummaries = resumeExperienceSummaries != null ? List.copyOf(resumeExperienceSummaries) : List.of();
        resumeProjectSummaries = resumeProjectSummaries != null ? List.copyOf(resumeProjectSummaries) : List.of();
        jobRequiredSkills = jobRequiredSkills != null ? List.copyOf(jobRequiredSkills) : List.of();
        jobPreferredSkills = jobPreferredSkills != null ? List.copyOf(jobPreferredSkills) : List.of();
        candidateVerifiedSkills = candidateVerifiedSkills != null ? List.copyOf(candidateVerifiedSkills) : List.of();
    }
}
