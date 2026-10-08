package com.jobcommandcenter.user.api;

import java.util.UUID;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresInMs,
    UUID userId,
    String email,
    String displayName,
    String role
) {}
