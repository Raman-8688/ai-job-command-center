package com.jobcommandcenter.application.infrastructure;

import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.JobApplication;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA entity mapping to job_applications table.
 */
@Entity
@Table(name = "job_applications")
public class JobApplicationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "resume_id")
    private UUID resumeId;

    @Column(name = "tailored_resume_id")
    private UUID tailoredResumeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_source", nullable = false, length = 50)
    private ApplicationSource submissionSource;

    @Column(name = "external_reference", length = 150)
    private String externalReference;

    @Column(name = "next_follow_up_date")
    private Instant nextFollowUpDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @OrderBy("occurredAt ASC")
    private List<JobApplicationEventJpaEntity> events = new ArrayList<>();

    public JobApplicationJpaEntity() {}

    public static JobApplicationJpaEntity fromDomain(JobApplication domain) {
        JobApplicationJpaEntity entity = new JobApplicationJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.jobId = domain.getJobId();
        entity.resumeId = domain.getResumeId();
        entity.tailoredResumeId = domain.getTailoredResumeId();
        entity.status = domain.getStatus();
        entity.appliedAt = domain.getAppliedAt();
        entity.submissionSource = domain.getSubmissionSource();
        entity.externalReference = domain.getExternalReference();
        entity.nextFollowUpDate = domain.getNextFollowUpDate();
        entity.notes = domain.getNotes();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        entity.version = domain.getVersion();

        if (domain.getEvents() != null) {
            entity.events = domain.getEvents().stream()
                .map(JobApplicationEventJpaEntity::fromDomain)
                .collect(Collectors.toList());
        }
        return entity;
    }

    public JobApplication toDomain() {
        return new JobApplication(
            this.id,
            this.userId,
            this.jobId,
            this.resumeId,
            this.tailoredResumeId,
            this.status,
            this.appliedAt,
            this.submissionSource,
            this.externalReference,
            this.nextFollowUpDate,
            this.notes,
            this.createdAt,
            this.updatedAt,
            this.version,
            this.events != null ? this.events.stream().map(JobApplicationEventJpaEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>()
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }
    public UUID getTailoredResumeId() { return tailoredResumeId; }
    public void setTailoredResumeId(UUID tailoredResumeId) { this.tailoredResumeId = tailoredResumeId; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }
    public ApplicationSource getSubmissionSource() { return submissionSource; }
    public void setSubmissionSource(ApplicationSource submissionSource) { this.submissionSource = submissionSource; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public Instant getNextFollowUpDate() { return nextFollowUpDate; }
    public void setNextFollowUpDate(Instant nextFollowUpDate) { this.nextFollowUpDate = nextFollowUpDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public List<JobApplicationEventJpaEntity> getEvents() { return events; }
    public void setEvents(List<JobApplicationEventJpaEntity> events) { this.events = events; }
}
