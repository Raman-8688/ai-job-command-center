package com.jobcommandcenter.application.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobApplicationUnitTest {

    @Test
    @DisplayName("createNew initializes application in DRAFT and adds CREATED event")
    void createNewInitializesCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        JobApplication app = JobApplication.createNew(
            userId,
            jobId,
            ApplicationStatus.DRAFT,
            null,
            ApplicationSource.LINKEDIN,
            "REF-100",
            null,
            null,
            "Initial notes"
        );

        assertThat(app.getId()).isNotNull();
        assertThat(app.getUserId()).isEqualTo(userId);
        assertThat(app.getJobId()).isEqualTo(jobId);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.DRAFT);
        assertThat(app.getSubmissionSource()).isEqualTo(ApplicationSource.LINKEDIN);
        assertThat(app.getExternalReference()).isEqualTo("REF-100");
        assertThat(app.getNotes()).isEqualTo("Initial notes");
        assertThat(app.getEvents()).hasSize(1);

        JobApplicationEvent event = app.getEvents().get(0);
        assertThat(event.getEventType()).isEqualTo(ApplicationEventType.CREATED);
        assertThat(event.getPreviousStatus()).isNull();
        assertThat(event.getNewStatus()).isEqualTo(ApplicationStatus.DRAFT);
    }

    @Test
    @DisplayName("transitionTo allows valid lifecycle sequence DRAFT -> APPLIED -> SCREENING -> INTERVIEW -> OFFER -> ACCEPTED")
    void validLifecycleTransitions() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        JobApplication app = JobApplication.createNew(
            userId,
            jobId,
            ApplicationStatus.DRAFT,
            null,
            ApplicationSource.COMPANY_WEBSITE,
            null,
            null,
            null,
            null
        );

        app.transitionTo(ApplicationStatus.APPLIED, "Submitted via company site", EventSource.USER);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(app.getAppliedAt()).isNotNull();
        assertThat(app.getEvents()).hasSize(2);

        app.transitionTo(ApplicationStatus.SCREENING, "Recruiter reached out", EventSource.USER);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.SCREENING);

        app.transitionTo(ApplicationStatus.INTERVIEW, "Technical screen scheduled", EventSource.USER);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);

        app.transitionTo(ApplicationStatus.OFFER, "Received verbal offer", EventSource.USER);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.OFFER);

        app.transitionTo(ApplicationStatus.ACCEPTED, "Offer accepted!", EventSource.USER);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("transitionTo rejects invalid state transition DRAFT -> OFFER with InvalidStateTransitionException")
    void invalidDirectTransitionThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        JobApplication app = JobApplication.createNew(
            userId,
            jobId,
            ApplicationStatus.DRAFT,
            null,
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            null
        );

        assertThatThrownBy(() -> app.transitionTo(ApplicationStatus.OFFER, "Jump to offer", EventSource.USER))
            .isInstanceOf(InvalidStateTransitionException.class)
            .hasMessageContaining("Invalid application status transition from DRAFT to OFFER");
    }

    @Test
    @DisplayName("transitionTo rejects invalid transition ACCEPTED -> APPLIED")
    void invalidAcceptedToAppliedTransitionThrowsException() {
        JobApplication app = new JobApplication(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            ApplicationStatus.ACCEPTED,
            Instant.now(),
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            Instant.now(),
            Instant.now(),
            0L,
            null
        );

        assertThatThrownBy(() -> app.transitionTo(ApplicationStatus.APPLIED, "Invalid regress", EventSource.USER))
            .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("transitionTo from terminal REJECTED or WITHDRAWN to DRAFT/APPLIED records REOPENED event")
    void terminalReopeningRecordsReopenedEvent() {
        JobApplication app = new JobApplication(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            ApplicationStatus.REJECTED,
            Instant.now(),
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            Instant.now(),
            Instant.now(),
            0L,
            null
        );

        app.transitionTo(ApplicationStatus.APPLIED, "Re-applied for new opening", EventSource.USER);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(app.getEvents()).hasSize(1);
        assertThat(app.getEvents().get(0).getEventType()).isEqualTo(ApplicationEventType.REOPENED);
    }

    @Test
    @DisplayName("linkResume records RESUME_LINKED event and updates resume IDs")
    void linkResumeRecordsEvent() {
        JobApplication app = JobApplication.createNew(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ApplicationStatus.DRAFT,
            null,
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            null
        );

        UUID resumeId = UUID.randomUUID();
        UUID tailoredResumeId = UUID.randomUUID();

        app.linkResume(resumeId, tailoredResumeId, "Linked customized resume v2", EventSource.USER);

        assertThat(app.getResumeId()).isEqualTo(resumeId);
        assertThat(app.getTailoredResumeId()).isEqualTo(tailoredResumeId);
        assertThat(app.getEvents()).hasSize(2);
        assertThat(app.getEvents().get(1).getEventType()).isEqualTo(ApplicationEventType.RESUME_LINKED);
    }

    @Test
    @DisplayName("addNote appends NOTE_ADDED event to application events")
    void addNoteAppendsEvent() {
        JobApplication app = JobApplication.createNew(
            UUID.randomUUID(),
            UUID.randomUUID(),
            ApplicationStatus.APPLIED,
            null,
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            null
        );

        app.addNote("Sent follow-up email to recruiter", EventSource.USER);
        assertThat(app.getEvents()).hasSize(2);
        assertThat(app.getEvents().get(1).getEventType()).isEqualTo(ApplicationEventType.NOTE_ADDED);
        assertThat(app.getEvents().get(1).getNotes()).isEqualTo("Sent follow-up email to recruiter");
    }
}
