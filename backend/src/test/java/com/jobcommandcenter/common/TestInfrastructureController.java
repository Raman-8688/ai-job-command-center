package com.jobcommandcenter.common;

import com.jobcommandcenter.common.error.ConflictException;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test harness controller located strictly in test scope.
 * Validates error handlers, validation constraints, and correlation tracking.
 */
@RestController
public class TestInfrastructureController {

    public record TestDto(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be well-formed")
        String email
    ) {}

    @GetMapping("/api/public/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "pong"));
    }

    @PostMapping("/api/test-infrastructure/validate")
    public ResponseEntity<Map<String, String>> validate(@Valid @RequestBody TestDto dto) {
        return ResponseEntity.ok(Map.of("message", "Valid", "name", dto.name()));
    }

    @GetMapping("/api/test-infrastructure/not-found")
    public ResponseEntity<Void> triggerNotFound() {
        throw new ResourceNotFoundException("Test resource was not found");
    }

    @GetMapping("/api/test-infrastructure/conflict")
    public ResponseEntity<Void> triggerConflict() {
        throw new ConflictException("Test resource conflict detected");
    }

    @GetMapping("/api/test-infrastructure/error")
    public ResponseEntity<Void> triggerServerError() {
        throw new RuntimeException("Deliberate unhandled failure for testing");
    }
}
