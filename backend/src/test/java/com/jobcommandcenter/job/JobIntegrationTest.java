package com.jobcommandcenter.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.job.api.CreateJobRequest;
import com.jobcommandcenter.job.api.UpdateJobRequest;
import com.jobcommandcenter.job.api.UpdateUserJobStatusRequest;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.profile.api.UpdateProfileRequest;
import com.jobcommandcenter.profile.domain.WorkPreference;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

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

    private Skill javaSkill;
    private Skill springSkill;
    private Skill k8sSkill;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM job_application_events");
        jdbcTemplate.execute("DELETE FROM job_applications");
        jdbcTemplate.execute("DELETE FROM emails");
        jdbcTemplate.execute("DELETE FROM email_connections");
        jdbcTemplate.execute("DELETE FROM tailored_resume_suggestions");
        jdbcTemplate.execute("DELETE FROM tailored_resumes");
        jdbcTemplate.execute("DELETE FROM resume_skills");
        jdbcTemplate.execute("DELETE FROM resume_experiences");
        jdbcTemplate.execute("DELETE FROM resume_projects");
        jdbcTemplate.execute("DELETE FROM resume_education");
        jdbcTemplate.execute("DELETE FROM resume_certifications");
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

        // 1. Create candidate user
        candidateUser = User.createNew(
            "candidate.lead@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Morgan",
            "Taylor",
            "Morgan T.",
            Role.USER
        );
        userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        // 2. Populate skill catalog
        javaSkill = skillRepository.save(Skill.createNew("Java", SkillCategory.LANGUAGE));
        springSkill = skillRepository.save(Skill.createNew("Spring Boot", SkillCategory.FRAMEWORK));
        k8sSkill = skillRepository.save(Skill.createNew("Kubernetes", SkillCategory.DEVOPS));
    }

    @Test
    @DisplayName("POST /api/jobs creates job and enforces multi-level deduplication")
    void createJobAndEnforceDeduplication() throws Exception {
        CreateJobRequest request = new CreateJobRequest(
            "GH-1001",
            "Senior Backend Engineer",
            "Acme Innovations",
            "https://acme.com",
            "https://acme.com/jobs/1001",
            "We are seeking a Senior Backend Engineer to lead our cloud platform.",
            "San Francisco, CA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            new BigDecimal("9.0"),
            new BigDecimal("160000"),
            new BigDecimal("190000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://acme.com/jobs",
            Instant.now(),
            Instant.now().plusSeconds(86400 * 30),
            List.of(javaSkill.getId(), springSkill.getId()),
            List.of(k8sSkill.getId())
        );

        MvcResult result = mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.title").value("Senior Backend Engineer"))
            .andExpect(jsonPath("$.companyName").value("Acme Innovations"))
            .andExpect(jsonPath("$.skills", hasSize(3)))
            .andReturn();

        // 1. Attempt duplicate by (source, externalJobId)
        CreateJobRequest dupExtId = new CreateJobRequest(
            "GH-1001", "Different Title", "Another Company", null, null,
            "Description", null, null, null, null, null, null, null, null,
            JobSource.COMPANY_CAREERS, null, null, null, null, null
        );
        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dupExtId)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(containsString("external ID 'GH-1001'")));

        // 2. Attempt duplicate by canonical URL
        CreateJobRequest dupUrl = new CreateJobRequest(
            null, "Unique Title", "Different Co", null, "https://acme.com/jobs/1001",
            "Description", null, null, null, null, null, null, null, null,
            JobSource.MANUAL, null, null, null, null, null
        );
        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dupUrl)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(containsString("Posting with URL")));

        // 3. Attempt duplicate by company + title + location (normalized fingerprint)
        CreateJobRequest dupHash = new CreateJobRequest(
            null, "  senior backend engineer ", " ACME INNOVATIONS ", null, null,
            "New description", "san francisco, ca", null, null, null, null, null, null, null,
            JobSource.MANUAL, null, null, null, null, null
        );
        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dupHash)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(containsString("already exists")));
    }

    @Test
    @DisplayName("GET /api/jobs supports filtering by query, company, workMode and pagination")
    void searchJobsWithFiltersAndPagination() throws Exception {
        // Create 2 jobs
        CreateJobRequest job1 = new CreateJobRequest(
            null, "Staff Java Developer", "Google Cloud", null, null,
            "Distributed systems and Java microservices", "Mountain View, CA",
            WorkMode.HYBRID, EmploymentType.FULL_TIME, null, null, null, null, null,
            JobSource.MANUAL, null, null, null, null, null
        );
        CreateJobRequest job2 = new CreateJobRequest(
            null, "React Frontend Engineer", "Meta Platforms", null, null,
            "Web applications with TypeScript", "Menlo Park, CA",
            WorkMode.REMOTE, EmploymentType.FULL_TIME, null, null, null, null, null,
            JobSource.MANUAL, null, null, null, null, null
        );

        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(job2)))
            .andExpect(status().isCreated());

        // Filter by company = 'Google'
        mockMvc.perform(get("/api/jobs?company=Google")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].companyName").value("Google Cloud"));

        // Filter by workMode = 'REMOTE'
        mockMvc.perform(get("/api/jobs?workMode=REMOTE")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].title").value("React Frontend Engineer"));

        // Free-text query = 'microservices'
        mockMvc.perform(get("/api/jobs?query=microservices")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].companyName").value("Google Cloud"));
    }

    @Test
    @DisplayName("GET /api/jobs/{id}/match calculates explainable match score using verified candidate data")
    void calculateJobMatchWithVerifiedData() throws Exception {
        // 1. Setup candidate profile
        UpdateProfileRequest profileRequest = new UpdateProfileRequest(
            "+1-555-0100", "San Francisco, CA", null, null, null,
            List.of("Lead Java Engineer", "Backend Architect"),
            List.of("San Francisco, CA", "Remote"),
            WorkPreference.REMOTE,
            new BigDecimal("8.0"),
            30, "OldCo", "Senior Developer"
        );
        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileRequest)))
            .andExpect(status().isOk());

        // 2. Candidate has VERIFIED Java and Spring Boot, but UNVERIFIED Kubernetes
        userSkillRepository.save(UserSkill.createNew(
            candidateUser.getId(), javaSkill.getId(), SkillProficiency.EXPERT,
            new BigDecimal("8.0"), true, VerificationSource.USER_EXPLICIT, "Production"
        ));
        userSkillRepository.save(UserSkill.createNew(
            candidateUser.getId(), springSkill.getId(), SkillProficiency.ADVANCED,
            new BigDecimal("6.0"), true, VerificationSource.USER_EXPLICIT, "Production"
        ));
        // Anti-hallucination check: Unverified Kubernetes skill
        userSkillRepository.save(UserSkill.createNew(
            candidateUser.getId(), k8sSkill.getId(), SkillProficiency.INTERMEDIATE,
            new BigDecimal("2.0"), false, VerificationSource.AI_SUGGESTED, "AI suggestion"
        ));

        // 3. Post Job requiring Java, Spring Boot, and Kubernetes
        CreateJobRequest jobRequest = new CreateJobRequest(
            null, "Lead Java Engineer", "NextGen Fintech", null, null,
            "Build high-throughput payment systems", "San Francisco, CA",
            WorkMode.REMOTE, EmploymentType.FULL_TIME,
            new BigDecimal("6.0"), new BigDecimal("10.0"), null, null, null,
            JobSource.MANUAL, null, null, null,
            List.of(javaSkill.getId(), springSkill.getId(), k8sSkill.getId()),
            null
        );

        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        UUID jobId = UUID.fromString(
            objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asText()
        );

        // 4. Calculate match
        mockMvc.perform(get("/api/jobs/" + jobId + "/match")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").value(jobId.toString()))
            .andExpect(jsonPath("$.hasMissingRequiredSkills").value(true))
            // Crucial: Kubernetes was unverified, so it MUST appear in missingRequiredSkills!
            .andExpect(jsonPath("$.missingRequiredSkills", hasItem("Kubernetes")))
            .andExpect(jsonPath("$.matchedSkills", hasItems("Java", "Spring Boot")))
            .andExpect(jsonPath("$.titleScore").value(100))
            .andExpect(jsonPath("$.experienceScore").value(100))
            .andExpect(jsonPath("$.workModeScore").value(100))
            .andExpect(jsonPath("$.locationScore").value(100))
            .andExpect(jsonPath("$.matchReasons[0]").value(containsString("ATTENTION: Missing 1 required skill(s): [Kubernetes]")));
    }

    @Test
    @DisplayName("Candidate can track personal interaction status for a job")
    void candidateTracksUserJobStatus() throws Exception {
        CreateJobRequest jobRequest = new CreateJobRequest(
            null, "DevOps Engineer", "CloudScale", null, null,
            "Kubernetes and Terraform infrastructure", "Remote",
            WorkMode.REMOTE, EmploymentType.FULL_TIME,
            null, null, null, null, null, JobSource.MANUAL, null, null, null, null, null
        );

        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        UUID jobId = UUID.fromString(
            objectMapper.readTree(jobResult.getResponse().getContentAsString()).get("id").asText()
        );

        // Update candidate status to SHORTLISTED
        UpdateUserJobStatusRequest statusReq = new UpdateUserJobStatusRequest(
            UserJobStatus.SHORTLISTED,
            "Top compensation and strong remote engineering culture."
        );

        mockMvc.perform(put("/api/jobs/" + jobId + "/user-status")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusReq)))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Unauthenticated request to /api/jobs is rejected with 401")
    void unauthenticatedAccessIsRejected() throws Exception {
        mockMvc.perform(get("/api/jobs"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }
}
