package com.jobcommandcenter.interview.domain;

import com.jobcommandcenter.common.error.BusinessException;

/**
 * Thrown when an illegal interview lifecycle transition or timing conflict is detected.
 */
public class InvalidInterviewStateException extends BusinessException {
    public InvalidInterviewStateException(String message) {
        super(message);
    }
}
