package com.jobcommandcenter.analytics.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing stage-to-stage transition metrics in the candidate funnel.
 * Encapsulates conversion rate percentage and median stage duration without division-by-zero errors.
 */
public record StageConversionRate(
    String fromStage,
    String toStage,
    long enteredCount,
    long progressedCount,
    BigDecimal conversionRatePercent,
    Double medianDaysInStage
) {

    public StageConversionRate {
        Objects.requireNonNull(fromStage, "fromStage must not be null");
        Objects.requireNonNull(toStage, "toStage must not be null");
        if (enteredCount < 0) {
            throw new IllegalArgumentException("enteredCount cannot be negative: " + enteredCount);
        }
        if (progressedCount < 0) {
            throw new IllegalArgumentException("progressedCount cannot be negative: " + progressedCount);
        }
        if (progressedCount > enteredCount) {
            throw new IllegalArgumentException("progressedCount (" + progressedCount + ") cannot exceed enteredCount (" + enteredCount + ")");
        }
        if (conversionRatePercent == null) {
            conversionRatePercent = calculatePercentage(progressedCount, enteredCount);
        }
        if (medianDaysInStage != null && medianDaysInStage < 0.0) {
            throw new IllegalArgumentException("medianDaysInStage cannot be negative: " + medianDaysInStage);
        }
    }

    /**
     * Factory constructor computing conversionRatePercent deterministically.
     */
    public static StageConversionRate of(
        String fromStage,
        String toStage,
        long enteredCount,
        long progressedCount,
        Double medianDaysInStage
    ) {
        BigDecimal rate = calculatePercentage(progressedCount, enteredCount);
        return new StageConversionRate(fromStage, toStage, enteredCount, progressedCount, rate, medianDaysInStage);
    }

    /**
     * Calculates safe 2-decimal percentage (0.00 to 100.00). Returns 0.00 when denominator is zero.
     */
    public static BigDecimal calculatePercentage(long numerator, long denominator) {
        if (denominator <= 0 || numerator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
