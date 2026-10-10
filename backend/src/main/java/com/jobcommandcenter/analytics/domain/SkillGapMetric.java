package com.jobcommandcenter.analytics.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a market skill demand vs candidate verification deficit.
 * Highlights high-demand skills in target job postings that the candidate has not yet verified.
 */
public record SkillGapMetric(
    UUID skillId,
    String skillName,
    String category,
    long requiredJobCount,
    long totalTargetJobs,
    BigDecimal marketDemandPercent,
    boolean candidateVerified,
    String candidateProficiency
) {

    public SkillGapMetric {
        Objects.requireNonNull(skillId, "skillId must not be null");
        Objects.requireNonNull(skillName, "skillName must not be null");
        Objects.requireNonNull(category, "category must not be null");
        if (requiredJobCount < 0) {
            throw new IllegalArgumentException("requiredJobCount cannot be negative: " + requiredJobCount);
        }
        if (totalTargetJobs < 0) {
            throw new IllegalArgumentException("totalTargetJobs cannot be negative: " + totalTargetJobs);
        }
        if (requiredJobCount > totalTargetJobs && totalTargetJobs > 0) {
            throw new IllegalArgumentException("requiredJobCount cannot exceed totalTargetJobs");
        }
        if (marketDemandPercent == null) {
            marketDemandPercent = StageConversionRate.calculatePercentage(requiredJobCount, totalTargetJobs);
        }
    }

    public static SkillGapMetric of(
        UUID skillId,
        String skillName,
        String category,
        long requiredJobCount,
        long totalTargetJobs,
        boolean candidateVerified,
        String candidateProficiency
    ) {
        BigDecimal demandRate = StageConversionRate.calculatePercentage(requiredJobCount, totalTargetJobs);
        return new SkillGapMetric(
            skillId,
            skillName,
            category,
            requiredJobCount,
            totalTargetJobs,
            demandRate,
            candidateVerified,
            candidateProficiency
        );
    }
}
