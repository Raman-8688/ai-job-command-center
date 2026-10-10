package com.jobcommandcenter.analytics.domain;

import com.jobcommandcenter.application.domain.ApplicationSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing conversion and efficacy metrics for an application source/channel.
 * Tracks total submitted, screenings, interviews, offers, and conversion ratios.
 */
public record SourceEffectiveness(
    ApplicationSource source,
    long totalApplications,
    long screeningsReached,
    long interviewsReached,
    long offersReceived,
    BigDecimal screeningRatePercent,
    BigDecimal interviewRatePercent,
    BigDecimal offerRatePercent
) {

    public SourceEffectiveness {
        Objects.requireNonNull(source, "Application source must not be null");
        if (totalApplications < 0) {
            throw new IllegalArgumentException("totalApplications cannot be negative: " + totalApplications);
        }
        if (screeningsReached < 0) {
            throw new IllegalArgumentException("screeningsReached cannot be negative: " + screeningsReached);
        }
        if (interviewsReached < 0) {
            throw new IllegalArgumentException("interviewsReached cannot be negative: " + interviewsReached);
        }
        if (offersReceived < 0) {
            throw new IllegalArgumentException("offersReceived cannot be negative: " + offersReceived);
        }
        if (screeningsReached > totalApplications) {
            throw new IllegalArgumentException("screeningsReached cannot exceed totalApplications");
        }
        if (interviewsReached > totalApplications) {
            throw new IllegalArgumentException("interviewsReached cannot exceed totalApplications");
        }
        if (offersReceived > totalApplications) {
            throw new IllegalArgumentException("offersReceived cannot exceed totalApplications");
        }
        if (screeningRatePercent == null) {
            screeningRatePercent = StageConversionRate.calculatePercentage(screeningsReached, totalApplications);
        }
        if (interviewRatePercent == null) {
            interviewRatePercent = StageConversionRate.calculatePercentage(interviewsReached, totalApplications);
        }
        if (offerRatePercent == null) {
            offerRatePercent = StageConversionRate.calculatePercentage(offersReceived, totalApplications);
        }
    }

    public static SourceEffectiveness of(
        ApplicationSource source,
        long totalApplications,
        long screeningsReached,
        long interviewsReached,
        long offersReceived
    ) {
        return new SourceEffectiveness(
            source,
            totalApplications,
            screeningsReached,
            interviewsReached,
            offersReceived,
            StageConversionRate.calculatePercentage(screeningsReached, totalApplications),
            StageConversionRate.calculatePercentage(interviewsReached, totalApplications),
            StageConversionRate.calculatePercentage(offersReceived, totalApplications)
        );
    }
}
