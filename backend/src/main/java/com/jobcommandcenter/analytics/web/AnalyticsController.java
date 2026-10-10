package com.jobcommandcenter.analytics.web;

import com.jobcommandcenter.ai.domain.AIAnalyticsAdvisorResponse;
import com.jobcommandcenter.analytics.application.AnalyticsAiInsightService;
import com.jobcommandcenter.analytics.application.AnalyticsQueryService;
import com.jobcommandcenter.analytics.domain.AnalyticsOverview;
import com.jobcommandcenter.analytics.domain.FunnelMetrics;
import com.jobcommandcenter.analytics.domain.SkillGapMetric;
import com.jobcommandcenter.analytics.domain.SourceEffectiveness;
import com.jobcommandcenter.security.jwt.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Controller exposing deterministic pipeline analytics, funnel metrics,
 * channel effectiveness, skill gaps, and the grounded AI Career Strategy Advisor.
 * Secures requests with @AuthenticationPrincipal SecurityUser and enforces read-only safety.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsQueryService queryService;
    private final AnalyticsAiInsightService insightService;

    public AnalyticsController(
        AnalyticsQueryService queryService,
        AnalyticsAiInsightService insightService
    ) {
        this.queryService = Objects.requireNonNull(queryService, "queryService must not be null");
        this.insightService = Objects.requireNonNull(insightService, "insightService must not be null");
    }

    /**
     * Retrieves overall career search KPIs for the authenticated user.
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverview> getOverview(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        AnalyticsOverview overview = queryService.getOverview(securityUser.getId());
        return ResponseEntity.ok(overview);
    }

    /**
     * Retrieves stage-by-stage funnel conversion metrics for the authenticated user.
     */
    @GetMapping("/funnel")
    public ResponseEntity<FunnelMetrics> getFunnel(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        FunnelMetrics funnel = queryService.getFunnelMetrics(securityUser.getId());
        return ResponseEntity.ok(funnel);
    }

    /**
     * Retrieves application source effectiveness and conversion rates for the authenticated user.
     */
    @GetMapping("/sources")
    public ResponseEntity<List<SourceEffectiveness>> getSources(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        List<SourceEffectiveness> sources = queryService.getSourceEffectiveness(securityUser.getId());
        return ResponseEntity.ok(sources);
    }

    /**
     * Retrieves target job skill gap metrics for the authenticated user.
     */
    @GetMapping("/skills")
    public ResponseEntity<List<SkillGapMetric>> getSkills(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        List<SkillGapMetric> skills = queryService.getSkillGaps(securityUser.getId());
        return ResponseEntity.ok(skills);
    }

    /**
     * Generates on-demand, evidence-grounded career strategy diagnostics and tactical actions.
     * Computes a fresh response grounded in real-time candidate pipeline metrics.
     */
    @PostMapping("/insights")
    public ResponseEntity<AIAnalyticsAdvisorResponse> generateInsights(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        AIAnalyticsAdvisorResponse response = insightService.generateInsights(securityUser.getId());
        return ResponseEntity.ok(response);
    }
}
