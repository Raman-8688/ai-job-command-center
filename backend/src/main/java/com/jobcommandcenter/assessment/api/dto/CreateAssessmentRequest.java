package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateAssessmentRequest(
    @NotNull(message = "Job ID is required")
    UUID jobId,

    UUID applicationId,
    UUID interviewId,

    @NotNull(message = "Assessment platform is required")
    AssessmentPlatform platform,

    @NotBlank(message = "Assessment title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration must not exceed 1440 minutes (24 hours)")
    Integer durationMinutes,

    Instant invitedAt,
    Instant expiresAt,
    Instant scheduledStartTime,

    @Size(max = 500, message = "Assessment URL must not exceed 500 characters")
    String assessmentUrl,

    @Size(max = 100, message = "Access code must not exceed 100 characters")
    String accessCode,

    String submissionNotes
) {}
