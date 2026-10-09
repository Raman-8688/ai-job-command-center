package com.jobcommandcenter.assessment.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record SubmitAssessmentRequest(
    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    BigDecimal score,

    @DecimalMin(value = "0.0", message = "Max score cannot be negative")
    BigDecimal maxScore,

    String submissionNotes,

    @Size(max = 500, message = "Submission repository URL must not exceed 500 characters")
    String submissionRepoUrl,

    Instant completedAt
) {}
