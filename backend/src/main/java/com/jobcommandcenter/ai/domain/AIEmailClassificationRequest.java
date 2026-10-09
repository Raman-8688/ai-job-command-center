package com.jobcommandcenter.ai.domain;

/**
 * Request payload for AI email classification.
 */
public record AIEmailClassificationRequest(
    String subject,
    String sender,
    String snippet,
    String bodyPlain
) {}
