package com.jobcommandcenter.analytics.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.MockDeterministicAIProvider;
import com.jobcommandcenter.analytics.domain.*;
import com.jobcommandcenter.application.domain.ApplicationSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsAiInsightService & Mock Advisor Unit Tests")
class AnalyticsAiInsightServiceUnitTest {

    @Mock
    private AnalyticsQueryService analyticsQueryService;

    private MockDeterministicAIProvider mockAIProvider;
    private AnalyticsAiInsightService insightService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockAIProvider = new MockDeterministicAIProvider();
        insightService = new AnalyticsAiInsightService(analyticsQueryService, mockAIProvider);
    }

    @Test
    @DisplayName("Handles empty candidate data gracefully with onboarding guidance and limitations")
    void shouldHandleEmptyCandidateDataGracefully() {
        when(analyticsQueryService.getOverview(eq(userId))).thenReturn(AnalyticsOverview.empty());
        when(analyticsQueryService.getFunnelMetrics(eq(userId))).thenReturn(FunnelMetrics.empty());
        when(analyticsQueryService.getSourceEffectiveness(eq(userId))).thenReturn(Collections.emptyList());
        when(analyticsQueryService.getSkillGaps(eq(userId))).thenReturn(Collections.emptyList());

        AIAnalyticsAdvisorResponse response = insightService.generateInsights(userId);

        assertThat(response).isNotNull();
        assertThat(response.summary()).contains("Pipeline is currently empty");
        assertThat(response.dataLimitations()).contains("Zero recorded job applications. Milestone conversion rates cannot be evaluated.");
        assertThat(response.bottlenecks()).isEmpty();
        assertThat(response.recommendations()).isNotEmpty();
        assertThat(response.recommendations().get(0).action()).contains("Shortlist target positions");
    }

    @Test
    @DisplayName("Diagnoses low resume screening conversion bottleneck grounded in actual metrics")
    void shouldDiagnoseScreeningConversionBottleneck() {
        AnalyticsOverview overview = new AnalyticsOverview(
            20L, // 20 apps
            10L, // active
            2L,  // interviews
            1L,  // assessments
            0L,  // offers
            8L,  // rejections
            new BigDecimal("10.00"), // 10.00% interview rate (below 15% threshold)
            new BigDecimal("100.00"),
            BigDecimal.ZERO
        );

        when(analyticsQueryService.getOverview(eq(userId))).thenReturn(overview);
        when(analyticsQueryService.getFunnelMetrics(eq(userId))).thenReturn(FunnelMetrics.empty());
        when(analyticsQueryService.getSourceEffectiveness(eq(userId))).thenReturn(Collections.emptyList());
        when(analyticsQueryService.getSkillGaps(eq(userId))).thenReturn(Collections.emptyList());

        AIAnalyticsAdvisorResponse response = insightService.generateInsights(userId);

        assertThat(response.bottlenecks()).hasSize(1);
        AdvisorBottleneck bottleneck = response.bottlenecks().get(0);
        assertThat(bottleneck.category()).isEqualTo("RESUME_SCREENING");
        assertThat(bottleneck.supportingMetrics()).containsEntry("interviewConversionRate", "10.00%");
        assertThat(bottleneck.supportingMetrics()).containsEntry("totalApplications", "20");
        assertThat(bottleneck.recommendations()).isNotEmpty();

        assertThat(response.recommendations()).anyMatch(r -> r.action().contains("Target resume tailoring"));
    }

    @Test
    @DisplayName("Diagnoses assessment pass rate bottleneck when below 50%")
    void shouldDiagnoseAssessmentPassRateBottleneck() {
        AnalyticsOverview overview = new AnalyticsOverview(
            15L,
            5L,
            5L,
            6L,  // 6 assessments
            1L,
            2L,
            new BigDecimal("33.33"),
            new BigDecimal("33.33"), // 33.33% pass rate (below 50%)
            new BigDecimal("6.67")
        );

        when(analyticsQueryService.getOverview(eq(userId))).thenReturn(overview);
        when(analyticsQueryService.getFunnelMetrics(eq(userId))).thenReturn(FunnelMetrics.empty());
        when(analyticsQueryService.getSourceEffectiveness(eq(userId))).thenReturn(Collections.emptyList());
        when(analyticsQueryService.getSkillGaps(eq(userId))).thenReturn(Collections.emptyList());

        AIAnalyticsAdvisorResponse response = insightService.generateInsights(userId);

        assertThat(response.bottlenecks()).anyMatch(b -> b.category().equals("ONLINE_ASSESSMENT"));
        AdvisorBottleneck oaBottleneck = response.bottlenecks().stream()
            .filter(b -> b.category().equals("ONLINE_ASSESSMENT"))
            .findFirst()
            .orElseThrow();
        assertThat(oaBottleneck.supportingMetrics()).containsEntry("assessmentPassRate", "33.33%");
    }

    @Test
    @DisplayName("Diagnoses unverified high-demand skill gap and provides verification recommendation")
    void shouldDiagnoseHighDemandSkillGap() {
        AnalyticsOverview overview = new AnalyticsOverview(
            10L, 5L, 3L, 2L, 1L, 2L,
            new BigDecimal("30.00"), new BigDecimal("100.00"), new BigDecimal("10.00")
        );

        SkillGapMetric gap = SkillGapMetric.of(
            UUID.randomUUID(), "Kubernetes", "DEVOPS", 6, 10, false, null
        );

        when(analyticsQueryService.getOverview(eq(userId))).thenReturn(overview);
        when(analyticsQueryService.getFunnelMetrics(eq(userId))).thenReturn(FunnelMetrics.empty());
        when(analyticsQueryService.getSourceEffectiveness(eq(userId))).thenReturn(Collections.emptyList());
        when(analyticsQueryService.getSkillGaps(eq(userId))).thenReturn(List.of(gap));

        AIAnalyticsAdvisorResponse response = insightService.generateInsights(userId);

        assertThat(response.bottlenecks()).anyMatch(b -> b.category().equals("SKILL_DEFICIT"));
        AdvisorBottleneck skillBottleneck = response.bottlenecks().stream()
            .filter(b -> b.category().equals("SKILL_DEFICIT"))
            .findFirst()
            .orElseThrow();
        assertThat(skillBottleneck.supportingMetrics()).containsEntry("skill", "Kubernetes");
        assertThat(skillBottleneck.supportingMetrics()).containsEntry("marketDemand", "60.00%");

        assertThat(response.recommendations()).anyMatch(r -> r.action().contains("Complete verification for Kubernetes"));
    }

    @Test
    @DisplayName("Diagnoses channel efficiency differences when sample sizes allow")
    void shouldDiagnoseChannelEfficiency() {
        AnalyticsOverview overview = new AnalyticsOverview(
            15L, 8L, 4L, 2L, 1L, 3L,
            new BigDecimal("26.67"), new BigDecimal("100.00"), new BigDecimal("6.67")
        );

        SourceEffectiveness referral = SourceEffectiveness.of(ApplicationSource.REFERRAL, 5, 4, 3, 1);
        SourceEffectiveness indeed = SourceEffectiveness.of(ApplicationSource.INDEED, 10, 1, 0, 0);

        when(analyticsQueryService.getOverview(eq(userId))).thenReturn(overview);
        when(analyticsQueryService.getFunnelMetrics(eq(userId))).thenReturn(FunnelMetrics.empty());
        when(analyticsQueryService.getSourceEffectiveness(eq(userId))).thenReturn(List.of(referral, indeed));
        when(analyticsQueryService.getSkillGaps(eq(userId))).thenReturn(Collections.emptyList());

        AIAnalyticsAdvisorResponse response = insightService.generateInsights(userId);

        assertThat(response.strengths()).anyMatch(s -> s.contains("REFERRAL produces higher conversion"));
        assertThat(response.recommendations()).anyMatch(r -> r.action().contains("Shift submission volume from INDEED toward REFERRAL"));
    }
}
