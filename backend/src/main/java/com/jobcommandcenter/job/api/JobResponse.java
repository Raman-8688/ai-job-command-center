package com.jobcommandcenter.job.api;

import com.jobcommandcenter.job.domain.EmploymentType;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobSource;
import com.jobcommandcenter.job.domain.JobStatus;
import com.jobcommandcenter.job.domain.WorkMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobResponse(
    UUID id,
    String externalJobId,
    String title,
    String companyName,
    String companyWebsite,
    String jobUrl,
    String description,
    String location,
    WorkMode workMode,
    EmploymentType employmentType,
    BigDecimal experienceMinYears,
    BigDecimal experienceMaxYears,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    String salaryCurrency,
    JobSource source,
    String sourceUrl,
    Instant postedAt,
    Instant discoveredAt,
    Instant applicationDeadline,
    JobStatus status,
    String deduplicationHash,
    Instant createdAt,
    Instant updatedAt,
    List<JobSkillDto> skills
) {
    public static JobResponse of(Job job, List<JobSkillDto> skills) {
        return new JobResponse(
            job.getId(),
            job.getExternalJobId(),
            job.getTitle(),
            job.getCompanyName(),
            job.getCompanyWebsite(),
            job.getJobUrl(),
            job.getDescription(),
            job.getLocation(),
            job.getWorkMode(),
            job.getEmploymentType(),
            job.getExperienceMinYears(),
            job.getExperienceMaxYears(),
            job.getSalaryMin(),
            job.getSalaryMax(),
            job.getSalaryCurrency(),
            job.getSource(),
            job.getSourceUrl(),
            job.getPostedAt(),
            job.getDiscoveredAt(),
            job.getApplicationDeadline(),
            job.getStatus(),
            job.getDeduplicationHash(),
            job.getCreatedAt(),
            job.getUpdatedAt(),
            skills != null ? skills : List.of()
        );
    }
}
