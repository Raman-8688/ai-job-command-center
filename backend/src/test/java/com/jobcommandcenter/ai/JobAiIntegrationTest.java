package com.jobcommandcenter.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.ai.api.dto.TriggerAiAnalysisRequestDto;
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
class JobAiIntegrationTest {

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
    private SkillRepository skillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    private User candidateUser;
    private String candidateToken;
    private Job testJob;
    private Skill javaSkill;
    private Skill springSkill;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM interview_preparations");
        jdbcTemplate.execute("DELETE FROM interview_events");
        jdbcTemplate.execute("DELETE FROM interviews");
        jdbcTemplate.execute("DELETE FROM job_application_events");
        jdbcTemplate.execute("DELETE FROM job_applications");
        jdbcTemplate.execute("DELETE FROM emails");
        jdbcTemplate.execute("DELETE FROM email_connections");
        jdbcTemplate.execute("DELETE FROM job_ai_red_flags");
        jdbcTemplate.execute("DELETE FROM job_ai_requirements");
        jdbcTemplate.execute("DELETE FROM job_ai_technologies");
        jdbcTemplate.execute("DELETE FROM job_ai_responsibilities");
        jdbcTemplate.execute("DELETE FROM job_ai_analyses");
        jdbcTemplate.execute("DELETE FROM user_jobs");
        jdbcTemplate.execute("DELETE FROM job_skills");
        jdbcTemplate.execute("DELETE FROM tailored_resume_suggestions");
        jdbcTemplate.execute("DELETE FROM tailored_resumes");
        jdbcTemplate.execute("DELETE FROM resume_skills");
        jdbcTemplate.execute("DELETE FROM resume_experiences");
        jdbcTemplate.execute("DELETE FROM resume_projects");
        jdbcTemplate.execute("DELETE FROM resume_education");
        jdbcTemplate.execute("DELETE FROM resume_certifications");
        jdbcTemplate.execute("DELETE FROM resumes");
        jdbcTemplate.execute("DELETE FROM user_skills");
        jdbcTemplate.execute("DELETE FROM skills");
        jdbcTemplate.execute("DELETE FROM profile_target_roles");
        jdbcTemplate.execute("DELETE FROM profile_preferred_locations");
        jdbcTemplate.execute("DELETE FROM profiles");
        jdbcTemplate.execute("DELETE FROM users");

        // 1. Candidate User
        candidateUser = User.createNew(
            "ai.candidate@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Alex",
            "Rivera",
            "Alex R.",
            Role.USER
        );
        userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        // 2. Verified Skills
        javaSkill = skillRepository.save(Skill.createNew("Java", SkillCategory.LANGUAGE));
        springSkill = skillRepository.save(Skill.createNew("Spring Boot", SkillCategory.FRAMEWORK));

        UserSkill userJava = UserSkill.createNew(
            candidateUser.getId(),
            javaSkill.getId(),
            SkillProficiency.EXPERT,
            new BigDecimal("6.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Verified in tests"
        );
        userSkillRepository.save(userJava);

        UserSkill userSpring = UserSkill.createNew(
            candidateUser.getId(),
            springSkill.getId(),
            SkillProficiency.ADVANCED,
            new BigDecimal("4.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Verified in tests"
        );
        userSkillRepository.save(userSpring);

        // 3. Test Job
        UUID newJobId = UUID.randomUUID();
        JobSkill js1 = JobSkill.create(newJobId, javaSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("5.0"));
        JobSkill js2 = JobSkill.create(newJobId, springSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("3.0"));

        testJob = new Job(
            newJobId,
            "ROLE-500",
            "Senior Java Cloud Engineer",
            "Nexus Tech",
            "https://nexus.tech",
            "https://nexus.tech/careers/500",
            """
            Role Overview:
            We are hiring a Senior Java Cloud Engineer to design distributed microservices.
            
            Responsibilities:
            * Build mission-critical backend systems using Java and Spring Boot.
            * Architect relational schemas in PostgreSQL.
            * Containerize and deploy services to AWS using Docker and Kubernetes.
            
            Requirements:
            * 5+ years of software development experience with Java.
            * Strong hands-on experience with Spring Boot and PostgreSQL.
            * Docker, Kubernetes, and AWS experience required.
            * Bachelor's degree in Computer Science or equivalent.
            * Kafka knowledge is nice to have.
            """,
            "Seattle, WA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            null,
            new BigDecimal("160000"),
            new BigDecimal("200000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://nexus.tech/careers",
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "dedup_hash_500",
            Instant.now(),
            Instant.now(),
            List.of(js1, js2)
        );
        testJob = jobRepository.save(testJob);
    }

    @Test
    @DisplayName("AI endpoints enforce JWT authentication")
    void endpointsEnforceAuthentication() throws Exception {
        mockMvc.perform(post("/api/jobs/" + testJob.getId() + "/ai-analysis"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/jobs/" + testJob.getId() + "/ai-analysis"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/jobs/" + testJob.getId() + "/ai-analysis/history"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/jobs/" + testJob.getId() + "/ai-fit"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Complete AI Job Analysis lifecycle: analyze, version increment, history, and explainable fit")
    void completeAiAnalysisLifecycle() throws Exception {
        TriggerAiAnalysisRequestDto triggerReq = new TriggerAiAnalysisRequestDto("MOCK");

        // 1. Trigger first AI analysis (version 1)
        mockMvc.perform(post("/api/jobs/" + testJob.getId() + "/ai-analysis")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(triggerReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.jobId", is(testJob.getId().toString())))
            .andExpect(jsonPath("$.version", is(1)))
            .andExpect(jsonPath("$.status", is("COMPLETED")))
            .andExpect(jsonPath("$.provider", is("MOCK")))
            .andExpect(jsonPath("$.seniorityLevel", is("SENIOR")))
            .andExpect(jsonPath("$.technologies", hasSize(greaterThanOrEqualTo(3))))
            .andExpect(jsonPath("$.confidence", greaterThan(0.70)));

        // 2. Trigger second AI analysis (version 2)
        mockMvc.perform(post("/api/jobs/" + testJob.getId() + "/ai-analysis")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(triggerReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.jobId", is(testJob.getId().toString())))
            .andExpect(jsonPath("$.version", is(2)))
            .andExpect(jsonPath("$.status", is("COMPLETED")));

        // 3. Get latest analysis (should be version 2)
        mockMvc.perform(get("/api/jobs/" + testJob.getId() + "/ai-analysis")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version", is(2)))
            .andExpect(jsonPath("$.status", is("COMPLETED")));

        // 4. Get analysis history (should contain v2 and v1)
        mockMvc.perform(get("/api/jobs/" + testJob.getId() + "/ai-analysis/history")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].version", is(2)))
            .andExpect(jsonPath("$[1].version", is(1)));

        // 5. Evaluate explainable AI fit
        mockMvc.perform(get("/api/jobs/" + testJob.getId() + "/ai-fit")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId", is(testJob.getId().toString())))
            .andExpect(jsonPath("$.overallFitTier", notNullValue()))
            .andExpect(jsonPath("$.deterministicScore", greaterThan(0)))
            .andExpect(jsonPath("$.matchedTechnologies", hasItems("Java", "Spring Boot")))
            .andExpect(jsonPath("$.qualitativeSummary", not(emptyOrNullString())))
            .andExpect(jsonPath("$.interviewPreparationNotes", not(empty())))
            .andExpect(jsonPath("$.analysisVersion", is(2)));
    }

    @Test
    @DisplayName("GET /api/jobs/{id}/ai-analysis returns 404 when job does not exist")
    void getAnalysisReturns404ForNonExistentJob() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/jobs/" + nonExistentId + "/ai-analysis")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNotFound());
    }
}
