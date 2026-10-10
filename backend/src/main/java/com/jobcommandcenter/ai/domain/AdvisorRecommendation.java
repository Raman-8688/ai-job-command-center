package com.jobcommandcenter.ai.domain;

/**
 * Value object representing an actionable career recommendation linked to an observed analytical metric.
 */
public record AdvisorRecommendation(
    String action,
    String reason,
    String priority,
    String metricReference
) {
    public AdvisorRecommendation {
        if (action == null) action = "";
        if (reason == null) reason = "";
        if (priority == null) priority = "MEDIUM";
    }
}
