package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.EventSource;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TransitionStatusRequest(
    @NotNull(message = "Target status is required")
    ApplicationStatus targetStatus,
    String notes,
    EventSource eventSource,
    Instant occurredAt
) {}
