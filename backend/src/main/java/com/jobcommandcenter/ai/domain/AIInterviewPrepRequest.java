package com.jobcommandcenter.ai.domain;

import java.util.List;

/**
 * Contextual payload for requesting AI-driven interview question preparation.
 */
public record AIInterviewPrepRequest(
    String jobTitle,
    String companyName,
    String jobDescription,
    String round,
    List<String> candidateVerifiedSkills,
    List<String> candidateExperiences
) {
    public AIInterviewPrepRequest {
        if (candidateVerifiedSkills == null) {
            candidateVerifiedSkills = List.of();
        }
        if (candidateExperiences == null) {
            candidateExperiences = List.of();
        }
    }
}
