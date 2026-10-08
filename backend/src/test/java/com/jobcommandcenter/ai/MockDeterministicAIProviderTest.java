package com.jobcommandcenter.ai;

import com.jobcommandcenter.ai.domain.AIJobAnalysisRequest;
import com.jobcommandcenter.ai.domain.AIJobAnalysisResponse;
import com.jobcommandcenter.ai.domain.AnalyzedTechnology;
import com.jobcommandcenter.ai.infrastructure.provider.MockDeterministicAIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockDeterministicAIProviderTest {

    private MockDeterministicAIProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MockDeterministicAIProvider();
    }

    @Test
    @DisplayName("Provider identifies metadata and provider names correctly")
    void providerMetadata() {
        assertThat(provider.getProviderName()).isEqualTo("MOCK");
        assertThat(provider.getDefaultModel()).isEqualTo("deterministic-rule-v1");
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Analyze senior backend posting extracts technologies, seniority, and responsibilities")
    void analyzeSeniorBackendPosting() {
        AIJobAnalysisRequest request = new AIJobAnalysisRequest(
            UUID.randomUUID(),
            "Sr. Software Engineer - Platform",
            "Fintech Corp",
            """
            About the Role:
            We are looking for a Senior Software Engineer to build high-scale banking platforms.
            
            Responsibilities:
            * Architect and build event-driven microservices using Java and Spring Boot.
            * Design resilient relational data models in PostgreSQL.
            * Deploy scalable containerized services with Docker and Kubernetes on AWS.
            * Collaborate with product managers and cross-functional engineering teams.
            
            Qualifications:
            * 5+ years of software engineering experience in enterprise Java systems.
            * Hands-on proficiency with Spring Boot, PostgreSQL, and Docker.
            * Bachelor's degree in Computer Science or equivalent practical experience.
            * Knowledge of Kafka is nice to have.
            """,
            "v1.0"
        );

        AIJobAnalysisResponse response = provider.analyzeJob(request);

        assertThat(response).isNotNull();
        assertThat(response.normalizedTitle()).contains("Senior");
        assertThat(response.seniorityLevel()).isEqualTo("SENIOR");
        assertThat(response.experienceExpectations()).contains("5+");
        assertThat(response.confidence()).isGreaterThan(new BigDecimal("0.80"));

        List<String> techNames = response.technologies().stream()
            .map(AnalyzedTechnology::technology)
            .toList();
        assertThat(techNames).contains("Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes", "AWS");

        // Kafka should be extracted with required = false because of "nice to have"
        AnalyzedTechnology kafkaTech = response.technologies().stream()
            .filter(t -> t.technology().equalsIgnoreCase("Kafka"))
            .findFirst()
            .orElse(null);
        if (kafkaTech != null) {
            assertThat(kafkaTech.required()).isFalse();
        }

        assertThat(response.coreResponsibilities()).isNotEmpty();
        assertThat(response.educationRequirements()).anyMatch(e -> e.contains("Bachelor"));
    }

    @Test
    @DisplayName("Analyze detects potential red flags from job posting context")
    void analyzeDetectsRedFlags() {
        AIJobAnalysisRequest request = new AIJobAnalysisRequest(
            UUID.randomUUID(),
            "Rockstar Ninja Developer",
            "Chaos Startup",
            """
            Looking for a 10x rockstar developer. We work in a fast-paced high stress environment
            where you will wear many hats and work unlimited overtime. Competitive salary offered.
            """,
            "v1.0"
        );

        AIJobAnalysisResponse response = provider.analyzeJob(request);

        assertThat(response.potentialRedFlags()).isNotEmpty();
        assertThat(response.potentialRedFlags()).anyMatch(f -> f.contains("rockstar"));
        assertThat(response.potentialRedFlags()).anyMatch(f -> f.contains("overload") || f.contains("balance"));
    }
}
