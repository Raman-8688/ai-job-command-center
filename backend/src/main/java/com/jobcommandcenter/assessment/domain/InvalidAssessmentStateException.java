package com.jobcommandcenter.assessment.domain;

import com.jobcommandcenter.common.error.BusinessException;

/**
 * Thrown when an illegal online assessment lifecycle transition, timing rule, or score violation occurs.
 */
public class InvalidAssessmentStateException extends BusinessException {

    public InvalidAssessmentStateException(String message) {
        super(message);
    }
}
