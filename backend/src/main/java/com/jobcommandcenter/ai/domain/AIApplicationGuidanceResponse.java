package com.jobcommandcenter.ai.domain;

/**
 * AI response providing advisory next-action guidance and drafted communication.
 */
public record AIApplicationGuidanceResponse(
    String recommendedAction,
    String rationale,
    String draftedFollowUpMessage
) {}
