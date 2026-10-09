package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateAssessmentRequest(
    AssessmentPlatform platform,

    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration must not exceed 1440 minutes")
    Integer durationMinutes,

    Instant expiresAt,
    Instant scheduledStartTime,

    @Size(max = 500, message = "Assessment URL must not exceed 500 characters")
    String assessmentUrl,

    @Size(max = 100, message = "Access code must not exceed 100 characters")
    String accessCode,

    String submissionNotes,

    @Size(max = 500, message = "Submission repository URL must not exceed 500 characters")
    String submissionRepoUrl,

    Long version
) {}
