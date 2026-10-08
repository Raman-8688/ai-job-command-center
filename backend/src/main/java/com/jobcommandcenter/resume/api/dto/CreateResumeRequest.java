package com.jobcommandcenter.resume.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateResumeRequest(
    @NotBlank(message = "Resume name cannot be blank")
    String name,
    String title,
    String summary,
    BigDecimal yearsOfExperience,
    String location,
    String contactEmail,
    String contactPhone
) {}
