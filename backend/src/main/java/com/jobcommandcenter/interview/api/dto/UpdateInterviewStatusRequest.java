package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewOutcome;
import com.jobcommandcenter.interview.domain.InterviewStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInterviewStatusRequest(
    @NotNull(message = "Target status is required")
    InterviewStatus status,
    InterviewOutcome outcome,
    String feedback,
    String notes,
    String source
) {}
