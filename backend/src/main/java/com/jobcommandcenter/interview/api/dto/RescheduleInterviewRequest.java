package com.jobcommandcenter.interview.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RescheduleInterviewRequest(
    @NotNull(message = "New scheduled start time is required")
    Instant scheduledStartTime,
    @NotNull(message = "New scheduled end time is required")
    Instant scheduledEndTime,
    String timeZone,
    String reason
) {}
