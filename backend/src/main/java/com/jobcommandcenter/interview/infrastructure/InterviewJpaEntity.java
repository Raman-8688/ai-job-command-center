package com.jobcommandcenter.interview.infrastructure;

import com.jobcommandcenter.interview.domain.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "interviews")
public class InterviewJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "round", nullable = false, length = 50)
    private InterviewRound round;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 50)
    private InterviewFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private InterviewStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 50)
    private InterviewOutcome outcome;

    @Column(name = "scheduled_start_time", nullable = false)
    private Instant scheduledStartTime;

    @Column(name = "scheduled_end_time", nullable = false)
    private Instant scheduledEndTime;

    @Column(name = "time_zone", nullable = false, length = 50)
    private String timeZone;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "interviewer_names", length = 255)
    private String interviewerNames;

    @Column(name = "interviewer_roles", length = 255)
    private String interviewerRoles;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "candidate_feedback", columnDefinition = "TEXT")
    private String candidateFeedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    @OrderBy("occurredAt ASC")
    private List<InterviewEventJpaEntity> events = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    @OrderBy("createdAt ASC")
    private List<InterviewPreparationJpaEntity> preparations = new ArrayList<>();

    public InterviewJpaEntity() {}

    public static InterviewJpaEntity fromDomain(Interview domain) {
        InterviewJpaEntity entity = new InterviewJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.applicationId = domain.getApplicationId();
        entity.jobId = domain.getJobId();
        entity.round = domain.getRound();
        entity.roundNumber = domain.getRoundNumber();
        entity.format = domain.getFormat();
        entity.status = domain.getStatus();
        entity.outcome = domain.getOutcome();
        entity.scheduledStartTime = domain.getScheduledStartTime();
        entity.scheduledEndTime = domain.getScheduledEndTime();
        entity.timeZone = domain.getTimeZone();
        entity.meetingLink = domain.getMeetingLink();
        entity.location = domain.getLocation();
        entity.interviewerNames = domain.getInterviewerNames();
        entity.interviewerRoles = domain.getInterviewerRoles();
        entity.notes = domain.getNotes();
        entity.candidateFeedback = domain.getCandidateFeedback();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        entity.version = domain.getVersion() != null ? domain.getVersion() : 0L;

        if (domain.getEvents() != null) {
            entity.events = domain.getEvents().stream()
                .map(InterviewEventJpaEntity::fromDomain)
                .collect(Collectors.toList());
        }
        if (domain.getPreparations() != null) {
            entity.preparations = domain.getPreparations().stream()
                .map(InterviewPreparationJpaEntity::fromDomain)
                .collect(Collectors.toList());
        }
        return entity;
    }

    public Interview toDomain() {
        return new Interview(
            this.id,
            this.userId,
            this.applicationId,
            this.jobId,
            this.round,
            this.roundNumber,
            this.format,
            this.status,
            this.outcome,
            this.scheduledStartTime,
            this.scheduledEndTime,
            this.timeZone,
            this.meetingLink,
            this.location,
            this.interviewerNames,
            this.interviewerRoles,
            this.notes,
            this.candidateFeedback,
            this.createdAt,
            this.updatedAt,
            this.version,
            this.events != null ? this.events.stream().map(InterviewEventJpaEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>(),
            this.preparations != null ? this.preparations.stream().map(InterviewPreparationJpaEntity::toDomain).collect(Collectors.toList()) : new ArrayList<>()
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public InterviewRound getRound() { return round; }
    public void setRound(InterviewRound round) { this.round = round; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public InterviewFormat getFormat() { return format; }
    public void setFormat(InterviewFormat format) { this.format = format; }
    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }
    public InterviewOutcome getOutcome() { return outcome; }
    public void setOutcome(InterviewOutcome outcome) { this.outcome = outcome; }
    public Instant getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(Instant scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
    public Instant getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(Instant scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getInterviewerNames() { return interviewerNames; }
    public void setInterviewerNames(String interviewerNames) { this.interviewerNames = interviewerNames; }
    public String getInterviewerRoles() { return interviewerRoles; }
    public void setInterviewerRoles(String interviewerRoles) { this.interviewerRoles = interviewerRoles; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCandidateFeedback() { return candidateFeedback; }
    public void setCandidateFeedback(String candidateFeedback) { this.candidateFeedback = candidateFeedback; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public List<InterviewEventJpaEntity> getEvents() { return events; }
    public void setEvents(List<InterviewEventJpaEntity> events) { this.events = events; }
    public List<InterviewPreparationJpaEntity> getPreparations() { return preparations; }
    public void setPreparations(List<InterviewPreparationJpaEntity> preparations) { this.preparations = preparations; }
}
