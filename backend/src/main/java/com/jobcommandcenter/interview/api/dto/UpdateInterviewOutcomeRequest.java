package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewOutcome;
import jakarta.validation.constraints.NotNull;

public record UpdateInterviewOutcomeRequest(
    @NotNull(message = "Outcome is required")
    InterviewOutcome outcome,
    String notes
) {}
