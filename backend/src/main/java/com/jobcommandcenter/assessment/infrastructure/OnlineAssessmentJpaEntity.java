package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import com.jobcommandcenter.assessment.domain.AssessmentResult;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.assessment.domain.OnlineAssessment;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "online_assessments")
public class OnlineAssessmentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "interview_id")
    private UUID interviewId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 50)
    private AssessmentPlatform platform;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AssessmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 50)
    private AssessmentResult result;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "invited_at", nullable = false)
    private Instant invitedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "scheduled_start_time")
    private Instant scheduledStartTime;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "score", precision = 6, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 6, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "assessment_url", length = 500)
    private String assessmentUrl;

    @Column(name = "access_code", length = 100)
    private String accessCode;

    @Column(name = "submission_notes", columnDefinition = "TEXT")
    private String submissionNotes;

    @Column(name = "submission_repo_url", length = 500)
    private String submissionRepoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    @OrderBy("occurredAt ASC")
    private List<OnlineAssessmentEventJpaEntity> events = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    @OrderBy("sortOrder ASC")
    private List<AssessmentChecklistJpaEntity> checklists = new ArrayList<>();

    public OnlineAssessmentJpaEntity() {}

    public static OnlineAssessmentJpaEntity fromDomain(OnlineAssessment domain) {
        OnlineAssessmentJpaEntity entity = new OnlineAssessmentJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.jobId = domain.getJobId();
        entity.applicationId = domain.getApplicationId();
        entity.interviewId = domain.getInterviewId();
        entity.platform = domain.getPlatform();
        entity.title = domain.getTitle();
        entity.status = domain.getStatus();
        entity.result = domain.getResult();
        entity.durationMinutes = domain.getDurationMinutes();
        entity.invitedAt = domain.getInvitedAt();
        entity.expiresAt = domain.getExpiresAt();
        entity.scheduledStartTime = domain.getScheduledStartTime();
        entity.completedAt = domain.getCompletedAt();
        entity.score = domain.getScore();
        entity.maxScore = domain.getMaxScore();
        entity.assessmentUrl = domain.getAssessmentUrl();
        entity.accessCode = domain.getAccessCode();
        entity.submissionNotes = domain.getSubmissionNotes();
        entity.submissionRepoUrl = domain.getSubmissionRepoUrl();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        entity.version = domain.getVersion() != null ? domain.getVersion() : 0L;

        if (domain.getEvents() != null) {
            entity.events = domain.getEvents().stream()
                .map(OnlineAssessmentEventJpaEntity::fromDomain)
                .collect(Collectors.toList());
        }
        if (domain.getChecklists() != null) {
            entity.checklists = domain.getChecklists().stream()
                .map(AssessmentChecklistJpaEntity::fromDomain)
                .collect(Collectors.toList());
        }
        return entity;
    }

    public OnlineAssessment toDomain() {
        return new OnlineAssessment(
            this.id,
            this.userId,
            this.jobId,
            this.applicationId,
            this.interviewId,
            this.platform,
            this.title,
            this.status,
            this.result,
            this.durationMinutes,
            this.invitedAt,
            this.expiresAt,
            this.scheduledStartTime,
            this.completedAt,
            this.score,
            this.maxScore,
            this.assessmentUrl,
            this.accessCode,
            this.submissionNotes,
            this.submissionRepoUrl,
            this.createdAt,
            this.updatedAt,
            this.version,
            this.events != null ? this.events.stream().map(OnlineAssessmentEventJpaEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>(),
            this.checklists != null ? this.checklists.stream().map(AssessmentChecklistJpaEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>()
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getInterviewId() { return interviewId; }
    public void setInterviewId(UUID interviewId) { this.interviewId = interviewId; }
    public AssessmentPlatform getPlatform() { return platform; }
    public void setPlatform(AssessmentPlatform platform) { this.platform = platform; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public AssessmentStatus getStatus() { return status; }
    public void setStatus(AssessmentStatus status) { this.status = status; }
    public AssessmentResult getResult() { return result; }
    public void setResult(AssessmentResult result) { this.result = result; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Instant getInvitedAt() { return invitedAt; }
    public void setInvitedAt(Instant invitedAt) { this.invitedAt = invitedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(Instant scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }
    public String getAssessmentUrl() { return assessmentUrl; }
    public void setAssessmentUrl(String assessmentUrl) { this.assessmentUrl = assessmentUrl; }
    public String getAccessCode() { return accessCode; }
    public void setAccessCode(String accessCode) { this.accessCode = accessCode; }
    public String getSubmissionNotes() { return submissionNotes; }
    public void setSubmissionNotes(String submissionNotes) { this.submissionNotes = submissionNotes; }
    public String getSubmissionRepoUrl() { return submissionRepoUrl; }
    public void setSubmissionRepoUrl(String submissionRepoUrl) { this.submissionRepoUrl = submissionRepoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public List<OnlineAssessmentEventJpaEntity> getEvents() { return events; }
    public void setEvents(List<OnlineAssessmentEventJpaEntity> events) { this.events = events; }
    public List<AssessmentChecklistJpaEntity> getChecklists() { return checklists; }
    public void setChecklists(List<AssessmentChecklistJpaEntity> checklists) { this.checklists = checklists; }
}
