package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OnlineAssessmentMappingTest {

    @Test
    @DisplayName("OnlineAssessmentJpaEntity maps from and to domain losslessly")
    void onlineAssessmentEntityMappingBidirectional() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID appId = UUID.randomUUID();
        UUID interviewId = UUID.randomUUID();

        Instant now = Instant.now();
        Instant expires = now.plus(3, ChronoUnit.DAYS);
        Instant started = now.plus(1, ChronoUnit.HOURS);
        Instant completed = now.plus(2, ChronoUnit.HOURS);

        OnlineAssessment assessment = new OnlineAssessment(
            id,
            userId,
            jobId,
            appId,
            interviewId,
            AssessmentPlatform.CODESIGNAL,
            "General Coding Assessment",
            AssessmentStatus.SUBMITTED,
            AssessmentResult.PASSED,
            70,
            now,
            expires,
            started,
            completed,
            new BigDecimal("830.00"),
            new BigDecimal("850.00"),
            "https://codesignal.com/eval/123",
            "CODE-123",
            "Solved all 4 problems with optimal complexity",
            "https://github.com/candidate/solutions",
            now,
            now,
            1L,
            List.of(
                new OnlineAssessmentEvent(UUID.randomUUID(), id, null, AssessmentStatus.INVITED, AssessmentEventType.INVITED, "Invited", "USER", now),
                new OnlineAssessmentEvent(UUID.randomUUID(), id, AssessmentStatus.INVITED, AssessmentStatus.IN_PROGRESS, AssessmentEventType.STARTED, "Started", "USER", started),
                new OnlineAssessmentEvent(UUID.randomUUID(), id, AssessmentStatus.IN_PROGRESS, AssessmentStatus.SUBMITTED, AssessmentEventType.SUBMITTED, "Submitted", "USER", completed)
            ),
            List.of(
                new AssessmentChecklistItem(UUID.randomUUID(), id, "ALGORITHMS", "DFS/BFS Grid", "Matrix pattern", true, 1, now, now),
                new AssessmentChecklistItem(UUID.randomUUID(), id, "DATA_STRUCTURES", "Prefix Sum HashMap", "Range queries", false, 2, now, now)
            )
        );

        // Convert to JPA entity
        OnlineAssessmentJpaEntity entity = OnlineAssessmentJpaEntity.fromDomain(assessment);

        assertEquals(id, entity.getId());
        assertEquals(userId, entity.getUserId());
        assertEquals(jobId, entity.getJobId());
        assertEquals(appId, entity.getApplicationId());
        assertEquals(interviewId, entity.getInterviewId());
        assertEquals(AssessmentPlatform.CODESIGNAL, entity.getPlatform());
        assertEquals("General Coding Assessment", entity.getTitle());
        assertEquals(AssessmentStatus.SUBMITTED, entity.getStatus());
        assertEquals(AssessmentResult.PASSED, entity.getResult());
        assertEquals(70, entity.getDurationMinutes());
        assertEquals(now, entity.getInvitedAt());
        assertEquals(expires, entity.getExpiresAt());
        assertEquals(started, entity.getScheduledStartTime());
        assertEquals(completed, entity.getCompletedAt());
        assertEquals(new BigDecimal("830.00"), entity.getScore());
        assertEquals(new BigDecimal("850.00"), entity.getMaxScore());
        assertEquals("https://codesignal.com/eval/123", entity.getAssessmentUrl());
        assertEquals("CODE-123", entity.getAccessCode());
        assertEquals(1L, entity.getVersion());
        assertEquals(3, entity.getEvents().size());
        assertEquals(2, entity.getChecklists().size());

        // Convert back to domain
        OnlineAssessment converted = entity.toDomain();

        assertEquals(assessment.getId(), converted.getId());
        assertEquals(assessment.getUserId(), converted.getUserId());
        assertEquals(assessment.getJobId(), converted.getJobId());
        assertEquals(assessment.getApplicationId(), converted.getApplicationId());
        assertEquals(assessment.getInterviewId(), converted.getInterviewId());
        assertEquals(assessment.getPlatform(), converted.getPlatform());
        assertEquals(assessment.getTitle(), converted.getTitle());
        assertEquals(assessment.getStatus(), converted.getStatus());
        assertEquals(assessment.getResult(), converted.getResult());
        assertEquals(assessment.getDurationMinutes(), converted.getDurationMinutes());
        assertEquals(assessment.getScore(), converted.getScore());
        assertEquals(assessment.getMaxScore(), converted.getMaxScore());
        assertEquals(3, converted.getEvents().size());
        assertEquals(2, converted.getChecklists().size());
        assertEquals(50, converted.getChecklistCompletionPercentage());
    }

    @Test
    @DisplayName("CompanyDossierJpaEntity maps from and to domain losslessly")
    void companyDossierEntityMappingBidirectional() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Instant now = Instant.now();

        CompanyDossier dossier = new CompanyDossier(
            id,
            userId,
            jobId,
            "Datadog",
            "GROWTH",
            "Observability SaaS platform",
            "Tens of millions of metrics per sec",
            "Java 21, Spring Boot, Kafka, ClickHouse",
            "Deep telemetry, blameless post-mortems",
            "High-throughput stream processing",
            "Discuss experience with Kafka partitioning",
            "How is cross-region lag managed during failover?",
            now,
            now
        );

        CompanyDossierJpaEntity entity = CompanyDossierJpaEntity.fromDomain(dossier);

        assertEquals(id, entity.getId());
        assertEquals(userId, entity.getUserId());
        assertEquals(jobId, entity.getJobId());
        assertEquals("Datadog", entity.getCompanyName());
        assertEquals("GROWTH", entity.getCompanyTier());
        assertEquals("Observability SaaS platform", entity.getOverview());

        CompanyDossier converted = entity.toDomain();

        assertEquals(dossier.getId(), converted.getId());
        assertEquals(dossier.getUserId(), converted.getUserId());
        assertEquals(dossier.getJobId(), converted.getJobId());
        assertEquals(dossier.getCompanyName(), converted.getCompanyName());
        assertEquals(dossier.getCompanyTier(), converted.getCompanyTier());
        assertEquals(dossier.getCoreTechStack(), converted.getCoreTechStack());
    }
}
