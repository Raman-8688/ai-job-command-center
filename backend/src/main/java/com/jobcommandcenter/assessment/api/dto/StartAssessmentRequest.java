package com.jobcommandcenter.assessment.api.dto;

import java.time.Instant;

public record StartAssessmentRequest(
    Instant startTime,
    String notes
) {}
