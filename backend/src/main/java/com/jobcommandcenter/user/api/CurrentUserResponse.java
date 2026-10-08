package com.jobcommandcenter.user.api;

import com.jobcommandcenter.user.domain.AccountStatus;
import com.jobcommandcenter.user.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record CurrentUserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String displayName,
    Role role,
    AccountStatus accountStatus,
    Instant createdAt,
    Instant lastLoginAt
) {}
