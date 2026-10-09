package com.jobcommandcenter.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.resume.api.dto.*;
import com.jobcommandcenter.resume.domain.*;
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
class TailoredResumeIntegrationTest {

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
    private ResumeRepository resumeRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User primaryUser;
    private User secondaryUser;
    private String primaryToken;
    private String secondaryToken;
    private Job testJob;
    private Resume masterResume;
    private Skill javaSkill;
    private Skill postgresSkill;

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
        jdbcTemplate.execute("DELETE FROM resume_skills");
        jdbcTemplate.execute("DELETE FROM resume_experiences");
        jdbcTemplate.execute("DELETE FROM resume_projects");
        jdbcTemplate.execute("DELETE FROM resume_education");
        jdbcTemplate.execute("DELETE FROM resume_certifications");
        jdbcTemplate.execute("DELETE FROM resumes");
        jdbcTemplate.execute("DELETE FROM job_skills");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM user_skills");
        jdbcTemplate.execute("DELETE FROM skills");
        jdbcTemplate.execute("DELETE FROM users");

        primaryUser = User.createNew(
            "candidate.tailor@example.com",
            passwordEncoder.encode("Password123!"),
            "Alex",
            "Tailor",
            "Alex T.",
            Role.USER
        );
        userRepository.save(primaryUser);
        primaryToken = jwtTokenService.generateToken(primaryUser);

        secondaryUser = User.createNew(
            "intruder@example.com",
            passwordEncoder.encode("Password123!"),
            "Bob",
            "Intruder",
            "Bob I.",
            Role.USER
        );
        userRepository.save(secondaryUser);
        secondaryToken = jwtTokenService.generateToken(secondaryUser);

        javaSkill = skillRepository.save(Skill.createNew("Java", SkillCategory.LANGUAGE));
        postgresSkill = skillRepository.save(Skill.createNew("PostgreSQL", SkillCategory.DATABASE));

        // Candidate has verified Java skill
        userSkillRepository.save(UserSkill.createNew(
            primaryUser.getId(),
            javaSkill.getId(),
            SkillProficiency.EXPERT,
            new BigDecimal("5.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Verified"
        ));

        // Create Master Resume
        masterResume = Resume.create(
            primaryUser.getId(),
            "Master Engineering Resume",
            "Software Engineer",
            "Original generic summary",
            new BigDecimal("5.0"),
            "San Francisco, CA",
            "alex@example.com",
            "+1-555-0100"
        );
        masterResume.setExperiences(List.of(new ResumeExperience(
            UUID.randomUUID(),
            "Tech Corp",
            "Software Engineer",
            null,
            null,
            true,
            "Remote",
            "Building high-scale services",
            List.of("Increased throughput"),
            List.of("Java"),
            0
        )));
        resumeRepository.save(masterResume);

        // Create Target Job
        UUID jobId = UUID.randomUUID();
        testJob = new Job(
            jobId,
            "JOB-999",
            "Senior Java Architect",
            "Cloud Dynamics",
            "https://clouddynamics.example.com",
            "https://clouddynamics.example.com/careers/999",
            "Looking for Senior Java Architect with deep PostgreSQL experience.",
            "Remote",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            null,
            new BigDecimal("150000"),
            new BigDecimal("180000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://clouddynamics.example.com/careers",
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "hash999",
            Instant.now(),
            Instant.now(),
            List.of(
                JobSkill.create(jobId, javaSkill.getId(), SkillRequirementType.REQUIRED, BigDecimal.valueOf(4)),
                JobSkill.create(jobId, postgresSkill.getId(), SkillRequirementType.REQUIRED, BigDecimal.valueOf(3))
            )
        );
        jobRepository.save(testJob);
    }

    @Test
    @DisplayName("Complete Phase 6 Flow: Create tailored draft, verify versioning, apply suggestion, transition status and enforce security")
    void completeTailoringWorkflow() throws Exception {
        // 1. Initiate Tailoring for Job -> creates v1 draft
        MvcResult createResult = mockMvc.perform(post("/api/resumes/" + masterResume.getId() + "/tailor/" + testJob.getId())
                .header("Authorization", "Bearer " + primaryToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.sourceResumeId").value(masterResume.getId().toString()))
            .andExpect(jsonPath("$.targetJobId").value(testJob.getId().toString()))
            .andExpect(jsonPath("$.version").value(1))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.keywordCoverageScore").isNotEmpty())
            .andExpect(jsonPath("$.matchedKeywords", hasItem("Java")))
            .andExpect(jsonPath("$.suggestions", hasSize(greaterThan(0))))
            .andReturn();

        TailoredResumeResponse draft1 = objectMapper.readValue(createResult.getResponse().getContentAsString(), TailoredResumeResponse.class);

        // 2. Initiate Tailoring again -> creates v2 draft
        mockMvc.perform(post("/api/resumes/" + masterResume.getId() + "/tailor/" + testJob.getId())
                .header("Authorization", "Bearer " + primaryToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.version").value(2));

        // 3. List tailored resumes for resume -> returns 2 versions
        mockMvc.perform(get("/api/resumes/" + masterResume.getId() + "/tailored")
                .header("Authorization", "Bearer " + primaryToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].version").value(2))
            .andExpect(jsonPath("$[1].version").value(1));

        // 4. List tailored resumes for job -> returns 2 versions
        mockMvc.perform(get("/api/jobs/" + testJob.getId() + "/tailored-resumes")
                .header("Authorization", "Bearer " + primaryToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));

        // 5. Get details of draft1
        mockMvc.perform(get("/api/tailored-resumes/" + draft1.id())
                .header("Authorization", "Bearer " + primaryToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(draft1.id().toString()))
            .andExpect(jsonPath("$.version").value(1));

        // 6. Apply first suggestion (e.g. summary recommendation)
        TailoredResumeSuggestionDto summarySug = draft1.suggestions().stream()
            .filter(s -> s.sectionType() == SectionType.SUMMARY)
            .findFirst()
            .orElseThrow();

        mockMvc.perform(post("/api/tailored-resumes/" + draft1.id() + "/suggestions/" + summarySug.id() + "/apply")
                .header("Authorization", "Bearer " + primaryToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tailoredSummary").value(summarySug.suggestedContent()))
            .andExpect(jsonPath("$.suggestions[?(@.id == '" + summarySug.id() + "')].applied").value(true));

        // 7. Transition status from DRAFT -> UNDER_REVIEW -> APPROVED
        mockMvc.perform(patch("/api/tailored-resumes/" + draft1.id() + "/status")
                .header("Authorization", "Bearer " + primaryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateTailoredResumeStatusRequest(TailoredResumeStatus.UNDER_REVIEW))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        mockMvc.perform(patch("/api/tailored-resumes/" + draft1.id() + "/status")
                .header("Authorization", "Bearer " + primaryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateTailoredResumeStatusRequest(TailoredResumeStatus.APPROVED))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));

        // 8. Security & User Ownership: Secondary user cannot access or alter draft1
        mockMvc.perform(get("/api/tailored-resumes/" + draft1.id())
                .header("Authorization", "Bearer " + secondaryToken))
            .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/tailored-resumes/" + draft1.id() + "/status")
                .header("Authorization", "Bearer " + secondaryToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateTailoredResumeStatusRequest(TailoredResumeStatus.REJECTED))))
            .andExpect(status().isNotFound());
    }
}
