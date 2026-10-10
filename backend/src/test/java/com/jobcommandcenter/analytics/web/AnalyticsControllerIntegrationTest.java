package com.jobcommandcenter.analytics.web;

import com.jobcommandcenter.ai.domain.AIAnalyticsAdvisorResponse;
import com.jobcommandcenter.ai.domain.AdvisorBottleneck;
import com.jobcommandcenter.ai.domain.AdvisorRecommendation;
import com.jobcommandcenter.analytics.application.AnalyticsAiInsightService;
import com.jobcommandcenter.analytics.application.AnalyticsQueryService;
import com.jobcommandcenter.security.jwt.JwtTokenService;
import com.jobcommandcenter.security.jwt.SecurityUser;
import com.jobcommandcenter.user.domain.AccountStatus;
import com.jobcommandcenter.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AnalyticsController AI Insights Integration Tests")
class AnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private com.jobcommandcenter.user.domain.UserRepository userRepository;

    @MockBean
    private AnalyticsQueryService queryService;

    @MockBean
    private AnalyticsAiInsightService insightService;

    @Test
    @DisplayName("GET /api/analytics/overview rejects unauthenticated requests with 401")
    void shouldRejectUnauthenticatedOverviewRequest() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/analytics/funnel rejects unauthenticated requests with 401")
    void shouldRejectUnauthenticatedFunnelRequest() throws Exception {
        mockMvc.perform(get("/api/analytics/funnel"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/analytics/sources rejects unauthenticated requests with 401")
    void shouldRejectUnauthenticatedSourcesRequest() throws Exception {
        mockMvc.perform(get("/api/analytics/sources"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/analytics/skills rejects unauthenticated requests with 401")
    void shouldRejectUnauthenticatedSkillsRequest() throws Exception {
        mockMvc.perform(get("/api/analytics/skills"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/analytics/insights rejects unauthenticated requests with 401")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/analytics/insights"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/analytics/overview returns 200 with deterministic overview KPIs for authenticated user")
    void shouldReturnOverviewForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = authenticateUser(userId);

        com.jobcommandcenter.analytics.domain.AnalyticsOverview overview =
            new com.jobcommandcenter.analytics.domain.AnalyticsOverview(
                10L, 4L, 3L, 2L, 1L, 2L,
                new BigDecimal("30.00"), new BigDecimal("100.00"), new BigDecimal("10.00")
            );

        when(queryService.getOverview(eq(userId))).thenReturn(overview);

        mockMvc.perform(get("/api/analytics/overview")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplications").value(10))
            .andExpect(jsonPath("$.activePipelines").value(4))
            .andExpect(jsonPath("$.interviewsCount").value(3))
            .andExpect(jsonPath("$.assessmentsCount").value(2))
            .andExpect(jsonPath("$.activeOffers").value(1))
            .andExpect(jsonPath("$.rejectionsCount").value(2))
            .andExpect(jsonPath("$.interviewConversionRatePercent").value(30.00))
            .andExpect(jsonPath("$.assessmentPassRatePercent").value(100.00))
            .andExpect(jsonPath("$.offerRatePercent").value(10.00));
    }

    @Test
    @DisplayName("GET /api/analytics/funnel returns 200 with funnel metrics for authenticated user")
    void shouldReturnFunnelForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = authenticateUser(userId);

        com.jobcommandcenter.analytics.domain.StageConversionRate stageRate =
            com.jobcommandcenter.analytics.domain.StageConversionRate.of(
                "APPLIED", "SCREENING", 10L, 4L, 3.5
            );

        com.jobcommandcenter.analytics.domain.FunnelMetrics funnel =
            new com.jobcommandcenter.analytics.domain.FunnelMetrics(
                10L, 4L, 0L, 10L, 4L, 2L, 3L, 1L, 0L, 2L, 0L, 0L,
                new BigDecimal("40.00"), new BigDecimal("20.00"), new BigDecimal("30.00"),
                new BigDecimal("10.00"), BigDecimal.ZERO.setScale(2),
                List.of(stageRate)
            );

        when(queryService.getFunnelMetrics(eq(userId))).thenReturn(funnel);

        mockMvc.perform(get("/api/analytics/funnel")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalApplications").value(10))
            .andExpect(jsonPath("$.activeApplications").value(4))
            .andExpect(jsonPath("$.screeningCount").value(4))
            .andExpect(jsonPath("$.screeningConversionRate").value(40.00))
            .andExpect(jsonPath("$.stageConversions[0].fromStage").value("APPLIED"))
            .andExpect(jsonPath("$.stageConversions[0].toStage").value("SCREENING"))
            .andExpect(jsonPath("$.stageConversions[0].enteredCount").value(10))
            .andExpect(jsonPath("$.stageConversions[0].progressedCount").value(4))
            .andExpect(jsonPath("$.stageConversions[0].conversionRatePercent").value(40.00))
            .andExpect(jsonPath("$.stageConversions[0].medianDaysInStage").value(3.5));
    }

    @Test
    @DisplayName("GET /api/analytics/sources returns 200 with source effectiveness for authenticated user")
    void shouldReturnSourcesForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = authenticateUser(userId);

        com.jobcommandcenter.analytics.domain.SourceEffectiveness source =
            com.jobcommandcenter.analytics.domain.SourceEffectiveness.of(
                com.jobcommandcenter.application.domain.ApplicationSource.LINKEDIN,
                15L, 6L, 3L, 1L
            );

        when(queryService.getSourceEffectiveness(eq(userId))).thenReturn(List.of(source));

        mockMvc.perform(get("/api/analytics/sources")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].source").value("LINKEDIN"))
            .andExpect(jsonPath("$[0].totalApplications").value(15))
            .andExpect(jsonPath("$[0].screeningsReached").value(6))
            .andExpect(jsonPath("$[0].interviewsReached").value(3))
            .andExpect(jsonPath("$[0].offersReceived").value(1))
            .andExpect(jsonPath("$[0].screeningRatePercent").value(40.00))
            .andExpect(jsonPath("$[0].interviewRatePercent").value(20.00))
            .andExpect(jsonPath("$[0].offerRatePercent").value(6.67));
    }

    @Test
    @DisplayName("GET /api/analytics/skills returns 200 with skill gaps for authenticated user")
    void shouldReturnSkillsForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = authenticateUser(userId);

        UUID skillId = UUID.randomUUID();
        com.jobcommandcenter.analytics.domain.SkillGapMetric gap =
            com.jobcommandcenter.analytics.domain.SkillGapMetric.of(
                skillId, "Kubernetes", "DEVOPS", 5L, 10L, false, null
            );

        when(queryService.getSkillGaps(eq(userId))).thenReturn(List.of(gap));

        mockMvc.perform(get("/api/analytics/skills")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].skillId").value(skillId.toString()))
            .andExpect(jsonPath("$[0].skillName").value("Kubernetes"))
            .andExpect(jsonPath("$[0].category").value("DEVOPS"))
            .andExpect(jsonPath("$[0].requiredJobCount").value(5))
            .andExpect(jsonPath("$[0].totalTargetJobs").value(10))
            .andExpect(jsonPath("$[0].marketDemandPercent").value(50.00))
            .andExpect(jsonPath("$[0].candidateVerified").value(false));
    }

    @Test
    @DisplayName("POST /api/analytics/insights returns 200 with grounded AI recommendations for authenticated user")
    void shouldReturnInsightsForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = authenticateUser(userId);

        AdvisorBottleneck bottleneck = new AdvisorBottleneck(
            "RESUME_SCREENING",
            "Screening Lag",
            "Conversion is below threshold",
            Map.of("interviewConversionRate", "10.00%"),
            "HIGH",
            List.of("Tailor resume")
        );

        AdvisorRecommendation rec = new AdvisorRecommendation(
            "Tailor resume to job skills",
            "Observed 10.00% conversion",
            "HIGH",
            "interviewConversionRate=10.00%"
        );

        AIAnalyticsAdvisorResponse response = new AIAnalyticsAdvisorResponse(
            "Pipeline summary for candidate",
            List.of(bottleneck),
            List.of(rec),
            List.of("Strong full stack foundation"),
            List.of(),
            new BigDecimal("0.90"),
            Instant.now()
        );

        when(insightService.generateInsights(eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/analytics/insights")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").value("Pipeline summary for candidate"))
            .andExpect(jsonPath("$.bottlenecks[0].category").value("RESUME_SCREENING"))
            .andExpect(jsonPath("$.bottlenecks[0].supportingMetrics.interviewConversionRate").value("10.00%"))
            .andExpect(jsonPath("$.recommendations[0].action").value("Tailor resume to job skills"))
            .andExpect(jsonPath("$.strengths[0]").value("Strong full stack foundation"));
    }

    @Test
    @DisplayName("Invalid token produces 401 Unauthorized")
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/analytics/overview")
                .header("Authorization", "Bearer invalid-jwt-token"))
            .andExpect(status().isUnauthorized());
    }

    private String authenticateUser(UUID userId) {
        com.jobcommandcenter.user.domain.User domainUser = new com.jobcommandcenter.user.domain.User(
            userId, "candidate-" + userId + "@example.com", "hash", "Candidate", "User", "Candidate User",
            AccountStatus.ACTIVE, Role.USER, Instant.now(), Instant.now(), null
        );
        when(userRepository.findById(eq(userId))).thenReturn(java.util.Optional.of(domainUser));
        return jwtTokenService.generateToken(domainUser);
    }
}
