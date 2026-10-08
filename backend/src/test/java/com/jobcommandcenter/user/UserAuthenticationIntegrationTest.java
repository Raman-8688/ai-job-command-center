package com.jobcommandcenter.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobcommandcenter.user.api.LoginRequest;
import com.jobcommandcenter.user.domain.AccountStatus;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private User activeUser;
    private User lockedUser;
    private User inactiveUser;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM user_skills");
        jdbcTemplate.execute("DELETE FROM profile_target_roles");
        jdbcTemplate.execute("DELETE FROM profile_preferred_locations");
        jdbcTemplate.execute("DELETE FROM profiles");
        jdbcTemplate.execute("DELETE FROM users");

        String rawPassword = "SecurePassword123!";
        String encoded = passwordEncoder.encode(rawPassword);

        // Active user
        activeUser = User.createNew(
            "candidate@jobcommandcenter.com",
            encoded,
            "Alex",
            "Rivera",
            "Alex R.",
            Role.USER
        );
        userRepository.save(activeUser);

        // Locked user
        lockedUser = User.createNew(
            "locked@jobcommandcenter.com",
            encoded,
            "Locked",
            "User",
            "Locked",
            Role.USER
        );
        lockedUser.changeStatus(AccountStatus.LOCKED);
        userRepository.save(lockedUser);

        // Inactive user
        inactiveUser = User.createNew(
            "inactive@jobcommandcenter.com",
            encoded,
            "Inactive",
            "User",
            "Inactive",
            Role.USER
        );
        inactiveUser.changeStatus(AccountStatus.INACTIVE);
        userRepository.save(inactiveUser);
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200 and JWT token")
    void loginWithValidCredentialsReturnsJwtToken() throws Exception {
        LoginRequest request = new LoginRequest("candidate@jobcommandcenter.com", "SecurePassword123!");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresInMs").isNumber())
            .andExpect(jsonPath("$.userId").value(activeUser.getId().toString()))
            .andExpect(jsonPath("$.email").value("candidate@jobcommandcenter.com"))
            .andExpect(jsonPath("$.displayName").value("Alex R."))
            .andExpect(jsonPath("$.role").value("USER"))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("token");
    }

    @Test
    @DisplayName("POST /api/auth/login with incorrect password returns 401 Unauthorized")
    void loginWithWrongPasswordReturns401() throws Exception {
        LoginRequest request = new LoginRequest("candidate@jobcommandcenter.com", "WrongPassword999!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.detail").value(containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("POST /api/auth/login with locked account returns 401 Unauthorized")
    void loginWithLockedAccountReturns401() throws Exception {
        LoginRequest request = new LoginRequest("locked@jobcommandcenter.com", "SecurePassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value(containsString("locked")));
    }

    @Test
    @DisplayName("POST /api/auth/login with inactive account returns 401 Unauthorized")
    void loginWithInactiveAccountReturns401() throws Exception {
        LoginRequest request = new LoginRequest("inactive@jobcommandcenter.com", "SecurePassword123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value(containsString("inactive")));
    }

    @Test
    @DisplayName("GET /api/users/me with valid Bearer token returns 200 and user profile")
    void getCurrentUserWithValidJwtReturns200() throws Exception {
        // Authenticate first
        LoginRequest loginRequest = new LoginRequest("candidate@jobcommandcenter.com", "SecurePassword123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();

        // Access protected endpoint
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(activeUser.getId().toString()))
            .andExpect(jsonPath("$.email").value("candidate@jobcommandcenter.com"))
            .andExpect(jsonPath("$.firstName").value("Alex"))
            .andExpect(jsonPath("$.lastName").value("Rivera"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("GET /api/users/me without Authorization header returns 401 Unauthorized")
    void getCurrentUserWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401));
    }
}
