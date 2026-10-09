package com.jobcommandcenter.interview.domain;

/**
 * Lifecycle status of an individual interview session.
 */
public enum InterviewStatus {
    SCHEDULED,
    RESCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
