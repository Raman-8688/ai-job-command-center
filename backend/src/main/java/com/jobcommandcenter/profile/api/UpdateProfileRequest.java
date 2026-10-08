package com.jobcommandcenter.profile.api;

import com.jobcommandcenter.profile.domain.WorkPreference;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProfileRequest(
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    String phone,

    @Size(max = 255, message = "Location must not exceed 255 characters")
    String location,

    @Pattern(regexp = "^(https?://.+)?$", message = "LinkedIn URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
    String linkedInUrl,

    @Pattern(regexp = "^(https?://.+)?$", message = "GitHub URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    String gitHubUrl,

    @Pattern(regexp = "^(https?://.+)?$", message = "Portfolio URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "Portfolio URL must not exceed 500 characters")
    String portfolioUrl,

    List<@Size(max = 150, message = "Target role must not exceed 150 characters") String> targetRoles,

    List<@Size(max = 150, message = "Preferred location must not exceed 150 characters") String> preferredLocations,

    WorkPreference workPreference,

    @DecimalMin(value = "0.0", message = "Years of experience cannot be negative")
    BigDecimal yearsExperience,

    @Min(value = 0, message = "Notice period days cannot be negative")
    Integer noticePeriodDays,

    @Size(max = 255, message = "Current company must not exceed 255 characters")
    String currentCompany,

    @Size(max = 255, message = "Current designation must not exceed 255 characters")
    String currentDesignation
) {}
