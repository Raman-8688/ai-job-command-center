package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.AssessmentResult;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecordAssessmentResultRequest(
    @NotNull(message = "Assessment result is required")
    AssessmentResult result,

    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    BigDecimal score,

    @DecimalMin(value = "0.0", message = "Max score cannot be negative")
    BigDecimal maxScore,

    String notes
) {}
