package com.jobcommandcenter.analytics;

import com.jobcommandcenter.analytics.application.AnalyticsQueryService;
import com.jobcommandcenter.analytics.domain.*;
import com.jobcommandcenter.application.domain.ApplicationEventType;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.EventSource;
import com.jobcommandcenter.application.infrastructure.JobApplicationEventJpaEntity;
import com.jobcommandcenter.application.infrastructure.JobApplicationJpaEntity;
import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import com.jobcommandcenter.assessment.domain.AssessmentResult;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.assessment.infrastructure.OnlineAssessmentJpaEntity;
import com.jobcommandcenter.interview.domain.InterviewFormat;
import com.jobcommandcenter.interview.domain.InterviewOutcome;
import com.jobcommandcenter.interview.domain.InterviewRound;
import com.jobcommandcenter.interview.domain.InterviewStatus;
import com.jobcommandcenter.interview.infrastructure.InterviewJpaEntity;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.job.infrastructure.JobJpaEntity;
import com.jobcommandcenter.job.infrastructure.JobSkillJpaEntity;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillProficiency;
import com.jobcommandcenter.skill.domain.VerificationSource;
import com.jobcommandcenter.skill.infrastructure.SkillJpaEntity;
import com.jobcommandcenter.skill.infrastructure.UserSkillJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Analytics Persistence & Tenant Isolation Integration Tests")
class AnalyticsPersistenceIntegrationTest {

    @Autowired
    private AnalyticsQueryService analyticsService;

    @Autowired
    private EntityManager entityManager;

    private UUID tenantUserA;
    private UUID tenantUserB;
    private UUID job1;
    private UUID job2;
    private UUID skillJava;
    private UUID skillDocker;

    @BeforeEach
    void setUp() {
        tenantUserA = UUID.randomUUID();
        tenantUserB = UUID.randomUUID();

        // 0. Create Users for Foreign Key Integrity
        com.jobcommandcenter.user.infrastructure.UserJpaEntity userA = new com.jobcommandcenter.user.infrastructure.UserJpaEntity(
            tenantUserA, "userA@example.com", "hashA", "User", "A", "User A",
            com.jobcommandcenter.user.domain.AccountStatus.ACTIVE, com.jobcommandcenter.user.domain.Role.USER,
            Instant.now(), Instant.now(), null
        );
        entityManager.persist(userA);

        com.jobcommandcenter.user.infrastructure.UserJpaEntity userB = new com.jobcommandcenter.user.infrastructure.UserJpaEntity(
            tenantUserB, "userB@example.com", "hashB", "User", "B", "User B",
            com.jobcommandcenter.user.domain.AccountStatus.ACTIVE, com.jobcommandcenter.user.domain.Role.USER,
            Instant.now(), Instant.now(), null
        );
        entityManager.persist(userB);

        // 1. Create Catalog Skills
        skillJava = UUID.randomUUID();
        SkillJpaEntity javaSkill = new SkillJpaEntity(
            skillJava, "Java", "java-" + UUID.randomUUID().toString().substring(0, 8), SkillCategory.LANGUAGE, Instant.now(), Instant.now()
        );
        entityManager.persist(javaSkill);

        skillDocker = UUID.randomUUID();
        SkillJpaEntity dockerSkill = new SkillJpaEntity(
            skillDocker, "Docker", "docker-" + UUID.randomUUID().toString().substring(0, 8), SkillCategory.DEVOPS, Instant.now(), Instant.now()
        );
        entityManager.persist(dockerSkill);

        // 2. Create Canonical Jobs
        job1 = UUID.randomUUID();
        JobJpaEntity j1 = new JobJpaEntity();
        j1.setId(job1);
        j1.setExternalJobId("EXT-1");
        j1.setTitle("Backend Engineer");
        j1.setCompanyName("TechCorp");
        j1.setCompanyWebsite("https://techcorp.com");
        j1.setJobUrl("https://jobs.techcorp.com/1");
        j1.setDescription("Core Java and cloud platform role");
        j1.setLocation("San Francisco, CA");
        j1.setWorkMode(WorkMode.REMOTE);
        j1.setEmploymentType(EmploymentType.FULL_TIME);
        j1.setExperienceMinYears(BigDecimal.valueOf(3.0));
        j1.setExperienceMaxYears(BigDecimal.valueOf(7.0));
        j1.setSalaryMin(BigDecimal.valueOf(140000));
        j1.setSalaryMax(BigDecimal.valueOf(180000));
        j1.setSalaryCurrency("USD");
        j1.setSource(JobSource.LINKEDIN);
        j1.setSourceUrl("https://linkedin.com/jobs/1");
        j1.setPostedAt(Instant.now());
        j1.setDiscoveredAt(Instant.now());
        j1.setApplicationDeadline(Instant.now().plus(30, ChronoUnit.DAYS));
        j1.setStatus(JobStatus.ACTIVE);
        j1.setDeduplicationHash("hash-1");
        j1.setCreatedAt(Instant.now());
        j1.setUpdatedAt(Instant.now());

        JobSkillJpaEntity js1 = new JobSkillJpaEntity(
            UUID.randomUUID(), job1, skillJava, SkillRequirementType.REQUIRED, BigDecimal.valueOf(3.0), Instant.now()
        );
        j1.getSkills().add(js1);
        entityManager.persist(j1);

        job2 = UUID.randomUUID();
        JobJpaEntity j2 = new JobJpaEntity();
        j2.setId(job2);
        j2.setExternalJobId("EXT-2");
        j2.setTitle("DevOps Engineer");
        j2.setCompanyName("CloudScale");
        j2.setCompanyWebsite("https://cloudscale.com");
        j2.setJobUrl("https://jobs.cloudscale.com/2");
        j2.setDescription("DevOps and container orchestration");
        j2.setLocation("Austin, TX");
        j2.setWorkMode(WorkMode.REMOTE);
        j2.setEmploymentType(EmploymentType.FULL_TIME);
        j2.setExperienceMinYears(BigDecimal.valueOf(4.0));
        j2.setExperienceMaxYears(BigDecimal.valueOf(8.0));
        j2.setSalaryMin(BigDecimal.valueOf(150000));
        j2.setSalaryMax(BigDecimal.valueOf(190000));
        j2.setSalaryCurrency("USD");
        j2.setSource(JobSource.COMPANY_CAREERS);
        j2.setSourceUrl("https://cloudscale.com/jobs/2");
        j2.setPostedAt(Instant.now());
        j2.setDiscoveredAt(Instant.now());
        j2.setApplicationDeadline(Instant.now().plus(30, ChronoUnit.DAYS));
        j2.setStatus(JobStatus.ACTIVE);
        j2.setDeduplicationHash("hash-2");
        j2.setCreatedAt(Instant.now());
        j2.setUpdatedAt(Instant.now());

        JobSkillJpaEntity js2 = new JobSkillJpaEntity(
            UUID.randomUUID(), job2, skillDocker, SkillRequirementType.REQUIRED, BigDecimal.valueOf(3.0), Instant.now()
        );
        j2.getSkills().add(js2);
        entityManager.persist(j2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Enforces strict tenant isolation across all analytical aggregations")
    void shouldEnforceStrictTenantIsolation() {
        // --- Populate Tenant User A Data ---
        UUID appA = UUID.randomUUID();
        JobApplicationJpaEntity applicationA = new JobApplicationJpaEntity();
        applicationA.setId(appA);
        applicationA.setUserId(tenantUserA);
        applicationA.setJobId(job1);
        applicationA.setStatus(ApplicationStatus.INTERVIEW);
        applicationA.setSubmissionSource(ApplicationSource.LINKEDIN);
        applicationA.setAppliedAt(Instant.now().minus(5, ChronoUnit.DAYS));
        applicationA.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));
        applicationA.setUpdatedAt(Instant.now());

        // Audit events for App A
        JobApplicationEventJpaEntity eventA1 = new JobApplicationEventJpaEntity();
        eventA1.setId(UUID.randomUUID());
        eventA1.setApplicationId(appA);
        eventA1.setNewStatus(ApplicationStatus.APPLIED);
        eventA1.setEventType(ApplicationEventType.CREATED);
        eventA1.setSource(EventSource.USER);
        eventA1.setOccurredAt(Instant.now().minus(5, ChronoUnit.DAYS));

        JobApplicationEventJpaEntity eventA2 = new JobApplicationEventJpaEntity();
        eventA2.setId(UUID.randomUUID());
        eventA2.setApplicationId(appA);
        eventA2.setPreviousStatus(ApplicationStatus.APPLIED);
        eventA2.setNewStatus(ApplicationStatus.INTERVIEW);
        eventA2.setEventType(ApplicationEventType.STATUS_CHANGED);
        eventA2.setSource(EventSource.USER);
        eventA2.setOccurredAt(Instant.now().minus(2, ChronoUnit.DAYS));

        applicationA.getEvents().add(eventA1);
        applicationA.getEvents().add(eventA2);
        entityManager.persist(applicationA);

        // Interview for User A
        InterviewJpaEntity interviewA = new InterviewJpaEntity();
        interviewA.setId(UUID.randomUUID());
        interviewA.setUserId(tenantUserA);
        interviewA.setJobId(job1);
        interviewA.setApplicationId(appA);
        interviewA.setRound(InterviewRound.TECHNICAL_SCREEN);
        interviewA.setRoundNumber(1);
        interviewA.setFormat(InterviewFormat.VIDEO_CALL);
        interviewA.setStatus(InterviewStatus.COMPLETED);
        interviewA.setOutcome(InterviewOutcome.PASSED);
        interviewA.setScheduledStartTime(Instant.now().minus(2, ChronoUnit.DAYS));
        interviewA.setScheduledEndTime(Instant.now().minus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
        interviewA.setTimeZone("UTC");
        interviewA.setCreatedAt(Instant.now().minus(3, ChronoUnit.DAYS));
        interviewA.setUpdatedAt(Instant.now());
        entityManager.persist(interviewA);

        // Assessment for User A: PASSED
        OnlineAssessmentJpaEntity assessmentA = new OnlineAssessmentJpaEntity();
        assessmentA.setId(UUID.randomUUID());
        assessmentA.setUserId(tenantUserA);
        assessmentA.setJobId(job1);
        assessmentA.setPlatform(AssessmentPlatform.HACKERRANK);
        assessmentA.setTitle("HackerRank Backend Algorithms");
        assessmentA.setStatus(AssessmentStatus.SUBMITTED);
        assessmentA.setResult(AssessmentResult.PASSED);
        assessmentA.setInvitedAt(Instant.now().minus(4, ChronoUnit.DAYS));
        assessmentA.setCreatedAt(Instant.now().minus(4, ChronoUnit.DAYS));
        assessmentA.setUpdatedAt(Instant.now());
        entityManager.persist(assessmentA);

        // User A verified skill: Java verified
        UserSkillJpaEntity userSkillA = new UserSkillJpaEntity(
            UUID.randomUUID(), tenantUserA, skillJava, SkillProficiency.ADVANCED,
            BigDecimal.valueOf(4.5), true, VerificationSource.ASSESSMENT, "Verified in HackerRank",
            Instant.now(), Instant.now(), Instant.now()
        );
        entityManager.persist(userSkillA);

        // --- Populate Tenant User B Data ---
        UUID appB = UUID.randomUUID();
        JobApplicationJpaEntity applicationB = new JobApplicationJpaEntity();
        applicationB.setId(appB);
        applicationB.setUserId(tenantUserB);
        applicationB.setJobId(job2);
        applicationB.setStatus(ApplicationStatus.REJECTED);
        applicationB.setSubmissionSource(ApplicationSource.REFERRAL);
        applicationB.setAppliedAt(Instant.now().minus(7, ChronoUnit.DAYS));
        applicationB.setCreatedAt(Instant.now().minus(7, ChronoUnit.DAYS));
        applicationB.setUpdatedAt(Instant.now());
        entityManager.persist(applicationB);

        // Assessment for User B: FAILED
        OnlineAssessmentJpaEntity assessmentB = new OnlineAssessmentJpaEntity();
        assessmentB.setId(UUID.randomUUID());
        assessmentB.setUserId(tenantUserB);
        assessmentB.setJobId(job2);
        assessmentB.setPlatform(AssessmentPlatform.CODESIGNAL);
        assessmentB.setTitle("CodeSignal General Coding Assessment");
        assessmentB.setStatus(AssessmentStatus.SUBMITTED);
        assessmentB.setResult(AssessmentResult.FAILED);
        assessmentB.setInvitedAt(Instant.now().minus(6, ChronoUnit.DAYS));
        assessmentB.setCreatedAt(Instant.now().minus(6, ChronoUnit.DAYS));
        assessmentB.setUpdatedAt(Instant.now());
        entityManager.persist(assessmentB);

        entityManager.flush();
        entityManager.clear();

        // --- Verify Tenant A Analytics ---
        AnalyticsOverview overviewA = analyticsService.getOverview(tenantUserA);
        assertThat(overviewA.totalApplications()).isEqualTo(1L);
        assertThat(overviewA.activePipelines()).isEqualTo(1L);
        assertThat(overviewA.interviewsCount()).isEqualTo(1L);
        assertThat(overviewA.assessmentsCount()).isEqualTo(1L);
        assertThat(overviewA.assessmentPassRatePercent()).isEqualTo(new BigDecimal("100.00")); // 1 passed, 0 failed
        assertThat(overviewA.rejectionsCount()).isEqualTo(0L); // Tenant B's rejection must not appear

        List<SourceEffectiveness> sourcesA = analyticsService.getSourceEffectiveness(tenantUserA);
        assertThat(sourcesA).hasSize(1);
        assertThat(sourcesA.get(0).source()).isEqualTo(ApplicationSource.LINKEDIN);
        assertThat(sourcesA.get(0).totalApplications()).isEqualTo(1L);

        List<SkillGapMetric> skillGapsA = analyticsService.getSkillGaps(tenantUserA);
        assertThat(skillGapsA).hasSize(1);
        assertThat(skillGapsA.get(0).skillName()).isEqualTo("Java");
        assertThat(skillGapsA.get(0).candidateVerified()).isTrue();

        // --- Verify Tenant B Analytics ---
        AnalyticsOverview overviewB = analyticsService.getOverview(tenantUserB);
        assertThat(overviewB.totalApplications()).isEqualTo(1L);
        assertThat(overviewB.activePipelines()).isEqualTo(0L); // REJECTED is not active
        assertThat(overviewB.interviewsCount()).isEqualTo(0L); // User A's interview must not appear
        assertThat(overviewB.rejectionsCount()).isEqualTo(1L);
        assertThat(overviewB.assessmentPassRatePercent()).isEqualTo(new BigDecimal("0.00")); // 0 passed, 1 failed

        List<SourceEffectiveness> sourcesB = analyticsService.getSourceEffectiveness(tenantUserB);
        assertThat(sourcesB).hasSize(1);
        assertThat(sourcesB.get(0).source()).isEqualTo(ApplicationSource.REFERRAL);

        List<SkillGapMetric> skillGapsB = analyticsService.getSkillGaps(tenantUserB);
        assertThat(skillGapsB).hasSize(1);
        assertThat(skillGapsB.get(0).skillName()).isEqualTo("Docker");
        assertThat(skillGapsB.get(0).candidateVerified()).isFalse(); // User B has no verified skills
    }
}
