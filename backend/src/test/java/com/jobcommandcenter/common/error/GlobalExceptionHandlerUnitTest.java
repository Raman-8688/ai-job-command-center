package com.jobcommandcenter.common.error;

import com.jobcommandcenter.common.correlation.CorrelationIdHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test-resource");
        CorrelationIdHolder.setCorrelationId("unit-test-corr-id");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdHolder.clear();
    }

    @Test
    @DisplayName("Should return 404 Problem Details on ResourceNotFoundException")
    void shouldHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Job 42 not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().title()).isEqualTo("Resource Not Found");
        assertThat(response.getBody().detail()).isEqualTo("Job 42 not found");
        assertThat(response.getBody().instance()).isEqualTo("/api/test-resource");
        assertThat(response.getBody().correlationId()).isEqualTo("unit-test-corr-id");
    }

    @Test
    @DisplayName("Should return 409 Problem Details on ConflictException")
    void shouldHandleConflict() {
        ConflictException ex = new ConflictException("Duplicate job entry detected");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().title()).isEqualTo("Resource Conflict");
        assertThat(response.getBody().detail()).isEqualTo("Duplicate job entry detected");
    }

    @Test
    @DisplayName("Should return 500 Problem Details with safe masked message on unexpected exception")
    void shouldMaskInternalServerError() {
        Exception ex = new RuntimeException("Sensitive database credentials or internal stack trace");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().title()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().detail()).doesNotContain("database credentials");
        assertThat(response.getBody().detail()).contains("An unexpected internal error occurred");
        assertThat(response.getBody().correlationId()).isEqualTo("unit-test-corr-id");
    }
}
