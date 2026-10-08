package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Structured qualitative assessment returned by AIProvider for resume-job evaluation.
 */
public record AIResumeAnalysisResponse(
    String qualitativeAssessment,
    List<String> responsibilityAlignment,
    List<String> improvementSuggestions,
    BigDecimal confidence
) {
    public AIResumeAnalysisResponse {
        responsibilityAlignment = responsibilityAlignment != null ? List.copyOf(responsibilityAlignment) : List.of();
        improvementSuggestions = improvementSuggestions != null ? List.copyOf(improvementSuggestions) : List.of();
        confidence = confidence != null ? confidence : new BigDecimal("0.90");
    }
}
