package com.jobcommandcenter.common.error;

/**
 * Thrown when a resource operation results in a state conflict or duplicate.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
