package com.jobcommandcenter.ai.domain;

import java.util.List;
import java.util.UUID;

/**
 * Structured request for generating qualitative resume tailoring recommendations.
 */
public record AITailoringRequest(
    UUID sourceResumeId,
    UUID targetJobId,
    String jobTitle,
    String jobCompanyName,
    List<String> requiredJobSkills,
    List<String> preferredJobSkills,
    List<String> candidateVerifiedSkills,
    String resumeTitle,
    String resumeSummary,
    List<String> experiences,
    List<String> projects
) {
}
