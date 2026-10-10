package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Structured AI response containing evidence-grounded pipeline diagnoses and tactical recommendations.
 */
public record AIAnalyticsAdvisorResponse(
    String summary,
    List<AdvisorBottleneck> bottlenecks,
    List<AdvisorRecommendation> recommendations,
    List<String> strengths,
    List<String> dataLimitations,
    BigDecimal confidenceScore,
    Instant generatedAt
) {
    public AIAnalyticsAdvisorResponse {
        if (summary == null) summary = "";
        if (bottlenecks == null) bottlenecks = List.of();
        if (recommendations == null) recommendations = List.of();
        if (strengths == null) strengths = List.of();
        if (dataLimitations == null) dataLimitations = List.of();
        if (confidenceScore == null) confidenceScore = new BigDecimal("0.90");
        if (generatedAt == null) generatedAt = Instant.now();
    }
}
