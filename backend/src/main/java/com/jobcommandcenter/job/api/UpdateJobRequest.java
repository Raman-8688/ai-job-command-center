package com.jobcommandcenter.job.api;

import com.jobcommandcenter.job.domain.EmploymentType;
import com.jobcommandcenter.job.domain.JobSource;
import com.jobcommandcenter.job.domain.JobStatus;
import com.jobcommandcenter.job.domain.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UpdateJobRequest(
    String externalJobId,

    @NotBlank(message = "Job title cannot be blank")
    @Size(max = 255, message = "Job title must not exceed 255 characters")
    String title,

    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    String companyName,

    @Size(max = 500, message = "Company website URL must not exceed 500 characters")
    String companyWebsite,

    @Size(max = 1000, message = "Job URL must not exceed 1000 characters")
    String jobUrl,

    @NotBlank(message = "Job description cannot be blank")
    String description,

    @Size(max = 255, message = "Location must not exceed 255 characters")
    String location,

    WorkMode workMode,

    EmploymentType employmentType,

    @DecimalMin(value = "0.0", message = "Minimum experience must be non-negative")
    BigDecimal experienceMinYears,

    @DecimalMin(value = "0.0", message = "Maximum experience must be non-negative")
    BigDecimal experienceMaxYears,

    @DecimalMin(value = "0.0", message = "Minimum salary must be non-negative")
    BigDecimal salaryMin,

    @DecimalMin(value = "0.0", message = "Maximum salary must be non-negative")
    BigDecimal salaryMax,

    @Size(max = 10, message = "Salary currency must not exceed 10 characters")
    String salaryCurrency,

    JobSource source,

    @Size(max = 1000, message = "Source URL must not exceed 1000 characters")
    String sourceUrl,

    Instant postedAt,

    Instant applicationDeadline,

    JobStatus status,

    List<UUID> requiredSkillIds,

    List<UUID> preferredSkillIds
) {}
