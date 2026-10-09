package com.jobcommandcenter.email.api.dto;

import com.jobcommandcenter.email.domain.EmailProcessingStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload to update an email's workflow processing status.
 */
public record UpdateProcessingStatusRequest(
    @NotNull(message = "Processing status must not be null")
    EmailProcessingStatus processingStatus
) {}
