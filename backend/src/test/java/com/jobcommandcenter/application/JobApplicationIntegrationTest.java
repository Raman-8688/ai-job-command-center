package com.jobcommandcenter.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.application.api.dto.*;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.EventSource;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.resume.domain.*;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobApplicationIntegrationTest {

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
    private ResumeRepository resumeRepository;

    @Autowired
    private TailoredResumeRepository tailoredResumeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User candidateUser;
    private String candidateToken;

    private User otherCandidate;
    private String otherToken;

    private Job testJob;
    private Job secondaryJob;
    private Resume testResume;
    private TailoredResume testTailoredResume;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM job_application_events");
        jdbcTemplate.execute("DELETE FROM job_applications");
        jdbcTemplate.execute("DELETE FROM emails");
        jdbcTemplate.execute("DELETE FROM email_connections");
        jdbcTemplate.execute("DELETE FROM tailored_resume_suggestions");
        jdbcTemplate.execute("DELETE FROM tailored_resumes");
        jdbcTemplate.execute("DELETE FROM resume_certifications");
        jdbcTemplate.execute("DELETE FROM resume_education");
        jdbcTemplate.execute("DELETE FROM resume_skills");
        jdbcTemplate.execute("DELETE FROM resume_projects");
        jdbcTemplate.execute("DELETE FROM resume_experiences");
        jdbcTemplate.execute("DELETE FROM resumes");
        jdbcTemplate.execute("DELETE FROM user_jobs");
        jdbcTemplate.execute("DELETE FROM job_skills");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM user_skills");
        jdbcTemplate.execute("DELETE FROM skills");
        jdbcTemplate.execute("DELETE FROM profile_target_roles");
        jdbcTemplate.execute("DELETE FROM profile_preferred_locations");
        jdbcTemplate.execute("DELETE FROM profiles");
        jdbcTemplate.execute("DELETE FROM users");

        candidateUser = User.createNew(
            "app.candidate@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Alex",
            "Taylor",
            "Alex T.",
            Role.USER
        );
        userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        otherCandidate = User.createNew(
            "app.other@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Jordan",
            "Casey",
            "Jordan C.",
            Role.USER
        );
        userRepository.save(otherCandidate);
        otherToken = jwtTokenService.generateToken(otherCandidate);

        testJob = Job.createNew(
            "ext-test-1",
            "Senior Backend Engineer",
            "Datadog",
            "https://datadoghq.com",
            "https://datadoghq.com/jobs/1",
            "Develop distributed metrics engine",
            "New York, NY",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new java.math.BigDecimal("5.0"),
            new java.math.BigDecimal("10.0"),
            new java.math.BigDecimal("150000"),
            new java.math.BigDecimal("190000"),
            "USD",
            JobSource.LINKEDIN,
            "https://datadoghq.com/jobs/1",
            Instant.now(),
            null
        );
        jobRepository.save(testJob);

        secondaryJob = Job.createNew(
            "ext-test-2",
            "Lead Cloud Architect",
            "Amazon",
            "https://amazon.jobs",
            "https://amazon.jobs/2",
            "Architect multi-cloud solutions",
            "Seattle, WA",
            WorkMode.HYBRID,
            EmploymentType.FULL_TIME,
            new java.math.BigDecimal("7.0"),
            new java.math.BigDecimal("12.0"),
            new java.math.BigDecimal("180000"),
            new java.math.BigDecimal("230000"),
            "USD",
            JobSource.INDEED,
            "https://amazon.jobs/2",
            Instant.now(),
            null
        );
        jobRepository.save(secondaryJob);

        testResume = Resume.create(
            candidateUser.getId(),
            "Alex Taylor - Master CV",
            "Senior Backend Engineer",
            "Experienced Java & Cloud Architect with 8+ years.",
            new java.math.BigDecimal("8.0"),
            "New York, NY",
            "alex.taylor@example.com",
            "+15550199"
        );
        resumeRepository.save(testResume);

        testTailoredResume = TailoredResume.createDraft(
            candidateUser.getId(),
            testResume.getId(),
            testJob.getId(),
            1,
            "Alex Taylor - Tailored for Datadog",
            "Specialized in backend metrics pipelines and high throughput.",
            new java.math.BigDecimal("0.90"),
            List.of("Java", "PostgreSQL"),
            List.of("Kubernetes"),
            List.of()
        );
        tailoredResumeRepository.save(testTailoredResume);
    }

    @Test
    @DisplayName("POST /api/applications creates new job application in DRAFT and returns 201")
    void createApplicationSuccess() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
            testJob.getId(),
            ApplicationStatus.DRAFT,
            ApplicationSource.LINKEDIN,
            null,
            "REQ-12345",
            "Drafting notes before sending",
            Instant.now().plus(7, ChronoUnit.DAYS),
            testResume.getId(),
            testTailoredResume.getId()
        );

        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.jobId").value(testJob.getId().toString()))
            .andExpect(jsonPath("$.jobTitle").value("Senior Backend Engineer"))
            .andExpect(jsonPath("$.companyName").value("Datadog"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.submissionSource").value("LINKEDIN"))
            .andExpect(jsonPath("$.externalReference").value("REQ-12345"))
            .andExpect(jsonPath("$.resumeId").value(testResume.getId().toString()))
            .andExpect(jsonPath("$.tailoredResumeId").value(testTailoredResume.getId().toString()))
            .andExpect(jsonPath("$.events", hasSize(1)))
            .andExpect(jsonPath("$.events[0].eventType").value("CREATED"));
    }

    @Test
    @DisplayName("POST /api/applications rejects duplicate application for same user and job with 409 Conflict")
    void createDuplicateApplicationThrowsConflict() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
            testJob.getId(),
            ApplicationStatus.DRAFT,
            ApplicationSource.COMPANY_WEBSITE,
            null,
            null,
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(containsString("already exists")));
    }

    @Test
    @DisplayName("GET /api/applications/{id} enforces multi-tenant security returning 404 for another user")
    void getApplicationEnforcesMultiTenantIsolation() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
            testJob.getId(),
            ApplicationStatus.APPLIED,
            ApplicationSource.LINKEDIN,
            Instant.now(),
            null,
            null,
            null,
            null,
            null
        );

        String responseBody = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JobApplicationResponse created = objectMapper.readValue(responseBody, JobApplicationResponse.class);

        // Candidate can view their own application
        mockMvc.perform(get("/api/applications/" + created.id())
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(created.id().toString()));

        // Other candidate receives 404
        mockMvc.perform(get("/api/applications/" + created.id())
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/applications/{id}/transition validates state transitions and records event timeline")
    void transitionStatusSuccessAndValidation() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
            testJob.getId(),
            ApplicationStatus.DRAFT,
            ApplicationSource.COMPANY_WEBSITE,
            null,
            null,
            null,
            null,
            null,
            null
        );

        String responseBody = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JobApplicationResponse created = objectMapper.readValue(responseBody, JobApplicationResponse.class);

        // 1. Invalid jump from DRAFT to INTERVIEW returns 400
        TransitionStatusRequest invalidReq = new TransitionStatusRequest(
            ApplicationStatus.INTERVIEW,
            "Illegal jump",
            EventSource.USER,
            null
        );
        mockMvc.perform(post("/api/applications/" + created.id() + "/transition")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value(containsString("Invalid application status transition from DRAFT to INTERVIEW")));

        // 2. Valid transition DRAFT -> APPLIED
        TransitionStatusRequest validReq1 = new TransitionStatusRequest(
            ApplicationStatus.APPLIED,
            "Submitted resume on career portal",
            EventSource.USER,
            null
        );
        mockMvc.perform(post("/api/applications/" + created.id() + "/transition")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReq1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPLIED"))
            .andExpect(jsonPath("$.appliedAt").isNotEmpty());

        // 3. Valid transition APPLIED -> SCREENING
        TransitionStatusRequest validReq2 = new TransitionStatusRequest(
            ApplicationStatus.SCREENING,
            "Recruiter email phone screen invite",
            EventSource.USER,
            null
        );
        mockMvc.perform(post("/api/applications/" + created.id() + "/transition")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validReq2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SCREENING"))
            .andExpect(jsonPath("$.events", hasSize(3)));
    }

    @Test
    @DisplayName("POST /api/applications/{id}/link-resume links user resume and rejects foreign resumes")
    void linkResumeChecksOwnership() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
            testJob.getId(),
            ApplicationStatus.DRAFT,
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            null,
            null,
            null
        );

        String responseBody = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JobApplicationResponse created = objectMapper.readValue(responseBody, JobApplicationResponse.class);

        // Linking legitimate resume succeeds
        LinkResumeRequest linkReq = new LinkResumeRequest(testResume.getId(), testTailoredResume.getId(), "Linked tailored version");
        mockMvc.perform(post("/api/applications/" + created.id() + "/link-resume")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumeId").value(testResume.getId().toString()))
            .andExpect(jsonPath("$.tailoredResumeId").value(testTailoredResume.getId().toString()));

        // Linking nonexistent or foreign resume returns 404
        LinkResumeRequest fakeReq = new LinkResumeRequest(UUID.randomUUID(), null, null);
        mockMvc.perform(post("/api/applications/" + created.id() + "/link-resume")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fakeReq)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/applications/dashboard-summary returns counts, active metrics and follow-ups due")
    void getDashboardSummary() throws Exception {
        // Create 1 draft on testJob
        CreateApplicationRequest req1 = new CreateApplicationRequest(
            testJob.getId(), ApplicationStatus.DRAFT, ApplicationSource.MANUAL, null, null, null, null, null, null
        );
        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isCreated());

        // Create 1 applied with follow-up due on secondaryJob
        CreateApplicationRequest req2 = new CreateApplicationRequest(
            secondaryJob.getId(),
            ApplicationStatus.APPLIED,
            ApplicationSource.LINKEDIN,
            Instant.now().minus(7, ChronoUnit.DAYS),
            null,
            null,
            Instant.now().minus(1, ChronoUnit.DAYS),
            null,
            null
        );
        mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications/dashboard-summary")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplicationsCount").value(2))
            .andExpect(jsonPath("$.activeApplicationsCount").value(2))
            .andExpect(jsonPath("$.followUpsDueCount").value(1))
            .andExpect(jsonPath("$.countsByStatus.DRAFT").value(1))
            .andExpect(jsonPath("$.countsByStatus.APPLIED").value(1));
    }

    @Test
    @DisplayName("GET /api/applications/{id}/guidance returns deterministic AI advisory next steps and email draft")
    void getGuidanceReturnsDeterministicAdvisory() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
            testJob.getId(),
            ApplicationStatus.APPLIED,
            ApplicationSource.COMPANY_WEBSITE,
            Instant.now().minus(5, ChronoUnit.DAYS),
            null,
            "Spoke with technical lead",
            null,
            null,
            null
        );

        String responseBody = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JobApplicationResponse created = objectMapper.readValue(responseBody, JobApplicationResponse.class);

        mockMvc.perform(get("/api/applications/" + created.id() + "/guidance")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recommendedAction").isNotEmpty())
            .andExpect(jsonPath("$.rationale").isNotEmpty())
            .andExpect(jsonPath("$.modelUsed").value("MockDeterministicAIProvider"));
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} allows deletion for DRAFT but forbids active INTERVIEW")
    void deleteApplicationLifecycleConstraints() throws Exception {
        CreateApplicationRequest request = new CreateApplicationRequest(
            testJob.getId(),
            ApplicationStatus.DRAFT,
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            null,
            null,
            null
        );

        String responseBody = mockMvc.perform(post("/api/applications")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        JobApplicationResponse created = objectMapper.readValue(responseBody, JobApplicationResponse.class);

        // Transition to APPLIED -> SCREENING -> INTERVIEW
        mockMvc.perform(post("/api/applications/" + created.id() + "/transition")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransitionStatusRequest(ApplicationStatus.APPLIED, null, EventSource.USER, null))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/applications/" + created.id() + "/transition")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransitionStatusRequest(ApplicationStatus.INTERVIEW, null, EventSource.USER, null))))
            .andExpect(status().isOk());

        // Deleting while in INTERVIEW is rejected with 400 Bad Request
        mockMvc.perform(delete("/api/applications/" + created.id())
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value(containsString("Only DRAFT or WITHDRAWN applications can be deleted")));

        // Transition to WITHDRAWN
        mockMvc.perform(post("/api/applications/" + created.id() + "/transition")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransitionStatusRequest(ApplicationStatus.WITHDRAWN, "Accepted another offer", EventSource.USER, null))))
            .andExpect(status().isOk());

        // Deleting when WITHDRAWN succeeds with 204 No Content
        mockMvc.perform(delete("/api/applications/" + created.id())
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNoContent());

        // Now application is gone
        mockMvc.perform(get("/api/applications/" + created.id())
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNotFound());
    }
}
