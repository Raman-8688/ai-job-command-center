package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateApplicationRequest(
    @NotNull(message = "Job ID is required")
    UUID jobId,
    ApplicationStatus status,
    ApplicationSource submissionSource,
    Instant appliedAt,
    String externalReference,
    String notes,
    Instant nextFollowUpDate,
    UUID resumeId,
    UUID tailoredResumeId
) {}
