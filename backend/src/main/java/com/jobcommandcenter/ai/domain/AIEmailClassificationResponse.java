package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;

/**
 * AI response for email classification.
 */
public record AIEmailClassificationResponse(
    String classification,
    BigDecimal confidence,
    String rationale
) {}
