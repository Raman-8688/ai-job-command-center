package com.jobcommandcenter.assessment.domain;

/**
 * Event classifications recorded in the immutable assessment audit timeline.
 */
public enum AssessmentEventType {
    INVITED,
    STARTED,
    SUBMITTED,
    RESULT_RECORDED,
    DEADLINE_EXTENDED,
    EXPIRED,
    ABANDONED,
    NOTE_ADDED
}
