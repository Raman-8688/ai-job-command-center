package com.jobcommandcenter.common.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class ValidationAndErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 400 Bad Request with field validation errors when input is invalid")
    void shouldReturn400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/api/test-infrastructure/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"A\",\"email\":\"not-an-email\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(header().exists("X-Correlation-ID"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.validationErrors", hasSize(2)))
            .andExpect(jsonPath("$.validationErrors[*].field", containsInAnyOrder("name", "email")))
            .andExpect(jsonPath("$.correlationId").isNotEmpty())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Should return 400 Bad Request on malformed JSON payload")
    void shouldReturn400OnMalformedJson() throws Exception {
        mockMvc.perform(post("/api/test-infrastructure/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ malformed-json: true }"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Malformed Request Body"))
            .andExpect(jsonPath("$.detail").value(containsString("malformed or unreadable JSON")));
    }

    @Test
    @DisplayName("Should return 404 Not Found on ResourceNotFoundException")
    void shouldReturn404OnCustomResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/test-infrastructure/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Test resource was not found"))
            .andExpect(jsonPath("$.instance").value("/api/test-infrastructure/not-found"));
    }

    @Test
    @DisplayName("Should return 404 Not Found on undefined URI")
    void shouldReturn404OnUndefinedUri() throws Exception {
        mockMvc.perform(get("/api/undefined-endpoint-xyz"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    @DisplayName("Should return 405 Method Not Allowed when method is unsupported")
    void shouldReturn405OnUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/api/public/ping"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.title").value("Method Not Allowed"))
            .andExpect(jsonPath("$.detail").value(containsString("HTTP method POST is not supported")));
    }

    @Test
    @DisplayName("Should return 409 Conflict on ConflictException")
    void shouldReturn409OnConflict() throws Exception {
        mockMvc.perform(get("/api/test-infrastructure/conflict"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Resource Conflict"))
            .andExpect(jsonPath("$.detail").value("Test resource conflict detected"));
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error with masked message on unexpected exception")
    void shouldReturn500WithMaskedMessage() throws Exception {
        mockMvc.perform(get("/api/test-infrastructure/error"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.detail").value(containsString("An unexpected internal error occurred")))
            .andExpect(jsonPath("$.detail").value(not(containsString("Deliberate unhandled failure"))));
    }
}
