package com.jobcommandcenter.application.domain;

import com.jobcommandcenter.common.error.BusinessException;

/**
 * Exception thrown when an illegal or unsupported lifecycle transition is attempted.
 */
public class InvalidStateTransitionException extends BusinessException {

    public InvalidStateTransitionException(ApplicationStatus from, ApplicationStatus to) {
        super(String.format("Invalid application status transition from %s to %s", from, to));
    }

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
