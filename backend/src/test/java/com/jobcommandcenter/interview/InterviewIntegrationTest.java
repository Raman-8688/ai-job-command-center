package com.jobcommandcenter.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.application.domain.JobApplicationRepository;
import com.jobcommandcenter.interview.api.dto.*;
import com.jobcommandcenter.interview.domain.*;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.security.jwt.JwtTokenService;
import com.jobcommandcenter.skill.domain.*;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InterviewIntegrationTest {

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
    private SkillRepository skillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User candidateUser;
    private String candidateToken;

    private User otherCandidate;
    private String otherToken;

    private Job testJob;
    private JobApplication testApplication;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM interview_preparations");
        jdbcTemplate.execute("DELETE FROM interview_events");
        jdbcTemplate.execute("DELETE FROM interviews");
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
            "interview.candidate@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Maya",
            "Lin",
            "Maya L.",
            Role.USER
        );
        userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        otherCandidate = User.createNew(
            "other.candidate@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Sam",
            "Wilson",
            "Sam W.",
            Role.USER
        );
        userRepository.save(otherCandidate);
        otherToken = jwtTokenService.generateToken(otherCandidate);

        testJob = Job.createNew(
            "ext-stripe-1",
            "Senior Systems Engineer",
            "Stripe",
            "https://stripe.com",
            "https://stripe.com/jobs/1",
            "Build robust payments infrastructure in Java and distributed systems",
            "San Francisco, CA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new java.math.BigDecimal("5.0"),
            new java.math.BigDecimal("10.0"),
            new java.math.BigDecimal("180000"),
            new java.math.BigDecimal("240000"),
            "USD",
            JobSource.LINKEDIN,
            "https://stripe.com/jobs/1",
            Instant.now(),
            null
        );
        jobRepository.save(testJob);

        testApplication = JobApplication.createNew(
            candidateUser.getId(),
            testJob.getId(),
            ApplicationStatus.INTERVIEW,
            Instant.now(),
            ApplicationSource.LINKEDIN,
            "REQ-9900",
            null,
            null,
            "Passed recruiter phone screen"
        );
        jobApplicationRepository.save(testApplication);

        Skill javaSkill = Skill.createNew("Java", SkillCategory.LANGUAGE);
        skillRepository.save(javaSkill);
        UserSkill userSkill = UserSkill.createNew(
            candidateUser.getId(),
            javaSkill.getId(),
            SkillProficiency.EXPERT,
            new java.math.BigDecimal("5.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Verified from engineering work"
        );
        userSkillRepository.save(userSkill);
    }

    @Test
    @DisplayName("POST /api/interviews schedules interview and returns 201 Created")
    void scheduleInterviewSuccess() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(45, ChronoUnit.MINUTES);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(),
            testApplication.getId(),
            InterviewRound.TECHNICAL_SCREEN,
            1,
            InterviewFormat.VIDEO_CALL,
            start,
            end,
            "America/New_York",
            "https://zoom.us/j/123456789",
            "Zoom Call",
            "Alexandre Dumas",
            "Principal Infrastructure Engineer",
            "Focus on concurrency and distributed cache"
        );

        mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.jobTitle").value("Senior Systems Engineer"))
            .andExpect(jsonPath("$.companyName").value("Stripe"))
            .andExpect(jsonPath("$.round").value("TECHNICAL_SCREEN"))
            .andExpect(jsonPath("$.status").value("SCHEDULED"))
            .andExpect(jsonPath("$.interviewerNames").value("Alexandre Dumas"))
            .andExpect(jsonPath("$.events", hasSize(1)))
            .andExpect(jsonPath("$.events[0].eventType").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /api/interviews rejects start time after end time with 400 Bad Request")
    void scheduleInterviewInvalidTimeWindowThrowsBadRequest() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.minus(1, ChronoUnit.HOURS);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(),
            null,
            InterviewRound.INITIAL_SCREEN,
            1,
            InterviewFormat.PHONE_SCREEN,
            start,
            end,
            "UTC",
            null,
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value(containsString("Scheduled end time must be after scheduled start time")));
    }

    @Test
    @DisplayName("GET /api/interviews/{id} enforces multi-tenant isolation returning 404 for other users")
    void interviewMultiTenantIsolation() throws Exception {
        Instant start = Instant.now().plus(3, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(), null, InterviewRound.BEHAVIORAL_CULTURE, 1, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        String responseBody = mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        InterviewResponse created = objectMapper.readValue(responseBody, InterviewResponse.class);

        // Candidate can view their interview
        mockMvc.perform(get("/api/interviews/" + created.id())
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(created.id().toString()));

        // Other candidate receives 404
        mockMvc.perform(get("/api/interviews/" + created.id())
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/interviews/{id}/reschedule updates schedule and logs audit event")
    void rescheduleInterviewSuccess() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(), null, InterviewRound.SYSTEM_DESIGN, 2, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        String responseBody = mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        InterviewResponse created = objectMapper.readValue(responseBody, InterviewResponse.class);

        Instant newStart = start.plus(2, ChronoUnit.DAYS);
        Instant newEnd = newStart.plus(1, ChronoUnit.HOURS);

        RescheduleInterviewRequest rescheduleReq = new RescheduleInterviewRequest(
            newStart,
            newEnd,
            "America/Los_Angeles",
            "Candidate requested conflict resolution"
        );

        mockMvc.perform(post("/api/interviews/" + created.id() + "/reschedule")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rescheduleReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESCHEDULED"))
            .andExpect(jsonPath("$.timeZone").value("America/Los_Angeles"))
            .andExpect(jsonPath("$.events", hasSize(2)))
            .andExpect(jsonPath("$.events[1].eventType").value("RESCHEDULED"));
    }

    @Test
    @DisplayName("POST /api/interviews/{id}/status completes interview and forbids subsequent rescheduling")
    void completeInterviewLifecycleConstraints() throws Exception {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(), null, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        String responseBody = mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        InterviewResponse created = objectMapper.readValue(responseBody, InterviewResponse.class);

        // Mark as COMPLETED
        UpdateInterviewStatusRequest completeReq = new UpdateInterviewStatusRequest(
            InterviewStatus.COMPLETED,
            InterviewOutcome.PASSED,
            "Strong algorithm clarity and excellent questions for the interviewer",
            "Awaiting round 2 scheduling",
            "USER"
        );

        mockMvc.perform(post("/api/interviews/" + created.id() + "/status")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.outcome").value("PASSED"))
            .andExpect(jsonPath("$.candidateFeedback").isNotEmpty());

        // Attempting to reschedule completed interview throws 400 Bad Request
        RescheduleInterviewRequest reschedReq = new RescheduleInterviewRequest(
            start.plus(3, ChronoUnit.DAYS), start.plus(3, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS), "UTC", "Try reschedule"
        );
        mockMvc.perform(post("/api/interviews/" + created.id() + "/reschedule")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reschedReq)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value(containsString("Cannot reschedule an interview that is already COMPLETED")));

        // Attempting to delete completed interview throws 400 Bad Request
        mockMvc.perform(delete("/api/interviews/" + created.id())
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value(containsString("Completed interviews cannot be deleted")));
    }

    @Test
    @DisplayName("AI Preparation flow: generates questions, allows saving notes and mark reviewed")
    void aiInterviewPrepFlow() throws Exception {
        Instant start = Instant.now().plus(4, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(), null, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        String responseBody = mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        InterviewResponse created = objectMapper.readValue(responseBody, InterviewResponse.class);

        // 1. Generate AI Prep
        String prepResponse = mockMvc.perform(post("/api/interviews/" + created.id() + "/ai-prep")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.interviewId").value(created.id().toString()))
            .andExpect(jsonPath("$.jobTitle").value("Senior Systems Engineer"))
            .andExpect(jsonPath("$.readinessScore").isNumber())
            .andExpect(jsonPath("$.questions", hasSize(greaterThanOrEqualTo(3))))
            .andReturn().getResponse().getContentAsString();

        InterviewPrepBundleResponse bundle = objectMapper.readValue(prepResponse, InterviewPrepBundleResponse.class);
        UUID prepId = bundle.questions().get(0).id();

        // 2. Candidate updates practice notes and marks reviewed
        UpdatePrepNotesRequest notesRequest = new UpdatePrepNotesRequest(
            "My personal STAR response: discussed high throughput message queues with zero drop rate.",
            true
        );

        mockMvc.perform(put("/api/interviews/" + created.id() + "/prep/" + prepId)
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notesRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userAnswerNotes").value(containsString("zero drop rate")))
            .andExpect(jsonPath("$.isReviewed").value(true));

        // 3. Inspect updated bundle
        mockMvc.perform(get("/api/interviews/" + created.id() + "/prep")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questions[0].isReviewed").value(true));
    }

    @Test
    @DisplayName("GET /api/interviews/dashboard-summary returns aggregate metrics and counts")
    void getDashboardSummarySuccess() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(), null, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/interviews/dashboard-summary")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalInterviews").value(1))
            .andExpect(jsonPath("$.upcomingInterviews").value(1))
            .andExpect(jsonPath("$.countByRound.TECHNICAL_SCREEN").value(1))
            .andExpect(jsonPath("$.nextUpcomingInterview").isNotEmpty())
            .andExpect(jsonPath("$.overallReadinessScore").value(0));
    }

    @Test
    @DisplayName("GET /api/interviews/dashboard-summary calculates readiness score from reviewed questions")
    void getDashboardSummaryCalculatesReadinessFromReviewedQuestions() throws Exception {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            testJob.getId(), null, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        String interviewJson = mockMvc.perform(post("/api/interviews")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        InterviewResponse interview = objectMapper.readValue(interviewJson, InterviewResponse.class);

        // Generate prep: MockDeterministicAIProvider generates 4 questions
        String prepResponse = mockMvc.perform(post("/api/interviews/" + interview.id() + "/ai-prep")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        InterviewPrepBundleResponse bundle = objectMapper.readValue(prepResponse, InterviewPrepBundleResponse.class);
        assertEquals(4, bundle.questions().size());

        // Prior to reviewing, readiness is 0%
        mockMvc.perform(get("/api/interviews/dashboard-summary")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.overallReadinessScore").value(0));

        // Mark 2 of the 4 questions as reviewed (2/4 = 50%)
        mockMvc.perform(put("/api/interviews/" + interview.id() + "/prep/" + bundle.questions().get(0).id())
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdatePrepNotesRequest("Notes 1", true))))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/interviews/" + interview.id() + "/prep/" + bundle.questions().get(1).id())
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdatePrepNotesRequest("Notes 2", true))))
            .andExpect(status().isOk());

        // Dashboard readiness accurately reflects 50%
        mockMvc.perform(get("/api/interviews/dashboard-summary")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.overallReadinessScore").value(50));
    }
}
