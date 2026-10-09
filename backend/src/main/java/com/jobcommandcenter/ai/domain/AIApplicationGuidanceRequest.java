package com.jobcommandcenter.ai.domain;

/**
 * Request payload for AI-assisted application guidance and follow-up generation.
 */
public record AIApplicationGuidanceRequest(
    String companyName,
    String jobTitle,
    String currentStatus,
    int daysSinceApplied,
    String lastEventNotes,
    String followUpDeadline
) {}
