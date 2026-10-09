package com.jobcommandcenter.application.domain;

/**
 * Stage and transition event types recorded in the application timeline.
 */
public enum ApplicationEventType {
    CREATED,
    STATUS_CHANGED,
    RESUME_LINKED,
    INTERVIEW_SCHEDULED,
    NOTE_ADDED,
    FOLLOW_UP_SCHEDULED,
    WITHDRAWN,
    REOPENED
}
