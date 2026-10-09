package com.jobcommandcenter.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.security.jwt.JwtTokenService;
import com.jobcommandcenter.skill.api.AddUserSkillRequest;
import com.jobcommandcenter.skill.api.CreateSkillRequest;
import com.jobcommandcenter.skill.api.UpdateUserSkillRequest;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillProficiency;
import com.jobcommandcenter.skill.domain.VerificationSource;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SkillAndAntiHallucinationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private User candidateUser;
    private String candidateToken;

    private User otherCandidate;
    private String otherToken;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM job_application_events");
        jdbcTemplate.execute("DELETE FROM job_applications");
        jdbcTemplate.execute("DELETE FROM emails");
        jdbcTemplate.execute("DELETE FROM email_connections");
        jdbcTemplate.execute("DELETE FROM tailored_resume_suggestions");
        jdbcTemplate.execute("DELETE FROM tailored_resumes");
        jdbcTemplate.execute("DELETE FROM resume_skills");
        jdbcTemplate.execute("DELETE FROM job_skills");
        jdbcTemplate.execute("DELETE FROM user_skills");
        jdbcTemplate.execute("DELETE FROM skills");
        jdbcTemplate.execute("DELETE FROM profile_target_roles");
        jdbcTemplate.execute("DELETE FROM profile_preferred_locations");
        jdbcTemplate.execute("DELETE FROM profiles");
        jdbcTemplate.execute("DELETE FROM users");

        candidateUser = User.createNew(
            "dev.candidate@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Dev",
            "Candidate",
            "Dev C.",
            Role.USER
        );
        userRepository.save(candidateUser);
        candidateToken = jwtTokenService.generateToken(candidateUser);

        otherCandidate = User.createNew(
            "other.candidate@jobcommandcenter.com",
            passwordEncoder.encode("Pass123!"),
            "Other",
            "User",
            "Other U.",
            Role.USER
        );
        userRepository.save(otherCandidate);
        otherToken = jwtTokenService.generateToken(otherCandidate);
    }

    @Test
    @DisplayName("Catalog: POST /api/skills creates skill, rejects duplicate normalized name with 409")
    void catalogEnforcesNormalizedUniqueness() throws Exception {
        CreateSkillRequest createReq = new CreateSkillRequest("PostgreSQL", SkillCategory.DATABASE);

        mockMvc.perform(post("/api/skills")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("PostgreSQL"))
            .andExpect(jsonPath("$.normalizedName").value("postgresql"))
            .andExpect(jsonPath("$.category").value("DATABASE"));

        // Duplicate with different casing
        CreateSkillRequest duplicateReq = new CreateSkillRequest("  postgresql  ", SkillCategory.DATABASE);

        mockMvc.perform(post("/api/skills")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateReq)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Resource Conflict"))
            .andExpect(jsonPath("$.detail").value(containsString("already exists")));
    }

    @Test
    @DisplayName("Anti-Hallucination: AI_SUGGESTED skill claim is strictly initialized as UNVERIFIED")
    void aiSuggestedSkillMustRemainUnverifiedByDefault() throws Exception {
        // Create catalog skill
        CreateSkillRequest catalogReq = new CreateSkillRequest("Kubernetes", SkillCategory.DEVOPS);
        MvcResult catalogResult = mockMvc.perform(post("/api/skills")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogReq)))
            .andExpect(status().isCreated())
            .andReturn();

        UUID skillId = UUID.fromString(
            objectMapper.readTree(catalogResult.getResponse().getContentAsString()).get("id").asText()
        );

        // Attempt to add AI_SUGGESTED skill with verified = true
        AddUserSkillRequest addReq = new AddUserSkillRequest(
            skillId,
            SkillProficiency.INTERMEDIATE,
            new BigDecimal("2.0"),
            true, // maliciously or mistakenly claimed as verified
            VerificationSource.AI_SUGGESTED,
            "Extracted by resume parser"
        );

        MvcResult addResult = mockMvc.perform(post("/api/profile/skills")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.skillName").value("Kubernetes"))
            .andExpect(jsonPath("$.verified").value(false)) // Anti-hallucination guard enforced!
            .andExpect(jsonPath("$.verificationSource").value("AI_SUGGESTED"))
            .andExpect(jsonPath("$.lastVerifiedAt").doesNotExist())
            .andReturn();

        UUID userSkillId = UUID.fromString(
            objectMapper.readTree(addResult.getResponse().getContentAsString()).get("id").asText()
        );

        // Explicit user confirmation elevates to verified and USER_EXPLICIT
        UpdateUserSkillRequest updateReq = new UpdateUserSkillRequest(
            SkillProficiency.ADVANCED,
            new BigDecimal("3.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Confirmed by candidate via production experience"
        );

        mockMvc.perform(put("/api/profile/skills/" + userSkillId)
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").value(true))
            .andExpect(jsonPath("$.verificationSource").value("USER_EXPLICIT"))
            .andExpect(jsonPath("$.lastVerifiedAt").isNotEmpty())
            .andExpect(jsonPath("$.proficiency").value("ADVANCED"));
    }

    @Test
    @DisplayName("Candidate Skills: User cannot modify or delete another user's skill claim")
    void candidateSkillOwnershipIsEnforced() throws Exception {
        // Create catalog skill
        CreateSkillRequest catalogReq = new CreateSkillRequest("Docker", SkillCategory.DEVOPS);
        MvcResult catalogResult = mockMvc.perform(post("/api/skills")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogReq)))
            .andExpect(status().isCreated())
            .andReturn();

        UUID skillId = UUID.fromString(
            objectMapper.readTree(catalogResult.getResponse().getContentAsString()).get("id").asText()
        );

        // Candidate adds skill
        AddUserSkillRequest addReq = new AddUserSkillRequest(
            skillId,
            SkillProficiency.INTERMEDIATE,
            new BigDecimal("2.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Docker in daily workflow"
        );

        MvcResult addResult = mockMvc.perform(post("/api/profile/skills")
                .header("Authorization", "Bearer " + candidateToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReq)))
            .andExpect(status().isCreated())
            .andReturn();

        UUID userSkillId = UUID.fromString(
            objectMapper.readTree(addResult.getResponse().getContentAsString()).get("id").asText()
        );

        // Other candidate tries to update it -> must fail with 404
        UpdateUserSkillRequest updateReq = new UpdateUserSkillRequest(
            SkillProficiency.EXPERT,
            new BigDecimal("10.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Hijack attempt"
        );

        mockMvc.perform(put("/api/profile/skills/" + userSkillId)
                .header("Authorization", "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isNotFound());

        // Other candidate tries to delete it -> must fail with 404
        mockMvc.perform(delete("/api/profile/skills/" + userSkillId)
                .header("Authorization", "Bearer " + otherToken))
            .andExpect(status().isNotFound());

        // Owner deletes it -> returns 204 No Content
        mockMvc.perform(delete("/api/profile/skills/" + userSkillId)
                .header("Authorization", "Bearer " + candidateToken))
            .andExpect(status().isNoContent());
    }
}
