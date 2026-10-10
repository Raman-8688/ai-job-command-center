package com.jobcommandcenter.ai.domain;

import java.util.List;
import java.util.Map;

/**
 * Value object representing a prioritized bottleneck diagnosed from candidate pipeline metrics.
 */
public record AdvisorBottleneck(
    String category,
    String title,
    String explanation,
    Map<String, String> supportingMetrics,
    String priority,
    List<String> recommendations
) {
    public AdvisorBottleneck {
        if (category == null) category = "GENERAL";
        if (title == null) title = "Pipeline Bottleneck";
        if (explanation == null) explanation = "";
        if (supportingMetrics == null) supportingMetrics = Map.of();
        if (priority == null) priority = "MEDIUM";
        if (recommendations == null) recommendations = List.of();
    }
}
