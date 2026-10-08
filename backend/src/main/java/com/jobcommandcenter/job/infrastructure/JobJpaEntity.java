package com.jobcommandcenter.job.infrastructure;

import com.jobcommandcenter.job.domain.EmploymentType;
import com.jobcommandcenter.job.domain.JobSource;
import com.jobcommandcenter.job.domain.JobStatus;
import com.jobcommandcenter.job.domain.WorkMode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class JobJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "external_job_id", length = 150)
    private String externalJobId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_website", length = 500)
    private String companyWebsite;

    @Column(name = "job_url", length = 1000)
    private String jobUrl;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 50)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 50)
    private EmploymentType employmentType;

    @Column(name = "experience_min_years", precision = 4, scale = 1)
    private BigDecimal experienceMinYears;

    @Column(name = "experience_max_years", precision = 4, scale = 1)
    private BigDecimal experienceMaxYears;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency", length = 10)
    private String salaryCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private JobSource source;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "discovered_at", nullable = false, updatable = false)
    private Instant discoveredAt;

    @Column(name = "application_deadline")
    private Instant applicationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private JobStatus status;

    @Column(name = "deduplication_hash", nullable = false, length = 64)
    private String deduplicationHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private List<JobSkillJpaEntity> skills = new ArrayList<>();

    public JobJpaEntity() {}

    public JobJpaEntity(UUID id,
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
                        List<JobSkillJpaEntity> skills) {
        this.id = id;
        this.externalJobId = externalJobId;
        this.title = title;
        this.companyName = companyName;
        this.companyWebsite = companyWebsite;
        this.jobUrl = jobUrl;
        this.description = description;
        this.location = location;
        this.workMode = workMode;
        this.employmentType = employmentType;
        this.experienceMinYears = experienceMinYears;
        this.experienceMaxYears = experienceMaxYears;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.discoveredAt = discoveredAt;
        this.applicationDeadline = applicationDeadline;
        this.status = status;
        this.deduplicationHash = deduplicationHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getExternalJobId() { return externalJobId; }
    public void setExternalJobId(String externalJobId) { this.externalJobId = externalJobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }

    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public BigDecimal getExperienceMinYears() { return experienceMinYears; }
    public void setExperienceMinYears(BigDecimal experienceMinYears) { this.experienceMinYears = experienceMinYears; }

    public BigDecimal getExperienceMaxYears() { return experienceMaxYears; }
    public void setExperienceMaxYears(BigDecimal experienceMaxYears) { this.experienceMaxYears = experienceMaxYears; }

    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }

    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }

    public String getSalaryCurrency() { return salaryCurrency; }
    public void setSalaryCurrency(String salaryCurrency) { this.salaryCurrency = salaryCurrency; }

    public JobSource getSource() { return source; }
    public void setSource(JobSource source) { this.source = source; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public Instant getPostedAt() { return postedAt; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }

    public Instant getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Instant discoveredAt) { this.discoveredAt = discoveredAt; }

    public Instant getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(Instant applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public String getDeduplicationHash() { return deduplicationHash; }
    public void setDeduplicationHash(String deduplicationHash) { this.deduplicationHash = deduplicationHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<JobSkillJpaEntity> getSkills() { return skills; }
    public void setSkills(List<JobSkillJpaEntity> skills) { this.skills = skills; }
}
