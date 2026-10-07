package com.jobcommandcenter.security;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public endpoint /api/public/ping should be accessible without authentication")
    void publicPingShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("pong"))
            .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    @DisplayName("Public actuator health endpoint should be accessible without authentication")
    void actuatorHealthShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Protected API endpoint should return 401 Unauthorized with RFC 7807 Problem Details")
    void protectedEndpointShouldReturn401ProblemDetails() throws Exception {
        mockMvc.perform(post("/api/test-infrastructure/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"test@example.com\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists("X-Correlation-ID"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.detail").value(containsString("Full authentication is required")))
            .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "test-user")
    @DisplayName("Protected API endpoint should be accessible with authenticated mock user")
    void protectedEndpointShouldBeAccessibleWhenAuthenticated() throws Exception {
        mockMvc.perform(post("/api/test-infrastructure/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Valid User\",\"email\":\"valid@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Valid"))
            .andExpect(jsonPath("$.name").value("Valid User"));
    }
}
