package com.jobcommandcenter.analytics.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
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
import com.jobcommandcenter.security.jwt.JwtTokenService;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillProficiency;
import com.jobcommandcenter.skill.domain.VerificationSource;
import com.jobcommandcenter.skill.infrastructure.SkillJpaEntity;
import com.jobcommandcenter.skill.infrastructure.UserSkillJpaEntity;
import com.jobcommandcenter.user.domain.AccountStatus;
import com.jobcommandcenter.user.domain.Role;
import com.jobcommandcenter.user.infrastructure.UserJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Analytics REST End-to-End Security & Tenant Isolation Integration Tests")
class AnalyticsEndToEndSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userAId;
    private UUID userBId;
    private UUID emptyUserId;
    private String tokenA;
    private String tokenB;
    private String tokenEmptyUser;

    private UUID jobAId;
    private UUID jobBId;
    private UUID skillJavaId;
    private UUID skillRustId;

    @BeforeEach
    void setUp() {
        userAId = UUID.randomUUID();
        userBId = UUID.randomUUID();
        emptyUserId = UUID.randomUUID();

        // 1. Create Users
        UserJpaEntity userAEntity = new UserJpaEntity(
            userAId, "usera@example.com", "hashA", "User", "A", "User A",
            AccountStatus.ACTIVE, Role.USER, Instant.now(), Instant.now(), null
        );
        entityManager.persist(userAEntity);

        UserJpaEntity userBEntity = new UserJpaEntity(
            userBId, "userb@example.com", "hashB", "User", "B", "User B",
            AccountStatus.ACTIVE, Role.USER, Instant.now(), Instant.now(), null
        );
        entityManager.persist(userBEntity);

        UserJpaEntity emptyUserEntity = new UserJpaEntity(
            emptyUserId, "empty@example.com", "hashEmpty", "Empty", "User", "Empty User",
            AccountStatus.ACTIVE, Role.USER, Instant.now(), Instant.now(), null
        );
        entityManager.persist(emptyUserEntity);

        com.jobcommandcenter.user.domain.User domainUserA = new com.jobcommandcenter.user.domain.User(
            userAId, "usera@example.com", "hashA", "User", "A", "User A",
            AccountStatus.ACTIVE, Role.USER, Instant.now(), Instant.now(), null
        );
        tokenA = jwtTokenService.generateToken(domainUserA);

        com.jobcommandcenter.user.domain.User domainUserB = new com.jobcommandcenter.user.domain.User(
            userBId, "userb@example.com", "hashB", "User", "B", "User B",
            AccountStatus.ACTIVE, Role.USER, Instant.now(), Instant.now(), null
        );
        tokenB = jwtTokenService.generateToken(domainUserB);

        com.jobcommandcenter.user.domain.User domainUserEmpty = new com.jobcommandcenter.user.domain.User(
            emptyUserId, "empty@example.com", "hashEmpty", "Empty", "User", "Empty User",
            AccountStatus.ACTIVE, Role.USER, Instant.now(), Instant.now(), null
        );
        tokenEmptyUser = jwtTokenService.generateToken(domainUserEmpty);

        // 2. Create Skills
        skillJavaId = UUID.randomUUID();
        SkillJpaEntity javaSkill = new SkillJpaEntity(
            skillJavaId, "Java", "java-" + UUID.randomUUID(), SkillCategory.LANGUAGE, Instant.now(), Instant.now()
        );
        entityManager.persist(javaSkill);

        skillRustId = UUID.randomUUID();
        SkillJpaEntity rustSkill = new SkillJpaEntity(
            skillRustId, "Rust", "rust-" + UUID.randomUUID(), SkillCategory.LANGUAGE, Instant.now(), Instant.now()
        );
        entityManager.persist(rustSkill);

        // 3. User A has verified Java skill
        UserSkillJpaEntity userASkill = new UserSkillJpaEntity(
            UUID.randomUUID(), userAId, skillJavaId, SkillProficiency.ADVANCED,
            BigDecimal.valueOf(4.5), true, VerificationSource.ASSESSMENT, "Verified in HackerRank",
            Instant.now(), Instant.now(), Instant.now()
        );
        entityManager.persist(userASkill);

        // 4. Create Jobs
        jobAId = UUID.randomUUID();
        JobJpaEntity jobA = new JobJpaEntity();
        jobA.setId(jobAId);
        jobA.setExternalJobId("EXT-A");
        jobA.setTitle("Senior Java Architect");
        jobA.setCompanyName("Alpha Corp");
        jobA.setCompanyWebsite("https://alphacorp.com");
        jobA.setJobUrl("https://jobs.alphacorp.com/1");
        jobA.setDescription("Java enterprise architecture");
        jobA.setLocation("New York, NY");
        jobA.setWorkMode(WorkMode.REMOTE);
        jobA.setEmploymentType(EmploymentType.FULL_TIME);
        jobA.setExperienceMinYears(BigDecimal.valueOf(5.0));
        jobA.setExperienceMaxYears(BigDecimal.valueOf(10.0));
        jobA.setSalaryMin(BigDecimal.valueOf(160000));
        jobA.setSalaryMax(BigDecimal.valueOf(200000));
        jobA.setSalaryCurrency("USD");
        jobA.setSource(JobSource.LINKEDIN);
        jobA.setSourceUrl("https://linkedin.com/jobs/alpha");
        jobA.setPostedAt(Instant.now());
        jobA.setDiscoveredAt(Instant.now());
        jobA.setApplicationDeadline(Instant.now().plus(30, ChronoUnit.DAYS));
        jobA.setStatus(JobStatus.ACTIVE);
        jobA.setDeduplicationHash("hash-job-a-" + UUID.randomUUID());
        jobA.setCreatedAt(Instant.now());
        jobA.setUpdatedAt(Instant.now());

        JobSkillJpaEntity jsA = new JobSkillJpaEntity(
            UUID.randomUUID(), jobAId, skillJavaId, SkillRequirementType.REQUIRED, BigDecimal.valueOf(5.0), Instant.now()
        );
        jobA.getSkills().add(jsA);
        entityManager.persist(jobA);

        jobBId = UUID.randomUUID();
        JobJpaEntity jobB = new JobJpaEntity();
        jobB.setId(jobBId);
        jobB.setExternalJobId("EXT-B");
        jobB.setTitle("Rust Systems Engineer");
        jobB.setCompanyName("Beta Systems");
        jobB.setCompanyWebsite("https://betasystems.com");
        jobB.setJobUrl("https://jobs.betasystems.com/2");
        jobB.setDescription("Low latency rust systems");
        jobB.setLocation("Remote");
        jobB.setWorkMode(WorkMode.REMOTE);
        jobB.setEmploymentType(EmploymentType.FULL_TIME);
        jobB.setExperienceMinYears(BigDecimal.valueOf(3.0));
        jobB.setExperienceMaxYears(BigDecimal.valueOf(6.0));
        jobB.setSalaryMin(BigDecimal.valueOf(150000));
        jobB.setSalaryMax(BigDecimal.valueOf(190000));
        jobB.setSalaryCurrency("USD");
        jobB.setSource(JobSource.COMPANY_CAREERS);
        jobB.setSourceUrl("https://betasystems.com/jobs/beta");
        jobB.setPostedAt(Instant.now());
        jobB.setDiscoveredAt(Instant.now());
        jobB.setApplicationDeadline(Instant.now().plus(30, ChronoUnit.DAYS));
        jobB.setStatus(JobStatus.ACTIVE);
        jobB.setDeduplicationHash("hash-job-b-" + UUID.randomUUID());
        jobB.setCreatedAt(Instant.now());
        jobB.setUpdatedAt(Instant.now());

        JobSkillJpaEntity jsB = new JobSkillJpaEntity(
            UUID.randomUUID(), jobBId, skillRustId, SkillRequirementType.REQUIRED, BigDecimal.valueOf(3.0), Instant.now()
        );
        jobB.getSkills().add(jsB);
        entityManager.persist(jobB);

        // 5. Populate User A Application (LINKEDIN, INTERVIEW)
        UUID appA = UUID.randomUUID();
        JobApplicationJpaEntity applicationA = new JobApplicationJpaEntity();
        applicationA.setId(appA);
        applicationA.setUserId(userAId);
        applicationA.setJobId(jobAId);
        applicationA.setStatus(ApplicationStatus.INTERVIEW);
        applicationA.setSubmissionSource(ApplicationSource.LINKEDIN);
        applicationA.setAppliedAt(Instant.now().minus(5, ChronoUnit.DAYS));
        applicationA.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));
        applicationA.setUpdatedAt(Instant.now());
        applicationA.setVersion(0L);
        entityManager.persist(applicationA);

        InterviewJpaEntity interviewA = new InterviewJpaEntity();
        interviewA.setId(UUID.randomUUID());
        interviewA.setUserId(userAId);
        interviewA.setApplicationId(appA);
        interviewA.setJobId(jobAId);
        interviewA.setRound(InterviewRound.TECHNICAL_SCREEN);
        interviewA.setRoundNumber(1);
        interviewA.setStatus(InterviewStatus.COMPLETED);
        interviewA.setOutcome(InterviewOutcome.PASSED);
        interviewA.setFormat(InterviewFormat.VIDEO_CALL);
        interviewA.setScheduledStartTime(Instant.now().minus(2, ChronoUnit.DAYS));
        interviewA.setScheduledEndTime(Instant.now().minus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
        interviewA.setTimeZone("UTC");
        interviewA.setCreatedAt(Instant.now().minus(3, ChronoUnit.DAYS));
        interviewA.setUpdatedAt(Instant.now());
        interviewA.setVersion(0L);
        entityManager.persist(interviewA);

        // 6. Populate User B Application (REFERRAL, OFFER)
        UUID appB = UUID.randomUUID();
        JobApplicationJpaEntity applicationB = new JobApplicationJpaEntity();
        applicationB.setId(appB);
        applicationB.setUserId(userBId);
        applicationB.setJobId(jobBId);
        applicationB.setStatus(ApplicationStatus.OFFER);
        applicationB.setSubmissionSource(ApplicationSource.REFERRAL);
        applicationB.setAppliedAt(Instant.now().minus(3, ChronoUnit.DAYS));
        applicationB.setCreatedAt(Instant.now().minus(3, ChronoUnit.DAYS));
        applicationB.setUpdatedAt(Instant.now());
        applicationB.setVersion(0L);
        entityManager.persist(applicationB);

        OnlineAssessmentJpaEntity assessmentB = new OnlineAssessmentJpaEntity();
        assessmentB.setId(UUID.randomUUID());
        assessmentB.setUserId(userBId);
        assessmentB.setJobId(jobBId);
        assessmentB.setApplicationId(appB);
        assessmentB.setTitle("Rust Coding Assessment");
        assessmentB.setPlatform(AssessmentPlatform.HACKERRANK);
        assessmentB.setStatus(AssessmentStatus.SUBMITTED);
        assessmentB.setResult(AssessmentResult.PASSED);
        assessmentB.setScore(new BigDecimal("98.00"));
        assessmentB.setInvitedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        assessmentB.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        assessmentB.setUpdatedAt(Instant.now());
        assessmentB.setVersion(0L);
        entityManager.persist(assessmentB);

        entityManager.flush();
    }

    @Test
    @DisplayName("1 & 2. Rejects unauthenticated requests across all analytics endpoints with 401")
    void shouldRejectAllUnauthenticatedEndpoints() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/analytics/funnel")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/analytics/sources")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/analytics/skills")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/analytics/insights")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("3, 4, 9. Enforces strict tenant isolation across all analytics endpoints between User A and User B")
    void shouldEnforceStrictTenantIsolationBetweenUsers() throws Exception {
        // --- Verify User A Analytics ---
        mockMvc.perform(get("/api/analytics/overview")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplications").value(1))
            .andExpect(jsonPath("$.interviewsCount").value(1))
            .andExpect(jsonPath("$.activeOffers").value(0))
            .andExpect(jsonPath("$.assessmentsCount").value(0));

        mockMvc.perform(get("/api/analytics/sources")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].source").value("LINKEDIN"))
            .andExpect(jsonPath("$[0].totalApplications").value(1))
            .andExpect(jsonPath("$[0].interviewsReached").value(1))
            .andExpect(jsonPath("$[0].offersReceived").value(0));

        mockMvc.perform(get("/api/analytics/skills")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].skillName").value("Java"))
            .andExpect(jsonPath("$[0].candidateVerified").value(true));

        mockMvc.perform(post("/api/analytics/insights")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").exists())
            .andExpect(jsonPath("$.generatedAt").exists());

        // --- Verify User B Analytics (Completely isolated from User A) ---
        mockMvc.perform(get("/api/analytics/overview")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplications").value(1))
            .andExpect(jsonPath("$.interviewsCount").value(0))
            .andExpect(jsonPath("$.activeOffers").value(1))
            .andExpect(jsonPath("$.assessmentsCount").value(1));

        mockMvc.perform(get("/api/analytics/sources")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].source").value("REFERRAL"))
            .andExpect(jsonPath("$[0].totalApplications").value(1))
            .andExpect(jsonPath("$[0].offersReceived").value(1));

        mockMvc.perform(get("/api/analytics/skills")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].skillName").value("Rust"))
            .andExpect(jsonPath("$[0].candidateVerified").value(false));

        mockMvc.perform(post("/api/analytics/insights")
                .header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").exists())
            .andExpect(jsonPath("$.generatedAt").exists());
    }

    @Test
    @DisplayName("5. Returns zero-safe values and empty arrays for newly created user with no data")
    void shouldReturnSafeDefaultsForEmptyUser() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                .header("Authorization", "Bearer " + tokenEmptyUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplications").value(0))
            .andExpect(jsonPath("$.activePipelines").value(0))
            .andExpect(jsonPath("$.interviewsCount").value(0))
            .andExpect(jsonPath("$.assessmentsCount").value(0))
            .andExpect(jsonPath("$.activeOffers").value(0))
            .andExpect(jsonPath("$.rejectionsCount").value(0))
            .andExpect(jsonPath("$.interviewConversionRatePercent").value(0.00))
            .andExpect(jsonPath("$.assessmentPassRatePercent").value(0.00))
            .andExpect(jsonPath("$.offerRatePercent").value(0.00));

        mockMvc.perform(get("/api/analytics/funnel")
                .header("Authorization", "Bearer " + tokenEmptyUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplications").value(0))
            .andExpect(jsonPath("$.stageConversions", hasSize(0)));

        mockMvc.perform(get("/api/analytics/sources")
                .header("Authorization", "Bearer " + tokenEmptyUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/analytics/skills")
                .header("Authorization", "Bearer " + tokenEmptyUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(post("/api/analytics/insights")
                .header("Authorization", "Bearer " + tokenEmptyUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").exists())
            .andExpect(jsonPath("$.dataLimitations").isArray());
    }

    @Test
    @DisplayName("6 & 7. Handles unexpected body payloads and unsupported methods gracefully")
    void shouldHandleUnexpectedBodyAndInvalidMethods() throws Exception {
        // Arbitrary body ignored safely
        mockMvc.perform(post("/api/analytics/insights")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"arbitraryKey\":\"arbitraryValue\"}"))
            .andExpect(status().isOk());

        // HTTP method not supported returns 405 Method Not Allowed
        mockMvc.perform(delete("/api/analytics/overview")
                .header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("10. Guarantees read-only semantics: zero database mutations caused by analytics requests")
    void shouldNotMutateAnyDatabaseEntitiesDuringAnalyticsCalls() throws Exception {
        long appCountBefore = ((Number) entityManager.createQuery("SELECT count(a) FROM JobApplicationJpaEntity a").getSingleResult()).longValue();
        long interviewCountBefore = ((Number) entityManager.createQuery("SELECT count(i) FROM InterviewJpaEntity i").getSingleResult()).longValue();
        long assessmentCountBefore = ((Number) entityManager.createQuery("SELECT count(oa) FROM OnlineAssessmentJpaEntity oa").getSingleResult()).longValue();
        long userSkillCountBefore = ((Number) entityManager.createQuery("SELECT count(s) FROM UserSkillJpaEntity s").getSingleResult()).longValue();

        // Perform requests
        mockMvc.perform(get("/api/analytics/overview").header("Authorization", "Bearer " + tokenA)).andExpect(status().isOk());
        mockMvc.perform(get("/api/analytics/funnel").header("Authorization", "Bearer " + tokenA)).andExpect(status().isOk());
        mockMvc.perform(get("/api/analytics/sources").header("Authorization", "Bearer " + tokenA)).andExpect(status().isOk());
        mockMvc.perform(get("/api/analytics/skills").header("Authorization", "Bearer " + tokenA)).andExpect(status().isOk());
        mockMvc.perform(post("/api/analytics/insights").header("Authorization", "Bearer " + tokenA)).andExpect(status().isOk());

        entityManager.flush();

        long appCountAfter = ((Number) entityManager.createQuery("SELECT count(a) FROM JobApplicationJpaEntity a").getSingleResult()).longValue();
        long interviewCountAfter = ((Number) entityManager.createQuery("SELECT count(i) FROM InterviewJpaEntity i").getSingleResult()).longValue();
        long assessmentCountAfter = ((Number) entityManager.createQuery("SELECT count(oa) FROM OnlineAssessmentJpaEntity oa").getSingleResult()).longValue();
        long userSkillCountAfter = ((Number) entityManager.createQuery("SELECT count(s) FROM UserSkillJpaEntity s").getSingleResult()).longValue();

        assertThat(appCountAfter).isEqualTo(appCountBefore);
        assertThat(interviewCountAfter).isEqualTo(interviewCountBefore);
        assertThat(assessmentCountAfter).isEqualTo(assessmentCountBefore);
        assertThat(userSkillCountAfter).isEqualTo(userSkillCountBefore);
    }
}
