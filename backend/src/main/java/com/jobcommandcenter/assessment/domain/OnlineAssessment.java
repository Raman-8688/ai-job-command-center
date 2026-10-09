package com.jobcommandcenter.assessment.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing an online assessment (OA), coding evaluation, or take-home assignment.
 * Enforces lifecycle invariants, immutable audit timeline logging, and study checklist progress.
 */
public class OnlineAssessment {

    private final UUID id;
    private final UUID userId;
    private final UUID jobId;
    private UUID applicationId;
    private UUID interviewId;

    private AssessmentPlatform platform;
    private String title;
    private AssessmentStatus status;
    private AssessmentResult result;

    private Integer durationMinutes;
    private Instant invitedAt;
    private Instant expiresAt;
    private Instant scheduledStartTime;
    private Instant completedAt;

    private BigDecimal score;
    private BigDecimal maxScore;

    private String assessmentUrl;
    private String accessCode;
    private String submissionNotes;
    private String submissionRepoUrl;

    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private final List<OnlineAssessmentEvent> events = new ArrayList<>();
    private final List<AssessmentChecklistItem> checklists = new ArrayList<>();

    public OnlineAssessment(UUID id, UUID userId, UUID jobId, UUID applicationId, UUID interviewId,
                            AssessmentPlatform platform, String title, AssessmentStatus status,
                            AssessmentResult result, Integer durationMinutes, Instant invitedAt,
                            Instant expiresAt, Instant scheduledStartTime, Instant completedAt,
                            BigDecimal score, BigDecimal maxScore, String assessmentUrl,
                            String accessCode, String submissionNotes, String submissionRepoUrl,
                            Instant createdAt, Instant updatedAt, Long version,
                            List<OnlineAssessmentEvent> events, List<AssessmentChecklistItem> checklists) {
        this.id = Objects.requireNonNull(id, "Assessment id must not be null");
        this.userId = Objects.requireNonNull(userId, "User id must not be null");
        this.jobId = Objects.requireNonNull(jobId, "Job id must not be null");
        this.platform = Objects.requireNonNull(platform, "Assessment platform must not be null");
        this.title = (title != null && !title.isBlank()) ? title : "Technical Assessment";
        this.status = (status != null) ? status : AssessmentStatus.INVITED;
        this.result = (result != null) ? result : AssessmentResult.PENDING;
        this.applicationId = applicationId;
        this.interviewId = interviewId;

        this.invitedAt = (invitedAt != null) ? invitedAt : Instant.now();
        validateExpirationWindow(this.invitedAt, expiresAt);
        this.expiresAt = expiresAt;

        validateDuration(durationMinutes);
        this.durationMinutes = durationMinutes;

        this.scheduledStartTime = scheduledStartTime;
        this.completedAt = completedAt;

        validateScores(score, maxScore);
        this.score = score;
        this.maxScore = maxScore;

        this.assessmentUrl = assessmentUrl;
        this.accessCode = accessCode;
        this.submissionNotes = submissionNotes;
        this.submissionRepoUrl = submissionRepoUrl;

        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.createdAt;
        this.version = (version != null) ? version : 0L;

        if (events != null) {
            this.events.addAll(events);
        }
        if (checklists != null) {
            this.checklists.addAll(checklists);
        }
    }

    public static OnlineAssessment create(UUID userId, UUID jobId, UUID applicationId, UUID interviewId,
                                          AssessmentPlatform platform, String title, Integer durationMinutes,
                                          Instant invitedAt, Instant expiresAt, String assessmentUrl,
                                          String accessCode) {
        UUID assessmentId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant effectiveInvitedAt = (invitedAt != null) ? invitedAt : now;

        OnlineAssessment assessment = new OnlineAssessment(
            assessmentId,
            userId,
            jobId,
            applicationId,
            interviewId,
            platform,
            title,
            AssessmentStatus.INVITED,
            AssessmentResult.PENDING,
            durationMinutes,
            effectiveInvitedAt,
            expiresAt,
            null,
            null,
            null,
            null,
            assessmentUrl,
            accessCode,
            null,
            null,
            now,
            now,
            0L,
            new ArrayList<>(),
            new ArrayList<>()
        );

        assessment.addEvent(OnlineAssessmentEvent.create(
            assessmentId,
            null,
            AssessmentStatus.INVITED,
            AssessmentEventType.INVITED,
            "Online assessment recorded and scheduled",
            "USER"
        ));

        return assessment;
    }

    public void start(Instant startTime) {
        if (this.status == AssessmentStatus.EXPIRED) {
            throw new InvalidAssessmentStateException("Cannot start an assessment that has already EXPIRED");
        }
        if (this.status == AssessmentStatus.ABANDONED) {
            throw new InvalidAssessmentStateException("Cannot start an assessment that has been ABANDONED");
        }
        if (this.status == AssessmentStatus.SUBMITTED) {
            throw new InvalidAssessmentStateException("Cannot start an assessment that is already SUBMITTED");
        }

        AssessmentStatus prev = this.status;
        this.status = AssessmentStatus.IN_PROGRESS;
        this.scheduledStartTime = (startTime != null) ? startTime : Instant.now();
        this.updatedAt = Instant.now();

        addEvent(OnlineAssessmentEvent.create(
            this.id,
            prev,
            AssessmentStatus.IN_PROGRESS,
            AssessmentEventType.STARTED,
            "Assessment attempt initiated by candidate",
            "USER"
        ));
    }

    public void submit(BigDecimal score, BigDecimal maxScore, String submissionNotes,
                       String submissionRepoUrl, Instant completionTime) {
        if (this.status == AssessmentStatus.EXPIRED) {
            throw new InvalidAssessmentStateException("Cannot submit an assessment that has already EXPIRED");
        }
        if (this.status == AssessmentStatus.ABANDONED) {
            throw new InvalidAssessmentStateException("Cannot submit an assessment that has been ABANDONED");
        }
        if (this.status == AssessmentStatus.SUBMITTED) {
            throw new InvalidAssessmentStateException("Assessment is already SUBMITTED");
        }

        validateScores(score, maxScore);

        AssessmentStatus prev = this.status;
        this.status = AssessmentStatus.SUBMITTED;
        this.score = score;
        this.maxScore = maxScore;
        this.submissionNotes = submissionNotes;
        this.submissionRepoUrl = submissionRepoUrl;
        this.completedAt = (completionTime != null) ? completionTime : Instant.now();
        this.updatedAt = Instant.now();

        addEvent(OnlineAssessmentEvent.create(
            this.id,
            prev,
            AssessmentStatus.SUBMITTED,
            AssessmentEventType.SUBMITTED,
            "Assessment solution submitted for evaluation",
            "USER"
        ));
    }

    public void recordResult(AssessmentResult newResult, String notes) {
        recordResult(newResult, null, null, notes);
    }

    public void recordResult(AssessmentResult newResult, BigDecimal score, BigDecimal maxScore, String notes) {
        if (this.status == AssessmentStatus.EXPIRED) {
            throw new InvalidAssessmentStateException("Cannot record result for an assessment that is EXPIRED");
        }
        if (this.status == AssessmentStatus.ABANDONED) {
            throw new InvalidAssessmentStateException("Cannot record result for an assessment that is ABANDONED");
        }

        validateScores(score, maxScore);

        AssessmentStatus prev = this.status;
        this.result = Objects.requireNonNull(newResult, "Assessment result must not be null");
        if (score != null) {
            this.score = score;
        }
        if (maxScore != null) {
            this.maxScore = maxScore;
        }

        // If a definitive outcome (PASSED/FAILED) is recorded for an active assessment, transition to SUBMITTED
        if (this.status != AssessmentStatus.SUBMITTED && (newResult == AssessmentResult.PASSED || newResult == AssessmentResult.FAILED)) {
            this.status = AssessmentStatus.SUBMITTED;
            if (this.completedAt == null) {
                this.completedAt = Instant.now();
            }
        }

        this.updatedAt = Instant.now();

        addEvent(OnlineAssessmentEvent.create(
            this.id,
            prev,
            this.status,
            AssessmentEventType.RESULT_RECORDED,
            (notes != null && !notes.isBlank()) ? notes : "Assessment evaluation result recorded: " + newResult,
            "USER"
        ));
    }

    public void extendDeadline(Instant newExpiresAt, String reason) {
        if (this.status == AssessmentStatus.SUBMITTED) {
            throw new InvalidAssessmentStateException("Cannot extend deadline for an assessment that is already SUBMITTED");
        }
        if (this.status == AssessmentStatus.ABANDONED) {
            throw new InvalidAssessmentStateException("Cannot extend deadline for an assessment that has been ABANDONED");
        }

        validateExpirationWindow(this.invitedAt, newExpiresAt);

        AssessmentStatus prev = this.status;
        this.expiresAt = newExpiresAt;

        // If assessment was expired and extended deadline is in the future or removed, reactivate
        if (this.status == AssessmentStatus.EXPIRED && (newExpiresAt == null || newExpiresAt.isAfter(Instant.now()))) {
            this.status = (this.scheduledStartTime != null) ? AssessmentStatus.IN_PROGRESS : AssessmentStatus.INVITED;
        }

        this.updatedAt = Instant.now();

        addEvent(OnlineAssessmentEvent.create(
            this.id,
            prev,
            this.status,
            AssessmentEventType.DEADLINE_EXTENDED,
            (reason != null && !reason.isBlank()) ? reason : "Assessment deadline extended to " + newExpiresAt,
            "USER"
        ));
    }

    public boolean isExpiredAt(Instant currentTime) {
        Instant checkTime = (currentTime != null) ? currentTime : Instant.now();
        return this.expiresAt != null
            && this.expiresAt.isBefore(checkTime)
            && this.status != AssessmentStatus.SUBMITTED
            && this.status != AssessmentStatus.ABANDONED;
    }

    public void expire(String reason) {
        if (this.status == AssessmentStatus.SUBMITTED) {
            throw new InvalidAssessmentStateException("Cannot expire an assessment that is already SUBMITTED");
        }

        AssessmentStatus prev = this.status;
        this.status = AssessmentStatus.EXPIRED;
        this.updatedAt = Instant.now();

        addEvent(OnlineAssessmentEvent.create(
            this.id,
            prev,
            AssessmentStatus.EXPIRED,
            AssessmentEventType.EXPIRED,
            (reason != null && !reason.isBlank()) ? reason : "Assessment deadline elapsed without submission",
            "SYSTEM"
        ));
    }

    public void abandon(String reason) {
        if (this.status == AssessmentStatus.SUBMITTED) {
            throw new InvalidAssessmentStateException("Cannot abandon an assessment that is already SUBMITTED");
        }

        AssessmentStatus prev = this.status;
        this.status = AssessmentStatus.ABANDONED;
        this.updatedAt = Instant.now();

        addEvent(OnlineAssessmentEvent.create(
            this.id,
            prev,
            AssessmentStatus.ABANDONED,
            AssessmentEventType.ABANDONED,
            (reason != null && !reason.isBlank()) ? reason : "Assessment abandoned by candidate",
            "USER"
        ));
    }

    public void updateDetails(AssessmentPlatform platform, String title, Integer durationMinutes,
                              Instant expiresAt, String assessmentUrl, String accessCode) {
        if (this.status == AssessmentStatus.SUBMITTED) {
            throw new InvalidAssessmentStateException("Cannot modify details of an assessment that is already SUBMITTED");
        }

        validateDuration(durationMinutes);
        validateExpirationWindow(this.invitedAt, expiresAt);

        if (platform != null) this.platform = platform;
        if (title != null && !title.isBlank()) this.title = title;
        this.durationMinutes = durationMinutes;
        this.expiresAt = expiresAt;
        this.assessmentUrl = assessmentUrl;
        this.accessCode = accessCode;
        this.updatedAt = Instant.now();

        addEvent(OnlineAssessmentEvent.create(
            this.id,
            this.status,
            this.status,
            AssessmentEventType.NOTE_ADDED,
            "Assessment configuration and metadata updated",
            "USER"
        ));
    }

    public void addChecklistItems(List<AssessmentChecklistItem> items) {
        if (items != null) {
            this.checklists.addAll(items);
            this.updatedAt = Instant.now();
        }
    }

    public void addChecklistItem(AssessmentChecklistItem item) {
        if (item != null) {
            this.checklists.add(item);
            this.updatedAt = Instant.now();
        }
    }

    public boolean toggleChecklistItem(UUID itemId) {
        for (AssessmentChecklistItem item : checklists) {
            if (item.getId().equals(itemId)) {
                item.toggleCompleted();
                this.updatedAt = Instant.now();
                return true;
            }
        }
        return false;
    }

    public boolean setChecklistItemCompleted(UUID itemId, boolean completed) {
        for (AssessmentChecklistItem item : checklists) {
            if (item.getId().equals(itemId)) {
                item.setCompleted(completed);
                this.updatedAt = Instant.now();
                return true;
            }
        }
        return false;
    }

    public void replaceChecklistItems(List<AssessmentChecklistItem> newItems) {
        this.checklists.clear();
        if (newItems != null) {
            this.checklists.addAll(newItems);
        }
        this.updatedAt = Instant.now();
    }

    public int getChecklistCompletionPercentage() {
        if (checklists.isEmpty()) return 0;
        long completed = checklists.stream().filter(AssessmentChecklistItem::isCompleted).count();
        return (int) Math.round(((double) completed / checklists.size()) * 100.0);
    }

    public void recordEvent(AssessmentEventType type, String notes, String source) {
        addEvent(OnlineAssessmentEvent.create(
            this.id,
            this.status,
            this.status,
            type,
            notes,
            source != null ? source : "USER"
        ));
        this.updatedAt = Instant.now();
    }

    private void addEvent(OnlineAssessmentEvent event) {
        this.events.add(event);
    }

    private static void validateExpirationWindow(Instant invitedAt, Instant expiresAt) {
        if (invitedAt != null && expiresAt != null && expiresAt.isBefore(invitedAt)) {
            throw new InvalidAssessmentStateException("Assessment expiration timestamp must be on or after invitation timestamp");
        }
    }

    private static void validateDuration(Integer durationMinutes) {
        if (durationMinutes != null && durationMinutes <= 0) {
            throw new InvalidAssessmentStateException("Assessment duration must be greater than zero minutes");
        }
    }

    private static void validateScores(BigDecimal score, BigDecimal maxScore) {
        if (score != null && score.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAssessmentStateException("Assessment score cannot be negative");
        }
        if (maxScore != null && maxScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAssessmentStateException("Maximum score must be greater than zero");
        }
        if (score != null && maxScore != null && score.compareTo(maxScore) > 0) {
            throw new InvalidAssessmentStateException("Assessment score cannot exceed maximum score");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getJobId() { return jobId; }
    public UUID getApplicationId() { return applicationId; }
    public UUID getInterviewId() { return interviewId; }
    public AssessmentPlatform getPlatform() { return platform; }
    public String getTitle() { return title; }
    public AssessmentStatus getStatus() { return status; }
    public AssessmentResult getResult() { return result; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Instant getInvitedAt() { return invitedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getScheduledStartTime() { return scheduledStartTime; }
    public Instant getCompletedAt() { return completedAt; }
    public BigDecimal getScore() { return score; }
    public BigDecimal getMaxScore() { return maxScore; }
    public String getAssessmentUrl() { return assessmentUrl; }
    public String getAccessCode() { return accessCode; }
    public String getSubmissionNotes() { return submissionNotes; }
    public String getSubmissionRepoUrl() { return submissionRepoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public List<OnlineAssessmentEvent> getEvents() { return Collections.unmodifiableList(events); }
    public List<AssessmentChecklistItem> getChecklists() { return Collections.unmodifiableList(checklists); }
}
