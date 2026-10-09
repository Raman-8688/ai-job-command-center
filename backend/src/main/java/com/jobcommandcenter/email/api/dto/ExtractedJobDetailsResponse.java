package com.jobcommandcenter.email.api.dto;

/**
 * Extracted job intelligence details from email contents.
 */
public record ExtractedJobDetailsResponse(
    String companyName,
    String jobTitle,
    String externalJobId,
    String nextSteps,
    String notes
) {}
