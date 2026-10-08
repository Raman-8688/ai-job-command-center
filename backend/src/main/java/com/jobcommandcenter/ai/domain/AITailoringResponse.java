package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Structured response containing section suggestions and overall tailored summary.
 */
public record AITailoringResponse(
    String tailoredTitle,
    String tailoredSummary,
    List<AISuggestionItem> suggestions,
    BigDecimal confidence
) {
}
