package com.jobcommandcenter.job.api;

import com.jobcommandcenter.job.domain.EmploymentType;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobSource;
import com.jobcommandcenter.job.domain.JobStatus;
import com.jobcommandcenter.job.domain.WorkMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record JobSummaryResponse(
    UUID id,
    String externalJobId,
    String title,
    String companyName,
    String location,
    WorkMode workMode,
    EmploymentType employmentType,
    BigDecimal experienceMinYears,
    BigDecimal experienceMaxYears,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    String salaryCurrency,
    JobSource source,
    JobStatus status,
    Instant postedAt,
    int requiredSkillsCount,
    int preferredSkillsCount,
    Instant createdAt
) {
    public static JobSummaryResponse fromDomain(Job job) {
        int reqCount = (int) job.getJobSkills().stream().filter(s -> s.isRequired()).count();
        int prefCount = (int) job.getJobSkills().stream().filter(s -> s.isPreferred()).count();

        return new JobSummaryResponse(
            job.getId(),
            job.getExternalJobId(),
            job.getTitle(),
            job.getCompanyName(),
            job.getLocation(),
            job.getWorkMode(),
            job.getEmploymentType(),
            job.getExperienceMinYears(),
            job.getExperienceMaxYears(),
            job.getSalaryMin(),
            job.getSalaryMax(),
            job.getSalaryCurrency(),
            job.getSource(),
            job.getStatus(),
            job.getPostedAt(),
            reqCount,
            prefCount,
            job.getCreatedAt()
        );
    }
}
