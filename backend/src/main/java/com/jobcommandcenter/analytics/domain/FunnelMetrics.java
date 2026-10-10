package com.jobcommandcenter.analytics.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value object summarizing overall candidate pipeline health and stage-by-stage conversions.
 * Guarantees division-by-zero safety across all summary metrics.
 */
public record FunnelMetrics(
    long totalApplications,
    long activeApplications,
    long draftsCount,
    long appliedCount,
    long screeningCount,
    long assessmentCount,
    long interviewCount,
    long offerCount,
    long acceptedCount,
    long rejectedCount,
    long withdrawnCount,
    long archivedCount,
    BigDecimal screeningConversionRate,
    BigDecimal assessmentConversionRate,
    BigDecimal interviewConversionRate,
    BigDecimal offerConversionRate,
    BigDecimal overallAcceptanceRate,
    List<StageConversionRate> stageConversions
) {

    public FunnelMetrics {
        if (totalApplications < 0) throw new IllegalArgumentException("totalApplications cannot be negative");
        if (activeApplications < 0) throw new IllegalArgumentException("activeApplications cannot be negative");
        if (draftsCount < 0) throw new IllegalArgumentException("draftsCount cannot be negative");
        if (appliedCount < 0) throw new IllegalArgumentException("appliedCount cannot be negative");
        if (screeningCount < 0) throw new IllegalArgumentException("screeningCount cannot be negative");
        if (assessmentCount < 0) throw new IllegalArgumentException("assessmentCount cannot be negative");
        if (interviewCount < 0) throw new IllegalArgumentException("interviewCount cannot be negative");
        if (offerCount < 0) throw new IllegalArgumentException("offerCount cannot be negative");
        if (acceptedCount < 0) throw new IllegalArgumentException("acceptedCount cannot be negative");
        if (rejectedCount < 0) throw new IllegalArgumentException("rejectedCount cannot be negative");
        if (withdrawnCount < 0) throw new IllegalArgumentException("withdrawnCount cannot be negative");
        if (archivedCount < 0) throw new IllegalArgumentException("archivedCount cannot be negative");

        if (screeningConversionRate == null) {
            screeningConversionRate = StageConversionRate.calculatePercentage(screeningCount, totalApplications);
        }
        if (assessmentConversionRate == null) {
            assessmentConversionRate = StageConversionRate.calculatePercentage(assessmentCount, totalApplications);
        }
        if (interviewConversionRate == null) {
            interviewConversionRate = StageConversionRate.calculatePercentage(interviewCount, totalApplications);
        }
        if (offerConversionRate == null) {
            offerConversionRate = StageConversionRate.calculatePercentage(offerCount, totalApplications);
        }
        if (overallAcceptanceRate == null) {
            overallAcceptanceRate = StageConversionRate.calculatePercentage(acceptedCount, totalApplications);
        }

        stageConversions = stageConversions != null ? List.copyOf(stageConversions) : Collections.emptyList();
    }

    /**
     * Creates an empty FunnelMetrics object for new users or zero-data scenarios.
     */
    public static FunnelMetrics empty() {
        return new FunnelMetrics(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            Collections.emptyList()
        );
    }
}
