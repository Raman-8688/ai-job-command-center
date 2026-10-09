package com.jobcommandcenter.ai.domain;

/**
 * AI response for job information extracted from an email.
 */
public record AIEmailJobExtractionResponse(
    String companyName,
    String jobTitle,
    String externalJobId,
    String nextSteps,
    String notes
) {}
