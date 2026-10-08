package com.jobcommandcenter.job.domain;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Domain entity representing a canonical job opportunity posting.
 * The original description is preserved as immutable source truth.
 */
public class Job {

    private final UUID id;
    private String externalJobId;
    private String title;
    private String companyName;
    private String companyWebsite;
    private String jobUrl;
    private String description;
    private String location;
    private WorkMode workMode;
    private EmploymentType employmentType;
    private BigDecimal experienceMinYears;
    private BigDecimal experienceMaxYears;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private JobSource source;
    private String sourceUrl;
    private Instant postedAt;
    private final Instant discoveredAt;
    private Instant applicationDeadline;
    private JobStatus status;
    private String deduplicationHash;
    private final Instant createdAt;
    private Instant updatedAt;
    private List<JobSkill> jobSkills;

    public Job(UUID id,
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
               List<JobSkill> jobSkills) {
        this.id = Objects.requireNonNull(id, "Job ID cannot be null");
        this.externalJobId = externalJobId;
        this.title = validateNonBlank(title, "Job title cannot be blank");
        this.companyName = validateNonBlank(companyName, "Company name cannot be blank");
        this.companyWebsite = companyWebsite;
        this.jobUrl = jobUrl;
        this.description = validateNonBlank(description, "Job description cannot be blank");
        this.location = location;
        this.workMode = workMode != null ? workMode : WorkMode.UNKNOWN;
        this.employmentType = employmentType != null ? employmentType : EmploymentType.FULL_TIME;
        this.experienceMinYears = experienceMinYears;
        this.experienceMaxYears = experienceMaxYears;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.source = source != null ? source : JobSource.MANUAL;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.discoveredAt = discoveredAt != null ? discoveredAt : Instant.now();
        this.applicationDeadline = applicationDeadline;
        this.status = status != null ? status : JobStatus.ACTIVE;
        this.deduplicationHash = deduplicationHash != null ? deduplicationHash : calculateDeduplicationHash(companyName, title, location);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.jobSkills = jobSkills != null ? new ArrayList<>(jobSkills) : new ArrayList<>();
    }

    public static Job createNew(String externalJobId,
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
                                Instant applicationDeadline) {
        Instant now = Instant.now();
        String hash = calculateDeduplicationHash(companyName, title, location);
        return new Job(
            UUID.randomUUID(),
            externalJobId,
            title,
            companyName,
            companyWebsite,
            jobUrl,
            description,
            location,
            workMode,
            employmentType,
            experienceMinYears,
            experienceMaxYears,
            salaryMin,
            salaryMax,
            salaryCurrency,
            source,
            sourceUrl,
            postedAt,
            now,
            applicationDeadline,
            JobStatus.ACTIVE,
            hash,
            now,
            now,
            new ArrayList<>()
        );
    }

    public static String calculateDeduplicationHash(String companyName, String title, String location) {
        String normCompany = companyName != null ? companyName.trim().toLowerCase() : "";
        String normTitle = title != null ? title.trim().toLowerCase() : "";
        String normLocation = location != null ? location.trim().toLowerCase() : "";
        String rawKey = normCompany + "|" + normTitle + "|" + normLocation;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public void updateDetails(String externalJobId,
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
                              Instant applicationDeadline,
                              JobStatus status) {
        this.externalJobId = externalJobId;
        this.title = validateNonBlank(title, "Job title cannot be blank");
        this.companyName = validateNonBlank(companyName, "Company name cannot be blank");
        this.companyWebsite = companyWebsite;
        this.jobUrl = jobUrl;
        this.description = validateNonBlank(description, "Job description cannot be blank");
        this.location = location;
        this.workMode = workMode != null ? workMode : this.workMode;
        this.employmentType = employmentType != null ? employmentType : this.employmentType;
        this.experienceMinYears = experienceMinYears;
        this.experienceMaxYears = experienceMaxYears;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.source = source != null ? source : this.source;
        this.sourceUrl = sourceUrl;
        this.postedAt = postedAt;
        this.applicationDeadline = applicationDeadline;
        if (status != null) {
            this.status = status;
        }
        this.deduplicationHash = calculateDeduplicationHash(companyName, title, location);
        this.updatedAt = Instant.now();
    }

    public void changeStatus(JobStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Job status cannot be null");
        this.updatedAt = Instant.now();
    }

    public void setJobSkills(List<JobSkill> newSkills) {
        this.jobSkills = newSkills != null ? new ArrayList<>(newSkills) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    private static String validateNonBlank(String value, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    // Getters
    public UUID getId() { return id; }
    public String getExternalJobId() { return externalJobId; }
    public String getTitle() { return title; }
    public String getCompanyName() { return companyName; }
    public String getCompanyWebsite() { return companyWebsite; }
    public String getJobUrl() { return jobUrl; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public WorkMode getWorkMode() { return workMode; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public BigDecimal getExperienceMinYears() { return experienceMinYears; }
    public BigDecimal getExperienceMaxYears() { return experienceMaxYears; }
    public BigDecimal getSalaryMin() { return salaryMin; }
    public BigDecimal getSalaryMax() { return salaryMax; }
    public String getSalaryCurrency() { return salaryCurrency; }
    public JobSource getSource() { return source; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getPostedAt() { return postedAt; }
    public Instant getDiscoveredAt() { return discoveredAt; }
    public Instant getApplicationDeadline() { return applicationDeadline; }
    public JobStatus getStatus() { return status; }
    public String getDeduplicationHash() { return deduplicationHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<JobSkill> getJobSkills() { return Collections.unmodifiableList(jobSkills); }
}
