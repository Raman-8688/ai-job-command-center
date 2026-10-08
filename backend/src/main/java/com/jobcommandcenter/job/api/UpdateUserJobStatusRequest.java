package com.jobcommandcenter.job.api;

import com.jobcommandcenter.job.domain.UserJobStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserJobStatusRequest(
    @NotNull(message = "Status cannot be null")
    UserJobStatus status,

    String notes
) {}
