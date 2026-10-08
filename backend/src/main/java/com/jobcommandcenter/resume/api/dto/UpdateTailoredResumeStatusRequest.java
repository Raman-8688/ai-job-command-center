package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.resume.domain.TailoredResumeStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTailoredResumeStatusRequest(
    @NotNull(message = "Status cannot be null")
    TailoredResumeStatus status
) {
}
