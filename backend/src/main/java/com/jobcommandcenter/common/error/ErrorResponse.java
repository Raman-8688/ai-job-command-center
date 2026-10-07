package com.jobcommandcenter.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Standard RFC 7807 Problem Details representation for API errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Instant timestamp,
    String correlationId,
    List<ValidationError> validationErrors
) {
    public static ErrorResponse of(String title, int status, String detail, String instance, String correlationId) {
        return new ErrorResponse(
            "about:blank",
            title,
            status,
            detail,
            instance,
            Instant.now(),
            correlationId,
            Collections.emptyList()
        );
    }

    public static ErrorResponse ofValidation(String title, int status, String detail, String instance,
                                             String correlationId, List<ValidationError> validationErrors) {
        return new ErrorResponse(
            "about:blank",
            title,
            status,
            detail,
            instance,
            Instant.now(),
            correlationId,
            validationErrors != null ? validationErrors : Collections.emptyList()
        );
    }
}
