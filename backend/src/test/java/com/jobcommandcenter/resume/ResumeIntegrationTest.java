package com.jobcommandcenter.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.resume.api.dto.*;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeIntegrationTest {

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

    private User otherUser;
    private String otherToken;

    private Skill javaSkill;
    private Skill springSkill;
    private Skill redisSkill;
    private Skill dockerSkill;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM resume_certifications");
        jdbcTemplate.execute("DELETE FROM resume_education");
        jdbcTemplate.execute("DELETE FROM resume_skills");
        jdbcTemplate.execute("DELETE FROM resume_projects");
        jdbcTemplate.execute("DELETE FROM resume_experiences");
        jdbcTemplate.execute("DELETE FROM resumes");
        jdbcTemplate.execute("DELETE FROM job_ai_red_flags");
        jdbcTemplate.execute("DELETE FROM job_ai_requirements");
        jdbcTemplate.execute("DELETE FROM job_ai_technologies");
        jdbcTemplate.execute("DELETE FROM job_ai_responsibilities");
        jdbcTemplate.execute("DELETE FROM job_ai_analyses");
        jdbcTemplate.execute("DELETE FROM user_jobs");
        jdbcTemplate.execute("DELETE FROM job_skills");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM user_skills");
        jdbcTemplate.execute("DELETE FROM skills");
        jdbcTemplate.execute("DELETE FROM profile_target_roles");
        jdbcTemplate.execute("DELETE FROM profile_preferred_locations");
        jdbcTemplate.execute("DELETE FROM profiles");
        jdbcTemplate.execute("DELETE FROM users");

        // 1. Create candidate user A
        candidateUser = User.createNew(
            "resume.owner@example.com",
            passwordEncoder.encode("Pass123!"),
            "Jordan",
            "Lee",
            "Jordan L.",
            Role.USER
        );
        userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        // 2. Create other user B
        otherUser = User.createNew(
            "other.user@example.com",
            passwordEncoder.encode("Pass123!"),
            "Taylor",
            "Smith",
            "Taylor S.",
            Role.USER
        );
        userRepository.save(otherUser);
        otherToken = jwtTokenService.generateToken(otherUser);

        // 3. Skills Catalog
        javaSkill = skillRepository.save(Skill.createNew("Java", SkillCategory.LANGUAGE));
        springSkill = skillRepository.save(Skill.createNew("Spring Boot", SkillCategory.FRAMEWORK));
        redisSkill = skillRepository.save(Skill.createNew("Redis", SkillCategory.DATABASE));
        dockerSkill = skillRepository.save(Skill.createNew("Docker", SkillCategory.DEVOPS));
    }

    @Test
    @DisplayName("Resume endpoints enforce authentication")
    void endpointsEnforceAuthentication() throws Exception {
        mockMvc.perform(get("/api/resumes"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/resumes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Complete Resume CRUD, structured sections, activation, and archiving")
    void completeResumeCrudAndLifecycle() throws Exception {
        // 1. Create Resume (starts in DRAFT)
        CreateResumeRequest createReq = new CreateResumeRequest(
            "Principal Backend Resume",
            "Lead Backend Engineer",
            "10 years engineering distributed systems.",
            new BigDecimal("10.0"),
            "New York, NY",
            "jordan@example.com",
            "+1-555-0100"
        );

        MvcResult createResult = mockMvc.perform(post("/api/resumes")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Principal Backend Resume")))
            .andExpect(jsonPath("$.status", is("DRAFT")))
            .andReturn();

        ResumeResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), ResumeResponse.class);
        UUID resumeId = created.id();

        // 2. List Resumes
        mockMvc.perform(get("/api/resumes")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(resumeId.toString())));

        // 3. Update Resume with all structured sections
        UpdateResumeRequest updateReq = new UpdateResumeRequest(
            "Principal Backend Resume - Revised",
            "Staff Platform Architect",
            "Updated summary for high scale.",
            new BigDecimal("10.0"),
            "New York, NY",
            "jordan@example.com",
            "+1-555-0100",
            List.of(new ResumeExperienceDto(
                null,
                "MegaCorp",
                "Senior Staff Engineer",
                LocalDate.of(2019, 1, 1),
                null,
                true,
                "New York, NY",
                "Architected event-driven microservices.",
                List.of("Increased throughput 3x"),
                List.of("Java", "Spring Boot"),
                0
            )),
            List.of(new ResumeProjectDto(
                null,
                "Payments Core",
                "Distributed payments ledger",
                "Architect",
                List.of("Java", "PostgreSQL"),
                List.of("Designed ACID pipelines"),
                List.of("Zero lost transactions"),
                "2 years",
                "https://github.com/jordan/payments",
                0
            )),
            List.of(new ResumeSkillDto(
                null,
                javaSkill.getId(),
                "Java",
                SkillProficiency.EXPERT,
                new BigDecimal("10.0")
            )),
            List.of(new ResumeEducationDto(
                null,
                "Cornell University",
                "B.S.",
                "Computer Science",
                2010,
                2014,
                0
            )),
            List.of(new ResumeCertificationDto(
                null,
                "AWS Solutions Architect",
                "Amazon Web Services",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2025, 1, 1),
                "AWS-9999",
                null,
                0
            ))
        );

        mockMvc.perform(put("/api/resumes/" + resumeId)
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Principal Backend Resume - Revised")))
            .andExpect(jsonPath("$.experiences", hasSize(1)))
            .andExpect(jsonPath("$.projects", hasSize(1)))
            .andExpect(jsonPath("$.skills", hasSize(1)))
            .andExpect(jsonPath("$.education", hasSize(1)))
            .andExpect(jsonPath("$.certifications", hasSize(1)));

        // 4. Activate Resume
        mockMvc.perform(post("/api/resumes/" + resumeId + "/activate")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("ACTIVE")));

        // 5. Archive Resume
        mockMvc.perform(post("/api/resumes/" + resumeId + "/archive")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("ARCHIVED")));

        // 6. Delete Resume
        mockMvc.perform(delete("/api/resumes/" + resumeId)
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNoContent());

        // Verify gone
        mockMvc.perform(get("/api/resumes/" + resumeId)
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Strict user ownership isolation: User B cannot access User A's resume")
    void ownershipIsolationEnforced() throws Exception {
        // User A creates resume
        CreateResumeRequest createReq = new CreateResumeRequest(
            "User A Private Resume",
            "Developer",
            "Summary",
            new BigDecimal("3.0"),
            "Austin",
            null,
            null
        );

        MvcResult result = mockMvc.perform(post("/api/resumes")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andReturn();

        ResumeResponse created = objectMapper.readValue(result.getResponse().getContentAsString(), ResumeResponse.class);
        UUID resumeId = created.id();

        // User B tries to read User A's resume -> 404
        mockMvc.perform(get("/api/resumes/" + resumeId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());

        // User B tries to update User A's resume -> 404
        UpdateResumeRequest updateReq = new UpdateResumeRequest(
            "Tampered", "Tampered", "Tampered", BigDecimal.ONE, null, null, null, null, null, null, null, null
        );
        mockMvc.perform(put("/api/resumes/" + resumeId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isNotFound());

        // User B tries to delete User A's resume -> 404
        mockMvc.perform(delete("/api/resumes/" + resumeId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Job-specific resume analysis compares resume, job, and verified skills")
    void jobSpecificResumeAnalysis() throws Exception {
        // 1. Candidate has verified skills: Java, Spring Boot, and REDIS!
        UserSkill uJava = UserSkill.createNew(candidateUser.getId(), javaSkill.getId(), SkillProficiency.EXPERT, new BigDecimal("8.0"), true, VerificationSource.USER_EXPLICIT, "V");
        UserSkill uSpring = UserSkill.createNew(candidateUser.getId(), springSkill.getId(), SkillProficiency.EXPERT, new BigDecimal("6.0"), true, VerificationSource.USER_EXPLICIT, "V");
        UserSkill uRedis = UserSkill.createNew(candidateUser.getId(), redisSkill.getId(), SkillProficiency.ADVANCED, new BigDecimal("4.0"), true, VerificationSource.USER_EXPLICIT, "V");
        userSkillRepository.save(uJava);
        userSkillRepository.save(uSpring);
        userSkillRepository.save(uRedis);

        // 2. Candidate creates a resume that mentions Java and Spring Boot, but OMITTED Redis!
        CreateResumeRequest resumeReq = new CreateResumeRequest(
            "Targeted Cloud Resume",
            "Senior Java Backend Engineer",
            "Experienced Java backend engineer.",
            new BigDecimal("6.0"),
            "Remote",
            "jordan@example.com",
            null
        );
        MvcResult resResult = mockMvc.perform(post("/api/resumes")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resumeReq)))
            .andExpect(status().isCreated())
            .andReturn();
        ResumeResponse resume = objectMapper.readValue(resResult.getResponse().getContentAsString(), ResumeResponse.class);

        // Add Java and Spring Boot to the resume
        UpdateResumeRequest updateReq = new UpdateResumeRequest(
            resume.name(),
            resume.title(),
            resume.summary(),
            resume.yearsOfExperience(),
            resume.location(),
            resume.contactEmail(),
            resume.contactPhone(),
            List.of(new ResumeExperienceDto(
                null,
                "Cloud Solutions",
                "Senior Backend Engineer",
                LocalDate.of(2020, 1, 1),
                null,
                true,
                "Remote",
                "Engineered high performance backend systems using Java and Spring Boot.",
                List.of("Scale achieved 50k req/s"),
                List.of("Java", "Spring Boot"),
                0
            )),
            List.of(),
            List.of(
                new ResumeSkillDto(null, javaSkill.getId(), "Java", SkillProficiency.EXPERT, new BigDecimal("6.0")),
                new ResumeSkillDto(null, springSkill.getId(), "Spring Boot", SkillProficiency.ADVANCED, new BigDecimal("4.0"))
            ),
            List.of(),
            List.of()
        );
        mockMvc.perform(put("/api/resumes/" + resume.id())
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk());

        // 3. Create target Job: requires Java, Spring Boot, and Docker
        UUID jobId = UUID.randomUUID();
        Job job = new Job(
            jobId,
            "JOB-900",
            "Senior Platform Engineer",
            "Acme Cloud",
            "https://acme.cloud",
            "https://acme.cloud/careers/900",
            "We require Java and Spring Boot. Docker is required for microservices.",
            "Remote",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            null,
            new BigDecimal("150000"),
            new BigDecimal("180000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://acme.cloud/careers",
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "hash900",
            Instant.now(),
            Instant.now(),
            List.of(
                JobSkill.create(jobId, javaSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("5.0")),
                JobSkill.create(jobId, springSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("3.0")),
                JobSkill.create(jobId, dockerSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("2.0"))
            )
        );
        jobRepository.save(job);

        // 4. Perform job-specific resume analysis (GET)
        mockMvc.perform(get("/api/resumes/" + resume.id() + "/jobs/" + jobId + "/analysis")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumeId", is(resume.id().toString())))
            .andExpect(jsonPath("$.jobId", is(jobId.toString())))
            .andExpect(jsonPath("$.strongMatches", hasItems("Java", "Spring Boot")))
            .andExpect(jsonPath("$.missingRequiredSkills", hasItem("Docker")))
            .andExpect(jsonPath("$.verifiedSkillsMissingFromResume", hasItem("Redis")))
            .andExpect(jsonPath("$.resumeEvidence", not(empty())))
            .andExpect(jsonPath("$.experienceAlignment.meetsExperienceRequirement", is(true)))
            .andExpect(jsonPath("$.aiAssessment", not(emptyOrNullString())))
            .andExpect(jsonPath("$.improvementSuggestions", not(empty())));

        // 5. Perform job-specific resume analysis (POST)
        mockMvc.perform(post("/api/resumes/" + resume.id() + "/jobs/" + jobId + "/analysis")
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resumeId", is(resume.id().toString())))
            .andExpect(jsonPath("$.strongMatches", hasItems("Java", "Spring Boot")));
    }
}
