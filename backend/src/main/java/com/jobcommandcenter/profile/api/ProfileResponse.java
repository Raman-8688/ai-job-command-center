package com.jobcommandcenter.profile.api;

import com.jobcommandcenter.profile.domain.WorkPreference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    UUID userId,
    String phone,
    String location,
    String linkedInUrl,
    String gitHubUrl,
    String portfolioUrl,
    List<String> targetRoles,
    List<String> preferredLocations,
    WorkPreference workPreference,
    BigDecimal yearsExperience,
    int noticePeriodDays,
    String currentCompany,
    String currentDesignation,
    Instant createdAt,
    Instant updatedAt
) {}
