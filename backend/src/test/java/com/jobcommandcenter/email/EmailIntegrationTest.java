package com.jobcommandcenter.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.email.api.dto.GmailCallbackRequest;
import com.jobcommandcenter.email.api.dto.UpdateEmailClassificationRequest;
import com.jobcommandcenter.email.api.dto.UpdateProcessingStatusRequest;
import com.jobcommandcenter.email.domain.*;
import com.jobcommandcenter.job.domain.EmploymentType;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobRepository;
import com.jobcommandcenter.job.domain.JobSource;
import com.jobcommandcenter.job.domain.WorkMode;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Email & Gmail Integration End-to-End Integration Tests")
class EmailIntegrationTest {

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
    private EmailRepository emailRepository;

    @Autowired
    private EmailConnectionRepository emailConnectionRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User candidateUser;
    private String candidateToken;

    private User otherCandidate;
    private String otherToken;

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

        // Primary candidate
        candidateUser = User.createNew(
            "candidate.alex@jobcommandcenter.com",
            passwordEncoder.encode("Password123!"),
            "Alex",
            "Rivera",
            "Alex Rivera",
            Role.USER
        );
        candidateUser = userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        // Secondary candidate for tenant isolation
        otherCandidate = User.createNew(
            "other.dev@jobcommandcenter.com",
            passwordEncoder.encode("Password123!"),
            "Other",
            "Developer",
            "Other Dev",
            Role.USER
        );
        otherCandidate = userRepository.save(otherCandidate);
        otherToken = jwtTokenService.generateToken(otherCandidate);
    }


    @Test
    @DisplayName("Complete Gmail OAuth flow, Inbox synchronization, Classification, Extraction, and Job association")
    void testCompleteGmailWorkflow() throws Exception {
        // 1. Get Connect URL
        mockMvc.perform(get("/api/email/gmail/connect-url")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorizationUrl", containsString("accounts.google.com")))
            .andExpect(jsonPath("$.state", notNullValue()));

        // 2. Complete OAuth Callback
        GmailCallbackRequest callbackReq = new GmailCallbackRequest("mock-auth-code-12345", "mock-state");
        mockMvc.perform(post("/api/email/gmail/callback")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.provider", is("GMAIL")))
            .andExpect(jsonPath("$.connected", is(true)))
            .andExpect(jsonPath("$.emailAddress", is("candidate.alex@gmail.com")));

        // 3. Check Connection Status
        mockMvc.perform(get("/api/email/gmail/status")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected", is(true)))
            .andExpect(jsonPath("$.emailAddress", is("candidate.alex@gmail.com")));

        // 4. Trigger Email Sync
        mockMvc.perform(post("/api/email/sync")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.syncStatus", is("SUCCESS")))
            .andExpect(jsonPath("$.newMessagesSynced", greaterThan(0)));

        // 5. Query Sync Status
        mockMvc.perform(get("/api/email/sync/status")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected", is(true)))
            .andExpect(jsonPath("$.totalEmailsCount", greaterThan(0)));

        // 6. List Synced Emails
        mockMvc.perform(get("/api/emails")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements", greaterThan(0)))
            .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.content[0].subject", notNullValue()));

        // 7. Filter Emails by Classification (INTERVIEW_INVITATION)
        mockMvc.perform(get("/api/emails")
                .param("classification", "INTERVIEW_INVITATION")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.content[0].classification", is("INTERVIEW_INVITATION")));

        // Get an email ID from the database for subsequent test steps
        Email testEmail = emailRepository.search(new EmailSearchCriteria(candidateUser.getId(), null, null, null, null, 0, 10))
            .get(0);
        UUID emailId = testEmail.getId();

        // 8. Fetch Email Details
        mockMvc.perform(get("/api/emails/" + emailId)
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(emailId.toString())))
            .andExpect(jsonPath("$.sender", notNullValue()))
            .andExpect(jsonPath("$.bodyPlain", notNullValue()));

        // 9. Manually Override Classification
        UpdateEmailClassificationRequest updateClassReq = new UpdateEmailClassificationRequest(EmailClassification.ASSESSMENT);
        mockMvc.perform(patch("/api/emails/" + emailId + "/classification")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateClassReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.classification", is("ASSESSMENT")));

        // 10. Update Processing Status
        UpdateProcessingStatusRequest updateStatusReq = new UpdateProcessingStatusRequest(EmailProcessingStatus.PROCESSED);
        mockMvc.perform(patch("/api/emails/" + emailId + "/processing-status")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.processingStatus", is("PROCESSED")));

        // 11. Run Process Email (Extraction)
        mockMvc.perform(post("/api/emails/" + emailId + "/process")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.processingStatus", is("PROCESSED")))
            .andExpect(jsonPath("$.extractedCompanyName", notNullValue()));

        // 12. Create a Target Job to associate
        Job targetJob = Job.createNew(
            "JOB-99",
            "Senior Backend Engineer",
            "Amazon",
            "https://amazon.jobs",
            null,
            "Target job description",
            "Seattle, WA",
            WorkMode.HYBRID,
            EmploymentType.FULL_TIME,
            null, null, null, null, null,
            JobSource.MANUAL,
            null,
            Instant.now(),
            null
        );
        targetJob = jobRepository.save(targetJob);

        // 13. Associate Email with Job
        mockMvc.perform(post("/api/emails/" + emailId + "/associate-job/" + targetJob.getId())
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.associatedJobId", is(targetJob.getId().toString())));

        // 14. Query Emails for Job
        mockMvc.perform(get("/api/jobs/" + targetJob.getId() + "/emails")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(emailId.toString())));

        // 15. Disassociate Email from Job
        mockMvc.perform(delete("/api/emails/" + emailId + "/associate-job")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.associatedJobId", nullValue()));

        // 16. Create Job From Email
        mockMvc.perform(post("/api/emails/" + emailId + "/create-job")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.companyName", notNullValue()));

        // 17. Tenant Isolation Check: User B accessing User A's email returns 404
        mockMvc.perform(get("/api/emails/" + emailId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());

        // 18. Disconnect Gmail
        mockMvc.perform(post("/api/email/gmail/disconnect")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNoContent());

        // Verify status reflects disconnected
        mockMvc.perform(get("/api/email/gmail/status")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.connected", is(false)));
    }
}
