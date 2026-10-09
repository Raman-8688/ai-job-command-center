package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.application.domain.ApplicationSource;

import java.time.Instant;

public record UpdateApplicationRequest(
    ApplicationSource submissionSource,
    String externalReference,
    Instant appliedAt,
    Instant nextFollowUpDate,
    String notes
) {}
