package com.jobcommandcenter.assessment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OnlineAssessmentUnitTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();
    private final UUID applicationId = UUID.randomUUID();

    @Test
    @DisplayName("Assessment creation initializes INVITED status, initial audit event, and valid defaults")
    void assessmentCreationInitializesProperly() {
        Instant invitedAt = Instant.now();
        Instant expiresAt = invitedAt.plus(3, ChronoUnit.DAYS);

        OnlineAssessment assessment = OnlineAssessment.create(
            userId,
            jobId,
            applicationId,
            null,
            AssessmentPlatform.CODESIGNAL,
            "General Coding Assessment (GCA)",
            70,
            invitedAt,
            expiresAt,
            "https://app.codesignal.com/test/123",
            "CODE-998"
        );

        assertNotNull(assessment.getId());
        assertEquals(userId, assessment.getUserId());
        assertEquals(jobId, assessment.getJobId());
        assertEquals(applicationId, assessment.getApplicationId());
        assertNull(assessment.getInterviewId());
        assertEquals(AssessmentPlatform.CODESIGNAL, assessment.getPlatform());
        assertEquals("General Coding Assessment (GCA)", assessment.getTitle());
        assertEquals(AssessmentStatus.INVITED, assessment.getStatus());
        assertEquals(AssessmentResult.PENDING, assessment.getResult());
        assertEquals(70, assessment.getDurationMinutes());
        assertEquals(invitedAt, assessment.getInvitedAt());
        assertEquals(expiresAt, assessment.getExpiresAt());
        assertEquals("https://app.codesignal.com/test/123", assessment.getAssessmentUrl());
        assertEquals("CODE-998", assessment.getAccessCode());

        // Check initial event
        assertEquals(1, assessment.getEvents().size());
        OnlineAssessmentEvent event = assessment.getEvents().get(0);
        assertEquals(AssessmentEventType.INVITED, event.getEventType());
        assertEquals(AssessmentStatus.INVITED, event.getNewStatus());
        assertNull(event.getPreviousStatus());
    }

    @Test
    @DisplayName("Assessment creation rejects expiration time earlier than invitation time")
    void assessmentCreationEnforcesExpirationWindow() {
        Instant invitedAt = Instant.now();
        Instant expiresAt = invitedAt.minus(1, ChronoUnit.HOURS);

        assertThrows(InvalidAssessmentStateException.class, () ->
            OnlineAssessment.create(
                userId,
                jobId,
                null,
                null,
                AssessmentPlatform.HACKERRANK,
                "Backend Screening",
                90,
                invitedAt,
                expiresAt,
                null,
                null
            )
        );
    }

    @Test
    @DisplayName("Assessment creation rejects non-positive duration minutes")
    void assessmentCreationRejectsNonPositiveDuration() {
        Instant invitedAt = Instant.now();
        Instant expiresAt = invitedAt.plus(2, ChronoUnit.DAYS);

        assertThrows(InvalidAssessmentStateException.class, () ->
            OnlineAssessment.create(
                userId,
                jobId,
                null,
                null,
                AssessmentPlatform.CODILITY,
                "Core Algorithms",
                0, // invalid duration
                invitedAt,
                expiresAt,
                null,
                null
            )
        );
    }

    @Test
    @DisplayName("start() transitions from INVITED to IN_PROGRESS and logs STARTED event")
    void startTransitionsToInProgress() {
        Instant invitedAt = Instant.now();
        Instant expiresAt = invitedAt.plus(2, ChronoUnit.DAYS);

        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.LEETCODE, "OA Assessment", 60, invitedAt, expiresAt, null, null
        );

        Instant startTime = Instant.now();
        assessment.start(startTime);

        assertEquals(AssessmentStatus.IN_PROGRESS, assessment.getStatus());
        assertEquals(startTime, assessment.getScheduledStartTime());
        assertEquals(2, assessment.getEvents().size());

        OnlineAssessmentEvent startEvent = assessment.getEvents().get(1);
        assertEquals(AssessmentEventType.STARTED, startEvent.getEventType());
        assertEquals(AssessmentStatus.INVITED, startEvent.getPreviousStatus());
        assertEquals(AssessmentStatus.IN_PROGRESS, startEvent.getNewStatus());
    }

    @Test
    @DisplayName("start() rejects when assessment is EXPIRED, ABANDONED, or SUBMITTED")
    void startRejectsTerminalOrInactiveStates() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "Algorithms", 60, Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS), null, null
        );

        assessment.expire("Timed out");
        assertThrows(InvalidAssessmentStateException.class, () -> assessment.start(Instant.now()));

        OnlineAssessment abandoned = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "Algorithms", 60, Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS), null, null
        );
        abandoned.abandon("Candidate decided not to take");
        assertThrows(InvalidAssessmentStateException.class, () -> abandoned.start(Instant.now()));
    }

    @Test
    @DisplayName("submit() transitions to SUBMITTED with valid scores, notes, and completion timestamp")
    void submitTransitionsToSubmittedWithScores() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.CODESIGNAL, "GCA", 70, Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), null, null
        );
        assessment.start(Instant.now());

        Instant completionTime = Instant.now().plus(65, ChronoUnit.MINUTES);
        BigDecimal score = new BigDecimal("825.00");
        BigDecimal maxScore = new BigDecimal("850.00");

        assessment.submit(score, maxScore, "Passed all 4 tasks with optimal time complexity", "https://github.com/candidate/solutions", completionTime);

        assertEquals(AssessmentStatus.SUBMITTED, assessment.getStatus());
        assertEquals(score, assessment.getScore());
        assertEquals(maxScore, assessment.getMaxScore());
        assertEquals(completionTime, assessment.getCompletedAt());
        assertTrue(assessment.getSubmissionNotes().contains("optimal time complexity"));
        assertEquals("https://github.com/candidate/solutions", assessment.getSubmissionRepoUrl());

        OnlineAssessmentEvent submitEvent = assessment.getEvents().get(2);
        assertEquals(AssessmentEventType.SUBMITTED, submitEvent.getEventType());
        assertEquals(AssessmentStatus.IN_PROGRESS, submitEvent.getPreviousStatus());
        assertEquals(AssessmentStatus.SUBMITTED, submitEvent.getNewStatus());
    }

    @Test
    @DisplayName("submit() rejects negative scores and scores exceeding maximum score")
    void submitValidatesScoreBounds() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "OA", 90, Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), null, null
        );
        assessment.start(Instant.now());

        // Negative score
        assertThrows(InvalidAssessmentStateException.class, () ->
            assessment.submit(new BigDecimal("-10"), new BigDecimal("100"), null, null, Instant.now())
        );

        // Max score <= 0
        assertThrows(InvalidAssessmentStateException.class, () ->
            assessment.submit(new BigDecimal("50"), new BigDecimal("0"), null, null, Instant.now())
        );

        // Score > Max score
        assertThrows(InvalidAssessmentStateException.class, () ->
            assessment.submit(new BigDecimal("110"), new BigDecimal("100"), null, null, Instant.now())
        );
    }

    @Test
    @DisplayName("submit() rejects submission when assessment is EXPIRED, ABANDONED, or already SUBMITTED")
    void submitRejectsInvalidStates() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "OA", 90, Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), null, null
        );

        assessment.submit(new BigDecimal("90"), new BigDecimal("100"), null, null, Instant.now());

        // Cannot submit again
        assertThrows(InvalidAssessmentStateException.class, () ->
            assessment.submit(new BigDecimal("95"), new BigDecimal("100"), null, null, Instant.now())
        );

        // Cannot start after submission
        assertThrows(InvalidAssessmentStateException.class, () -> assessment.start(Instant.now()));
    }

    @Test
    @DisplayName("recordResult() updates evaluation outcome and logs audit event")
    void recordResultUpdatesEvaluation() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.BYTEBOARD, "System Take-Home", null, Instant.now(), Instant.now().plus(4, ChronoUnit.DAYS), null, null
        );
        assessment.submit(new BigDecimal("95"), new BigDecimal("100"), "Take-home code committed", "https://github.com/my-repo", Instant.now());

        assessment.recordResult(AssessmentResult.PASSED, "Candidate exceeded architectural bar");

        assertEquals(AssessmentResult.PASSED, assessment.getResult());
        OnlineAssessmentEvent resultEvent = assessment.getEvents().get(2);
        assertEquals(AssessmentEventType.RESULT_RECORDED, resultEvent.getEventType());
        assertTrue(resultEvent.getNotes().contains("exceeded architectural bar"));
    }

    @Test
    @DisplayName("recordResult() rejects on EXPIRED or ABANDONED assessments")
    void recordResultRejectsExpiredOrAbandoned() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "OA", 60, Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS), null, null
        );
        assessment.expire("Elapsed");

        assertThrows(InvalidAssessmentStateException.class, () ->
            assessment.recordResult(AssessmentResult.FAILED, "No score")
        );
    }

    @Test
    @DisplayName("extendDeadline() updates expiration timestamp and reactivates expired assessment")
    void extendDeadlineUpdatesAndReactivates() {
        Instant invitedAt = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant originalExpiresAt = invitedAt.plus(1, ChronoUnit.DAYS);

        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "OA", 60, invitedAt, originalExpiresAt, null, null
        );
        assessment.expire("Past deadline");
        assertEquals(AssessmentStatus.EXPIRED, assessment.getStatus());

        Instant newExpiresAt = Instant.now().plus(3, ChronoUnit.DAYS);
        assessment.extendDeadline(newExpiresAt, "Recruiter granted 3-day extension");

        assertEquals(AssessmentStatus.INVITED, assessment.getStatus());
        assertEquals(newExpiresAt, assessment.getExpiresAt());

        OnlineAssessmentEvent extendEvent = assessment.getEvents().get(2);
        assertEquals(AssessmentEventType.DEADLINE_EXTENDED, extendEvent.getEventType());
    }

    @Test
    @DisplayName("extendDeadline() rejects for SUBMITTED or ABANDONED assessments")
    void extendDeadlineRejectsSubmittedOrAbandoned() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.CODESIGNAL, "OA", 60, Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), null, null
        );
        assessment.submit(new BigDecimal("800"), new BigDecimal("850"), "Submitted", null, Instant.now());

        assertThrows(InvalidAssessmentStateException.class, () ->
            assessment.extendDeadline(Instant.now().plus(5, ChronoUnit.DAYS), "Extension request")
        );
    }

    @Test
    @DisplayName("Checklist items can be added, toggled, and completion percentage computed")
    void checklistItemsManagement() {
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.CODESIGNAL, "GCA", 70, Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), null, null
        );

        assertEquals(0, assessment.getChecklistCompletionPercentage());

        AssessmentChecklistItem item1 = AssessmentChecklistItem.create(
            assessment.getId(), "ALGORITHMS", "Practice 2D Matrix traversal", "Review DFS/BFS grid patterns", 1
        );
        AssessmentChecklistItem item2 = AssessmentChecklistItem.create(
            assessment.getId(), "DATA_STRUCTURES", "HashMap Prefix Sum", "Review Subarray Sum Equals K", 2
        );

        assessment.addChecklistItems(List.of(item1, item2));
        assertEquals(2, assessment.getChecklists().size());
        assertEquals(0, assessment.getChecklistCompletionPercentage());

        // Toggle first item
        boolean toggled = assessment.toggleChecklistItem(item1.getId());
        assertTrue(toggled);
        assertTrue(item1.isCompleted());
        // 1 of 2 completed => 50%
        assertEquals(50, assessment.getChecklistCompletionPercentage());

        // Toggle second item
        assessment.toggleChecklistItem(item2.getId());
        // 2 of 2 completed => 100%
        assertEquals(100, assessment.getChecklistCompletionPercentage());
    }
}
