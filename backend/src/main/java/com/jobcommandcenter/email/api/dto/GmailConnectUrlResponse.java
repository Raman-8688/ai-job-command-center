package com.jobcommandcenter.email.api.dto;

/**
 * Response containing OAuth authorization URL and state token.
 */
public record GmailConnectUrlResponse(
    String authorizationUrl,
    String state
) {}
