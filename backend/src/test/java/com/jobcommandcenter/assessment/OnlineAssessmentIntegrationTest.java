package com.jobcommandcenter.assessment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.application.domain.JobApplicationRepository;
import com.jobcommandcenter.assessment.api.dto.*;
import com.jobcommandcenter.assessment.domain.*;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.security.jwt.JwtTokenService;
import com.jobcommandcenter.user.domain.Role;
import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OnlineAssessmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User candidateUser;
    private String candidateToken;

    private User otherCandidate;
    private String otherToken;

    private Job testJob;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM online_assessment_checklists");
        jdbcTemplate.execute("DELETE FROM online_assessment_events");
        jdbcTemplate.execute("DELETE FROM online_assessments");
        jdbcTemplate.execute("DELETE FROM company_dossiers");
        jdbcTemplate.execute("DELETE FROM job_application_events");
        jdbcTemplate.execute("DELETE FROM job_applications");
        jdbcTemplate.execute("DELETE FROM job_skills");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM users");

        candidateUser = User.createNew(
            "candidate.oa@example.com",
            passwordEncoder.encode("Password123!"),
            "Candidate",
            "User",
            "Candidate U.",
            Role.USER
        );
        userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        otherCandidate = User.createNew(
            "other.oa@example.com",
            passwordEncoder.encode("Password123!"),
            "Other",
            "Candidate",
            "Other C.",
            Role.USER
        );
        userRepository.save(otherCandidate);
        otherToken = jwtTokenService.generateToken(otherCandidate);

        testJob = Job.createNew(
            "ext-meta-1",
            "Production Engineer",
            "Meta",
            "https://meta.com",
            "https://metacareers.com/1",
            "Systems programming in C++ and Linux kernel diagnostics",
            "Menlo Park, CA",
            WorkMode.HYBRID,
            EmploymentType.FULL_TIME,
            new BigDecimal("4.0"),
            new BigDecimal("8.0"),
            new BigDecimal("170000"),
            new BigDecimal("230000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://metacareers.com",
            Instant.now().minus(3, ChronoUnit.DAYS),
            Instant.now().plus(20, ChronoUnit.DAYS)
        );
        jobRepository.save(testJob);
    }

    @Test
    @DisplayName("Unauthenticated requests to assessment endpoints are rejected with 401")
    void unauthenticatedRequestsRejected() throws Exception {
        mockMvc.perform(get("/api/assessments"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/assessments"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/assessments/dashboard-summary"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/intel/jobs/" + testJob.getId() + "/company-dossier"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Complete Online Assessment lifecycle: create, get, start, briefing, checklist toggle, submit, result, and history")
    void completeAssessmentLifecycle() throws Exception {
        // 1. Create Assessment
        CreateAssessmentRequest createReq = new CreateAssessmentRequest(
            testJob.getId(),
            null,
            null,
            AssessmentPlatform.HACKERRANK,
            "Meta Production Engineering Screening",
            75,
            Instant.now(),
            Instant.now().plus(4, ChronoUnit.DAYS),
            null,
            "https://hackerrank.com/test-meta",
            "ACCESS99",
            "Initial preparation notes"
        );

        String createJson = mockMvc.perform(post("/api/assessments")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.platform", is("HACKERRANK")))
            .andExpect(jsonPath("$.status", is("INVITED")))
            .andExpect(jsonPath("$.result", is("PENDING")))
            .andExpect(jsonPath("$.durationMinutes", is(75)))
            .andExpect(jsonPath("$.jobTitle", is(testJob.getTitle())))
            .andExpect(jsonPath("$.companyName", is(testJob.getCompanyName())))
            .andReturn().getResponse().getContentAsString();

        OnlineAssessmentResponse created = objectMapper.readValue(createJson, OnlineAssessmentResponse.class);
        UUID assessmentId = created.id();

        // 2. List Assessments (should find 1)
        mockMvc.perform(get("/api/assessments")
                .header("Authorization", "Bearer " + candidateToken)
                .param("status", "INVITED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(assessmentId.toString())));

        // 3. Start Assessment
        mockMvc.perform(post("/api/assessments/" + assessmentId + "/start")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new StartAssessmentRequest(Instant.now(), "Started test"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // 4. Generate AI Briefing
        String briefingJson = mockMvc.perform(post("/api/assessments/" + assessmentId + "/briefing")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GenerateBriefingRequest("Please focus on timeout limits"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.platformGuidance", containsString("HackerRank")))
            .andExpect(jsonPath("$.timeManagementAdvice", containsString("75-minute assessment")))
            .andExpect(jsonPath("$.totalChecklistItems", greaterThanOrEqualTo(4)))
            .andExpect(jsonPath("$.checklist", hasSize(greaterThanOrEqualTo(4))))
            .andReturn().getResponse().getContentAsString();

        AssessmentBriefingResponse briefing = objectMapper.readValue(briefingJson, AssessmentBriefingResponse.class);
        UUID firstChecklistItemId = briefing.checklist().get(0).id();

        // 5. Toggle Checklist Item
        mockMvc.perform(patch("/api/assessments/" + assessmentId + "/checklist/" + firstChecklistItemId)
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completedItems", is(1)))
            .andExpect(jsonPath("$.completionPercentage", greaterThan(0)));

        // 6. Submit Assessment
        SubmitAssessmentRequest submitReq = new SubmitAssessmentRequest(
            new BigDecimal("92.5"), new BigDecimal("100.0"), "Submitted all sections", "https://github.com/my-solution", Instant.now()
        );

        mockMvc.perform(post("/api/assessments/" + assessmentId + "/submit")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("SUBMITTED")))
            .andExpect(jsonPath("$.score", is(92.5)));

        // 7. Record Evaluation Result
        RecordAssessmentResultRequest resultReq = new RecordAssessmentResultRequest(
            AssessmentResult.PASSED, new BigDecimal("92.5"), new BigDecimal("100.0"), "Recruiter confirmed pass"
        );

        mockMvc.perform(post("/api/assessments/" + assessmentId + "/result")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resultReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result", is("PASSED")));

        // 8. Retrieve Audit Event History
        mockMvc.perform(get("/api/assessments/" + assessmentId + "/events")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
            .andExpect(jsonPath("$[0].eventType", is("INVITED")));

        // 9. Dashboard Summary
        mockMvc.perform(get("/api/assessments/dashboard-summary")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAssessments", is(1)))
            .andExpect(jsonPath("$.submitted", is(1)));
    }

    @Test
    @DisplayName("Cross-tenant assessment isolation: other candidate cannot access or mutate resources")
    void crossTenantIsolationEnforced() throws Exception {
        CreateAssessmentRequest createReq = new CreateAssessmentRequest(
            testJob.getId(), null, null, AssessmentPlatform.LEETCODE, "Secret Assessment", 60, null, null, null, null, null, null
        );

        String createJson = mockMvc.perform(post("/api/assessments")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        OnlineAssessmentResponse created = objectMapper.readValue(createJson, OnlineAssessmentResponse.class);
        UUID assessmentId = created.id();

        // Other candidate attempting GET returns 404
        mockMvc.perform(get("/api/assessments/" + assessmentId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());

        // Other candidate attempting start returns 404
        mockMvc.perform(post("/api/assessments/" + assessmentId + "/start")
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());

        // Other candidate attempting list sees empty array
        mockMvc.perform(get("/api/assessments")
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Company dossier lifecycle: generate and retrieve via /api/intel/jobs/{jobId}/company-dossier")
    void companyDossierLifecycle() throws Exception {
        // 1. Initially 404 before generation
        mockMvc.perform(get("/api/intel/jobs/" + testJob.getId() + "/company-dossier")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNotFound());

        // 2. Generate dossier
        GenerateCompanyDossierRequest genReq = new GenerateCompanyDossierRequest("Meta operates massive hyperscale data centers");
        mockMvc.perform(post("/api/intel/jobs/" + testJob.getId() + "/company-dossier")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companyName", is("Meta")))
            .andExpect(jsonPath("$.companyTier", is("TIER_1_TECH")))
            .andExpect(jsonPath("$.overview", containsString("hyperscale data centers")))
            .andExpect(jsonPath("$.tailoredTalkingPoints", notNullValue()))
            .andExpect(jsonPath("$.interviewerQuestions", notNullValue()));

        // 3. Retrieve dossier
        mockMvc.perform(get("/api/intel/jobs/" + testJob.getId() + "/company-dossier")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.companyName", is("Meta")))
            .andExpect(jsonPath("$.companyTier", is("TIER_1_TECH")));

        // 4. Other user cannot retrieve candidate's dossier for this job
        mockMvc.perform(get("/api/intel/jobs/" + testJob.getId() + "/company-dossier")
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Invalid assessment payload returns 400 Bad Request with Problem Details validation errors")
    void invalidPayloadReturnsBadRequest() throws Exception {
        CreateAssessmentRequest invalidReq = new CreateAssessmentRequest(
            null, // missing jobId
            null,
            null,
            null, // missing platform
            "",   // blank title
            -10,  // invalid duration
            null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/assessments")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title", is("Validation Failed")))
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.validationErrors", hasSize(greaterThanOrEqualTo(3))));
    }
}
