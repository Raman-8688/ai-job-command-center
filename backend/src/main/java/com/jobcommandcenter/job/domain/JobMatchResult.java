package com.jobcommandcenter.job.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Value object representing a deterministic, explainable job fit calculation.
 * Ensures missing required skills are explicitly highlighted and cannot be hidden by a high score.
 */
public record JobMatchResult(
    UUID jobId,
    UUID userId,
    int overallScore,
    int titleScore,
    int requiredSkillScore,
    int preferredSkillScore,
    int experienceScore,
    int locationScore,
    int workModeScore,
    List<String> matchedSkills,
    List<String> missingRequiredSkills,
    List<String> matchedPreferredSkills,
    List<String> missingPreferredSkills,
    List<String> matchReasons
) {
    public JobMatchResult {
        matchedSkills = matchedSkills != null ? List.copyOf(matchedSkills) : List.of();
        missingRequiredSkills = missingRequiredSkills != null ? List.copyOf(missingRequiredSkills) : List.of();
        matchedPreferredSkills = matchedPreferredSkills != null ? List.copyOf(matchedPreferredSkills) : List.of();
        missingPreferredSkills = missingPreferredSkills != null ? List.copyOf(missingPreferredSkills) : List.of();
        matchReasons = matchReasons != null ? List.copyOf(matchReasons) : List.of();
    }

    public boolean hasMissingRequiredSkills() {
        return !missingRequiredSkills.isEmpty();
    }
}
