package com.jobcommandcenter.ai.domain;

/**
 * Request payload for extracting job information from email contents.
 */
public record AIEmailJobExtractionRequest(
    String subject,
    String sender,
    String bodyPlain
) {}
