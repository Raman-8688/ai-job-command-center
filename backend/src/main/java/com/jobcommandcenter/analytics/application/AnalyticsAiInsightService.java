package com.jobcommandcenter.analytics.application;

import com.jobcommandcenter.ai.domain.AIAnalyticsAdvisorRequest;
import com.jobcommandcenter.ai.domain.AIAnalyticsAdvisorResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.analytics.domain.AnalyticsOverview;
import com.jobcommandcenter.analytics.domain.FunnelMetrics;
import com.jobcommandcenter.analytics.domain.SkillGapMetric;
import com.jobcommandcenter.analytics.domain.SourceEffectiveness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service generating grounded career strategy insights and bottleneck recommendations.
 * Synthesizes tenant-isolated analytics and delegates to AIProvider without modifying user state.
 */
@Service
public class AnalyticsAiInsightService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsAiInsightService.class);

    private final AnalyticsQueryService analyticsQueryService;
    private final AIProvider aiProvider;

    public AnalyticsAiInsightService(AnalyticsQueryService analyticsQueryService, AIProvider aiProvider) {
        this.analyticsQueryService = Objects.requireNonNull(analyticsQueryService, "analyticsQueryService must not be null");
        this.aiProvider = Objects.requireNonNull(aiProvider, "aiProvider must not be null");
    }

    /**
     * Generates grounded career strategy advice for the authenticated tenant user.
     */
    public AIAnalyticsAdvisorResponse generateInsights(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        log.info("Generating grounded career strategy advisor insights for user: {}", userId);

        // 1. Gather authoritative tenant analytics
        AnalyticsOverview overview = analyticsQueryService.getOverview(userId);
        FunnelMetrics funnel = analyticsQueryService.getFunnelMetrics(userId);
        List<SourceEffectiveness> sources = analyticsQueryService.getSourceEffectiveness(userId);
        List<SkillGapMetric> skillGaps = analyticsQueryService.getSkillGaps(userId);

        // 2. Build allowlisted typed request payload
        AIAnalyticsAdvisorRequest request = new AIAnalyticsAdvisorRequest(
            overview,
            funnel,
            sources,
            skillGaps
        );

        // 3. Invoke provider
        AIAnalyticsAdvisorResponse response = aiProvider.generateCareerStrategy(request);

        // 4. Validate grounding invariants
        if (response == null) {
            log.warn("AIProvider returned null career strategy response for user: {}", userId);
            return new AIAnalyticsAdvisorResponse(
                "Analytics advisor temporarily unavailable.",
                List.of(),
                List.of(),
                List.of(),
                List.of("AI response was empty or unparseable."),
                null,
                Instant.now()
            );
        }

        return response;
    }
}
