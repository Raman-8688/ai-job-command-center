package com.jobcommandcenter.ai.domain;

import com.jobcommandcenter.analytics.domain.AnalyticsOverview;
import com.jobcommandcenter.analytics.domain.FunnelMetrics;
import com.jobcommandcenter.analytics.domain.SkillGapMetric;
import com.jobcommandcenter.analytics.domain.SourceEffectiveness;

import java.util.List;

/**
 * Allowlisted context payload for requesting grounded career strategy advice.
 * Contains only computed tenant metrics without PII, tokens, or raw resume text.
 */
public record AIAnalyticsAdvisorRequest(
    AnalyticsOverview overview,
    FunnelMetrics funnel,
    List<SourceEffectiveness> sourceEffectiveness,
    List<SkillGapMetric> skillGaps
) {
    public AIAnalyticsAdvisorRequest {
        if (sourceEffectiveness == null) {
            sourceEffectiveness = List.of();
        }
        if (skillGaps == null) {
            skillGaps = List.of();
        }
    }
}
