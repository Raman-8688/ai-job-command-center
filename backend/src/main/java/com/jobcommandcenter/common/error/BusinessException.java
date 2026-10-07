package com.jobcommandcenter.common.error;

/**
 * Base business rule exception indicating an unprocessable request or invalid state.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
