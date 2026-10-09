package com.jobcommandcenter.ai.domain;

import java.util.List;

/**
 * Contextual payload for requesting AI-generated technical assessment briefing and study checklist.
 */
public record AIAssessmentBriefingRequest(
    String jobTitle,
    String companyName,
    String jobDescription,
    String platform,
    Integer durationMinutes,
    List<String> canonicalJobRequirements,
    List<String> candidateVerifiedSkills,
    String notes
) {
    public AIAssessmentBriefingRequest {
        if (canonicalJobRequirements == null) {
            canonicalJobRequirements = List.of();
        }
        if (candidateVerifiedSkills == null) {
            candidateVerifiedSkills = List.of();
        }
        if (notes == null) {
            notes = "";
        }
    }
}
