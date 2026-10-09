package com.jobcommandcenter.email.infrastructure.client;

import java.time.Instant;

/**
 * Encapsulated OAuth 2.0 token response.
 */
public record OAuthTokens(
    String accessToken,
    String refreshToken,
    Instant expiresAt,
    String scopes
) {}
