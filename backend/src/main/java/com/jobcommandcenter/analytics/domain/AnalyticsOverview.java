package com.jobcommandcenter.analytics.domain;

import java.math.BigDecimal;

/**
 * Value object representing top-level career pipeline KPIs for a candidate.
 * Guarantees zero-division safety and null-safe BigDecimal scaling.
 */
public record AnalyticsOverview(
    long totalApplications,
    long activePipelines,
    long interviewsCount,
    long assessmentsCount,
    long activeOffers,
    long rejectionsCount,
    BigDecimal interviewConversionRatePercent,
    BigDecimal assessmentPassRatePercent,
    BigDecimal offerRatePercent
) {

    public AnalyticsOverview {
        if (totalApplications < 0) throw new IllegalArgumentException("totalApplications cannot be negative");
        if (activePipelines < 0) throw new IllegalArgumentException("activePipelines cannot be negative");
        if (interviewsCount < 0) throw new IllegalArgumentException("interviewsCount cannot be negative");
        if (assessmentsCount < 0) throw new IllegalArgumentException("assessmentsCount cannot be negative");
        if (activeOffers < 0) throw new IllegalArgumentException("activeOffers cannot be negative");
        if (rejectionsCount < 0) throw new IllegalArgumentException("rejectionsCount cannot be negative");

        if (interviewConversionRatePercent == null) {
            interviewConversionRatePercent = StageConversionRate.calculatePercentage(interviewsCount, totalApplications);
        }
        if (assessmentPassRatePercent == null) {
            assessmentPassRatePercent = BigDecimal.ZERO.setScale(2);
        }
        if (offerRatePercent == null) {
            offerRatePercent = StageConversionRate.calculatePercentage(activeOffers, totalApplications);
        }
    }

    public static AnalyticsOverview empty() {
        return new AnalyticsOverview(
            0, 0, 0, 0, 0, 0,
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2),
            BigDecimal.ZERO.setScale(2)
        );
    }
}
