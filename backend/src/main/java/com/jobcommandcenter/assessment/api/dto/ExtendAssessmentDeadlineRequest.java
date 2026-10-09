package com.jobcommandcenter.assessment.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ExtendAssessmentDeadlineRequest(
    @NotNull(message = "New expiration timestamp is required")
    Instant newExpiresAt,

    String reason
) {}
