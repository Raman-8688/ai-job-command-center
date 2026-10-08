package com.jobcommandcenter.resume.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateTailoredResumeRequest(
    @Size(max = 255, message = "Tailored title must not exceed 255 characters")
    String tailoredTitle,

    String tailoredSummary
) {
}
