package com.jobcommandcenter.assessment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CompanyDossierUnitTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();

    @Test
    @DisplayName("CompanyDossier creation initializes fields and default tier properly")
    void companyDossierCreationInitializesProperly() {
        CompanyDossier dossier = CompanyDossier.create(
            userId,
            jobId,
            "Datadog",
            "GROWTH",
            "Observability and monitoring SaaS platform at hyper-scale",
            "Millions of metrics per second, distributed Kafka pipelines, Go/Java microservices",
            "Java 21, Spring Boot, Kafka, PostgreSQL, ClickHouse, AWS",
            "Blameless post-mortem culture, high engineering rigor, deep telemetry",
            "High-throughput low-latency stream processing, event sourcing",
            "Discuss experience with Kafka partitioning and Spring Boot microservices",
            "How does the observability team manage cross-region replication lag during regional failover?"
        );

        assertNotNull(dossier.getId());
        assertEquals(userId, dossier.getUserId());
        assertEquals(jobId, dossier.getJobId());
        assertEquals("Datadog", dossier.getCompanyName());
        assertEquals("GROWTH", dossier.getCompanyTier());
        assertTrue(dossier.getOverview().contains("Observability"));
        assertTrue(dossier.getCoreTechStack().contains("Kafka"));
        assertTrue(dossier.getInterviewerQuestions().contains("cross-region replication lag"));
        assertNotNull(dossier.getCreatedAt());
        assertNotNull(dossier.getUpdatedAt());
    }

    @Test
    @DisplayName("updateBriefing updates intelligence content and updates timestamp")
    void updateBriefingUpdatesContent() {
        CompanyDossier dossier = CompanyDossier.create(
            userId,
            jobId,
            "Stripe",
            "BIG_TECH",
            "Initial overview",
            "Scale 1",
            "Tech 1",
            "Culture 1",
            "Arch 1",
            "Points 1",
            "Questions 1"
        );

        dossier.updateBriefing(
            "Global financial infrastructure platform processing billions",
            "Tens of thousands TPS, 99.999% availability SLAs",
            "Java, Ruby, Sorbet, Envoy, CockroachDB",
            "Written RFC culture, high API design standards",
            "Idempotency, distributed ledger consistency",
            "Emphasize experience with distributed transactions and payment consistency",
            "What architectural investments are planned to support next-generation real-time settlement?"
        );

        assertTrue(dossier.getOverview().contains("Global financial infrastructure"));
        assertTrue(dossier.getCoreTechStack().contains("CockroachDB"));
        assertTrue(dossier.getArchitectureFocus().contains("Idempotency"));
    }
}
