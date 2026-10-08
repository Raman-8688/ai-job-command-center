package com.jobcommandcenter.job.api;

import com.jobcommandcenter.job.domain.JobMatchResult;

import java.util.List;
import java.util.UUID;

public record JobMatchResponse(
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
    List<String> matchReasons,
    boolean hasMissingRequiredSkills
) {
    public static JobMatchResponse fromResult(JobMatchResult result) {
        return new JobMatchResponse(
            result.jobId(),
            result.userId(),
            result.overallScore(),
            result.titleScore(),
            result.requiredSkillScore(),
            result.preferredSkillScore(),
            result.experienceScore(),
            result.locationScore(),
            result.workModeScore(),
            result.matchedSkills(),
            result.missingRequiredSkills(),
            result.matchedPreferredSkills(),
            result.missingPreferredSkills(),
            result.matchReasons(),
            result.hasMissingRequiredSkills()
        );
    }
}
