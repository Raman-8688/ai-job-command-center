package com.jobcommandcenter.common.error;

import com.jobcommandcenter.common.correlation.CorrelationIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized REST exception handler providing uniform RFC 7807 Problem Details responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                  HttpServletRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationError(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage()
            ));
        }

        ErrorResponse response = ErrorResponse.ofValidation(
            "Validation Failed",
            HttpStatus.BAD_REQUEST.value(),
            "One or more input fields failed validation constraints",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId(),
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                                                                       HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Validation Failed",
            HttpStatus.BAD_REQUEST.value(),
            "Request method parameters failed validation",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
            .map(cv -> new ValidationError(
                cv.getPropertyPath().toString(),
                cv.getInvalidValue(),
                cv.getMessage()
            ))
            .toList();

        ErrorResponse response = ErrorResponse.ofValidation(
            "Constraint Violation",
            HttpStatus.BAD_REQUEST.value(),
            "Parameters violated validation constraints",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId(),
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Malformed Request Body",
            HttpStatus.BAD_REQUEST.value(),
            "The request payload is malformed or unreadable JSON",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Invalid Argument",
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex,
                                                                 HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Business Rule Violation",
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Resource Not Found",
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                                                               HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Resource Not Found",
            HttpStatus.NOT_FOUND.value(),
            "The requested endpoint does not exist: " + request.getRequestURI(),
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Method Not Allowed",
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "HTTP method " + ex.getMethod() + " is not supported for this endpoint",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex,
                                                        HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Resource Conflict",
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                      HttpServletRequest request) {
        log.warn("Database integrity violation on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "Data Conflict",
            HttpStatus.CONFLICT.value(),
            "Database constraint violation or conflict with existing resource",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Forbidden",
            HttpStatus.FORBIDDEN.value(),
            "Access to the requested resource is denied",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex,
                                                                       HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
            "Unauthorized",
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication is required to access this resource",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex,
                                                                HttpServletRequest request) {
        log.error("Unhandled server exception on {} [correlationId={}]",
            request.getRequestURI(), CorrelationIdHolder.getCorrelationId(), ex);

        ErrorResponse response = ErrorResponse.of(
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected internal error occurred. Please contact system administrator with correlation ID.",
            request.getRequestURI(),
            CorrelationIdHolder.getCorrelationId()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
