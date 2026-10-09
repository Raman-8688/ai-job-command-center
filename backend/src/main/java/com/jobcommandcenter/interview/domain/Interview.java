package com.jobcommandcenter.interview.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing an interview session, its lifecycle transitions,
 * audit event history, and preparation questions.
 */
public class Interview {

    private final UUID id;
    private final UUID userId;
    private UUID applicationId;
    private final UUID jobId;

    private InterviewRound round;
    private int roundNumber;
    private InterviewFormat format;
    private InterviewStatus status;
    private InterviewOutcome outcome;

    private Instant scheduledStartTime;
    private Instant scheduledEndTime;
    private String timeZone;
    private String meetingLink;
    private String location;
    private String interviewerNames;
    private String interviewerRoles;
    private String notes;
    private String candidateFeedback;

    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private final List<InterviewEvent> events = new ArrayList<>();
    private final List<InterviewPreparation> preparations = new ArrayList<>();

    public Interview(UUID id, UUID userId, UUID applicationId, UUID jobId,
                     InterviewRound round, int roundNumber, InterviewFormat format,
                     InterviewStatus status, InterviewOutcome outcome,
                     Instant scheduledStartTime, Instant scheduledEndTime, String timeZone,
                     String meetingLink, String location, String interviewerNames,
                     String interviewerRoles, String notes, String candidateFeedback,
                     Instant createdAt, Instant updatedAt, Long version,
                     List<InterviewEvent> events, List<InterviewPreparation> preparations) {
        this.id = Objects.requireNonNull(id, "Interview id must not be null");
        this.userId = Objects.requireNonNull(userId, "User id must not be null");
        this.jobId = Objects.requireNonNull(jobId, "Job id must not be null");
        this.round = Objects.requireNonNull(round, "Interview round must not be null");
        this.format = Objects.requireNonNull(format, "Interview format must not be null");
        this.status = Objects.requireNonNull(status, "Interview status must not be null");
        this.outcome = (outcome != null) ? outcome : InterviewOutcome.PENDING;
        this.applicationId = applicationId;
        this.roundNumber = Math.max(1, roundNumber);

        validateTimeWindow(scheduledStartTime, scheduledEndTime);
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;

        this.timeZone = (timeZone != null && !timeZone.isBlank()) ? timeZone : "UTC";
        this.meetingLink = meetingLink;
        this.location = location;
        this.interviewerNames = interviewerNames;
        this.interviewerRoles = interviewerRoles;
        this.notes = notes;
        this.candidateFeedback = candidateFeedback;

        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.createdAt;
        this.version = (version != null) ? version : 0L;

        if (events != null) {
            this.events.addAll(events);
        }
        if (preparations != null) {
            this.preparations.addAll(preparations);
        }
    }

    public static Interview create(UUID userId, UUID applicationId, UUID jobId,
                                  InterviewRound round, int roundNumber, InterviewFormat format,
                                  Instant scheduledStartTime, Instant scheduledEndTime, String timeZone,
                                  String meetingLink, String location, String interviewerNames,
                                  String interviewerRoles, String notes) {
        UUID interviewId = UUID.randomUUID();
        Instant now = Instant.now();

        Interview interview = new Interview(
            interviewId,
            userId,
            applicationId,
            jobId,
            round,
            roundNumber,
            format,
            InterviewStatus.SCHEDULED,
            InterviewOutcome.PENDING,
            scheduledStartTime,
            scheduledEndTime,
            timeZone,
            meetingLink,
            location,
            interviewerNames,
            interviewerRoles,
            notes,
            null,
            now,
            now,
            0L,
            null,
            null
        );

        interview.events.add(InterviewEvent.create(
            interviewId,
            null,
            InterviewStatus.SCHEDULED,
            InterviewEventType.SCHEDULED,
            "Interview scheduled for " + round + " (Round " + roundNumber + ")",
            "USER"
        ));

        return interview;
    }

    public void reschedule(Instant newStart, Instant newEnd, String newTimeZone, String reason, String source) {
        if (this.status == InterviewStatus.COMPLETED || this.status == InterviewStatus.CANCELLED) {
            throw new InvalidInterviewStateException("Cannot reschedule an interview that is already " + this.status);
        }
        validateTimeWindow(newStart, newEnd);

        InterviewStatus prevStatus = this.status;
        this.scheduledStartTime = newStart;
        this.scheduledEndTime = newEnd;
        if (newTimeZone != null && !newTimeZone.isBlank()) {
            this.timeZone = newTimeZone;
        }
        this.status = InterviewStatus.RESCHEDULED;
        this.updatedAt = Instant.now();

        this.events.add(InterviewEvent.create(
            this.id,
            prevStatus,
            InterviewStatus.RESCHEDULED,
            InterviewEventType.RESCHEDULED,
            reason != null ? reason : "Rescheduled to " + newStart,
            source
        ));
    }

    public void complete(String feedback, InterviewOutcome outcome, String notes, String source) {
        if (this.status == InterviewStatus.CANCELLED) {
            throw new InvalidInterviewStateException("Cannot complete a cancelled interview");
        }
        InterviewStatus prev = this.status;
        this.status = InterviewStatus.COMPLETED;
        if (outcome != null) {
            this.outcome = outcome;
        }
        if (feedback != null && !feedback.isBlank()) {
            this.candidateFeedback = feedback;
        }
        if (notes != null && !notes.isBlank()) {
            this.notes = notes;
        }
        this.updatedAt = Instant.now();

        this.events.add(InterviewEvent.create(
            this.id,
            prev,
            InterviewStatus.COMPLETED,
            InterviewEventType.COMPLETED,
            "Completed interview with outcome: " + this.outcome,
            source
        ));
    }

    public void cancel(String reason, String source) {
        if (this.status == InterviewStatus.COMPLETED) {
            throw new InvalidInterviewStateException("Cannot cancel an already completed interview");
        }
        InterviewStatus prev = this.status;
        this.status = InterviewStatus.CANCELLED;
        this.updatedAt = Instant.now();

        this.events.add(InterviewEvent.create(
            this.id,
            prev,
            InterviewStatus.CANCELLED,
            InterviewEventType.CANCELLED,
            reason != null ? reason : "Interview cancelled",
            source
        ));
    }

    public void markNoShow(String reason, String source) {
        if (this.status == InterviewStatus.COMPLETED) {
            throw new InvalidInterviewStateException("Cannot mark a completed interview as no-show");
        }
        InterviewStatus prev = this.status;
        this.status = InterviewStatus.NO_SHOW;
        this.updatedAt = Instant.now();

        this.events.add(InterviewEvent.create(
            this.id,
            prev,
            InterviewStatus.NO_SHOW,
            InterviewEventType.STATUS_CHANGED,
            reason != null ? reason : "Marked as no show",
            source
        ));
    }

    public void updateOutcome(InterviewOutcome newOutcome, String notes, String source) {
        this.outcome = Objects.requireNonNull(newOutcome, "Outcome must not be null");
        this.updatedAt = Instant.now();

        this.events.add(InterviewEvent.create(
            this.id,
            this.status,
            this.status,
            InterviewEventType.OUTCOME_UPDATED,
            notes != null ? notes : "Outcome updated to " + newOutcome,
            source
        ));
    }

    public void updateDetails(InterviewRound round, Integer roundNumber, InterviewFormat format,
                              String meetingLink, String location, String interviewerNames,
                              String interviewerRoles, String notes) {
        if (round != null) this.round = round;
        if (roundNumber != null && roundNumber > 0) this.roundNumber = roundNumber;
        if (format != null) this.format = format;
        if (meetingLink != null) this.meetingLink = meetingLink;
        if (location != null) this.location = location;
        if (interviewerNames != null) this.interviewerNames = interviewerNames;
        if (interviewerRoles != null) this.interviewerRoles = interviewerRoles;
        if (notes != null) this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public void addPreparation(InterviewPreparation preparation) {
        this.preparations.add(Objects.requireNonNull(preparation, "Preparation must not be null"));
        this.updatedAt = Instant.now();
    }

    public void addPreparations(List<InterviewPreparation> preps) {
        if (preps != null) {
            this.preparations.addAll(preps);
            this.updatedAt = Instant.now();
        }
    }

    private static void validateTimeWindow(Instant start, Instant end) {
        Objects.requireNonNull(start, "Scheduled start time must not be null");
        Objects.requireNonNull(end, "Scheduled end time must not be null");
        if (!end.isAfter(start)) {
            throw new InvalidInterviewStateException("Scheduled end time must be after scheduled start time");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getApplicationId() { return applicationId; }
    public UUID getJobId() { return jobId; }
    public InterviewRound getRound() { return round; }
    public int getRoundNumber() { return roundNumber; }
    public InterviewFormat getFormat() { return format; }
    public InterviewStatus getStatus() { return status; }
    public InterviewOutcome getOutcome() { return outcome; }
    public Instant getScheduledStartTime() { return scheduledStartTime; }
    public Instant getScheduledEndTime() { return scheduledEndTime; }
    public String getTimeZone() { return timeZone; }
    public String getMeetingLink() { return meetingLink; }
    public String getLocation() { return location; }
    public String getInterviewerNames() { return interviewerNames; }
    public String getInterviewerRoles() { return interviewerRoles; }
    public String getNotes() { return notes; }
    public String getCandidateFeedback() { return candidateFeedback; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public List<InterviewEvent> getEvents() { return Collections.unmodifiableList(events); }
    public List<InterviewPreparation> getPreparations() { return Collections.unmodifiableList(preparations); }
}
