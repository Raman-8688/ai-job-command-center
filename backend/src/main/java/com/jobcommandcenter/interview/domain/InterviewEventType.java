package com.jobcommandcenter.interview.domain;

/**
 * Event categories logged in the immutable interview audit timeline.
 */
public enum InterviewEventType {
    SCHEDULED,
    RESCHEDULED,
    COMPLETED,
    CANCELLED,
    STATUS_CHANGED,
    OUTCOME_UPDATED,
    PREPARATION_GENERATED,
    NOTE_ADDED
}
