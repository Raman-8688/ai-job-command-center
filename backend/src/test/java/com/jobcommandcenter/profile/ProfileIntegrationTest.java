package com.jobcommandcenter.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.profile.api.UpdateProfileRequest;
import com.jobcommandcenter.profile.domain.WorkPreference;
import com.jobcommandcenter.user.domain.Role;
import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import com.jobcommandcenter.security.jwt.JwtTokenService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileIntegrationTest {

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

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM user_skills");
        jdbcTemplate.execute("DELETE FROM profile_target_roles");
        jdbcTemplate.execute("DELETE FROM profile_preferred_locations");
        jdbcTemplate.execute("DELETE FROM profiles");
        jdbcTemplate.execute("DELETE FROM users");

        testUser = User.createNew(
            "profile.candidate@jobcommandcenter.com",
            passwordEncoder.encode("SecretPass123!"),
            "Jordan",
            "Lee",
            "Jordan L.",
            Role.USER
        );
        userRepository.save(testUser);
        jwtToken = jwtTokenService.generateToken(testUser);
    }

    @Test
    @DisplayName("GET /api/profile returns or creates candidate profile for authenticated user")
    void getProfileReturnsCandidateProfile() throws Exception {
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(testUser.getId().toString()))
            .andExpect(jsonPath("$.workPreference").value("REMOTE"))
            .andExpect(jsonPath("$.yearsExperience").value(0.0));
    }

    @Test
    @DisplayName("PUT /api/profile updates professional candidate profile details")
    void updateProfileUpdatesCandidateDetails() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
            "+1-555-0199",
            "San Francisco, CA",
            "https://linkedin.com/in/jordanlee",
            "https://github.com/jordanlee",
            "https://jordanlee.dev",
            List.of("Staff Software Engineer", "Backend Lead"),
            List.of("Remote - US", "San Francisco, CA"),
            WorkPreference.HYBRID,
            new BigDecimal("8.5"),
            30,
            "TechCorp Global",
            "Senior Backend Engineer"
        );

        mockMvc.perform(put("/api/profile")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.phone").value("+1-555-0199"))
            .andExpect(jsonPath("$.location").value("San Francisco, CA"))
            .andExpect(jsonPath("$.linkedInUrl").value("https://linkedin.com/in/jordanlee"))
            .andExpect(jsonPath("$.targetRoles", hasItems("Staff Software Engineer", "Backend Lead")))
            .andExpect(jsonPath("$.preferredLocations", hasItems("Remote - US", "San Francisco, CA")))
            .andExpect(jsonPath("$.workPreference").value("HYBRID"))
            .andExpect(jsonPath("$.yearsExperience").value(8.5))
            .andExpect(jsonPath("$.noticePeriodDays").value(30))
            .andExpect(jsonPath("$.currentCompany").value("TechCorp Global"))
            .andExpect(jsonPath("$.currentDesignation").value("Senior Backend Engineer"));
    }
}
