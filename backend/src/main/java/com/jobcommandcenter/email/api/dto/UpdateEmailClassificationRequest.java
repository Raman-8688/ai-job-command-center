package com.jobcommandcenter.email.api.dto;

import com.jobcommandcenter.email.domain.EmailClassification;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload to manually override an email's classification.
 */
public record UpdateEmailClassificationRequest(
    @NotNull(message = "Classification must not be null")
    EmailClassification classification
) {}
