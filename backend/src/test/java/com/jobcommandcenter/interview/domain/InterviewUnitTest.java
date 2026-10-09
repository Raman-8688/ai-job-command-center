package com.jobcommandcenter.interview.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InterviewUnitTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();

    @Test
    @DisplayName("Interview creation enforces start time before end time")
    void interviewCreationEnforcesTimeWindow() {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.minus(1, ChronoUnit.HOURS);

        assertThrows(InvalidInterviewStateException.class, () ->
            Interview.create(
                userId,
                null,
                jobId,
                InterviewRound.TECHNICAL_SCREEN,
                1,
                InterviewFormat.VIDEO_CALL,
                start,
                end,
                "UTC",
                "https://meet.google.com/abc-def-ghi",
                null,
                "John Doe",
                "Senior Staff Engineer",
                "System design prep"
            )
        );
    }

    @Test
    @DisplayName("Interview creation initializes status SCHEDULED and logs initial event")
    void interviewCreationInitializesProperly() {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        Interview interview = Interview.create(
            userId,
            null,
            jobId,
            InterviewRound.TECHNICAL_SCREEN,
            1,
            InterviewFormat.VIDEO_CALL,
            start,
            end,
            "UTC",
            "https://meet.google.com/abc-def-ghi",
            "Google Meet",
            "Sarah Connor",
            "Engineering Director",
            "Focus on microservices"
        );

        assertNotNull(interview.getId());
        assertEquals(InterviewStatus.SCHEDULED, interview.getStatus());
        assertEquals(InterviewOutcome.PENDING, interview.getOutcome());
        assertEquals(1, interview.getEvents().size());
        assertEquals(InterviewEventType.SCHEDULED, interview.getEvents().get(0).getEventType());
    }

    @Test
    @DisplayName("Reschedule transitions status to RESCHEDULED and records event")
    void rescheduleTransitionsStatus() {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        Interview interview = Interview.create(
            userId, null, jobId, InterviewRound.SYSTEM_DESIGN, 2, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        Instant newStart = start.plus(1, ChronoUnit.DAYS);
        Instant newEnd = newStart.plus(1, ChronoUnit.HOURS);

        interview.reschedule(newStart, newEnd, "America/New_York", "Interviewer conflict", "USER");

        assertEquals(InterviewStatus.RESCHEDULED, interview.getStatus());
        assertEquals(newStart, interview.getScheduledStartTime());
        assertEquals(newEnd, interview.getScheduledEndTime());
        assertEquals("America/New_York", interview.getTimeZone());
        assertEquals(2, interview.getEvents().size());
        assertEquals(InterviewEventType.RESCHEDULED, interview.getEvents().get(1).getEventType());
    }

    @Test
    @DisplayName("Cannot reschedule or cancel a completed interview")
    void cannotRescheduleOrCancelCompletedInterview() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        Interview interview = Interview.create(
            userId, null, jobId, InterviewRound.INITIAL_SCREEN, 1, InterviewFormat.PHONE_SCREEN,
            start, end, "UTC", null, null, null, null, null
        );

        interview.complete("Went very well, strong culture fit", InterviewOutcome.PASSED, "Recruiter will follow up", "USER");
        assertEquals(InterviewStatus.COMPLETED, interview.getStatus());
        assertEquals(InterviewOutcome.PASSED, interview.getOutcome());

        assertThrows(InvalidInterviewStateException.class, () ->
            interview.reschedule(start.plus(2, ChronoUnit.DAYS), end.plus(2, ChronoUnit.DAYS), "UTC", "Try to reschedule", "USER")
        );

        assertThrows(InvalidInterviewStateException.class, () ->
            interview.cancel("Try to cancel", "USER")
        );
    }
}
