package com.jobcommandcenter.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representation of a single field-level validation constraint violation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationError(
    String field,
    Object rejectedValue,
    String message
) {}
