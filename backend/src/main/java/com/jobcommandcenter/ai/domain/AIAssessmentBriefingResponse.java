package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Structured AI response containing platform-specific guidance, time management,
 * prioritized topics, and technical checklist items.
 */
public record AIAssessmentBriefingResponse(
    String platformGuidance,
    String timeManagementAdvice,
    List<String> prioritizedTopics,
    List<AIAssessmentChecklistItem> checklistItems,
    BigDecimal confidenceScore
) {
    public AIAssessmentBriefingResponse {
        if (prioritizedTopics == null) {
            prioritizedTopics = List.of();
        }
        if (checklistItems == null) {
            checklistItems = List.of();
        }
        if (confidenceScore == null) {
            confidenceScore = new BigDecimal("0.85");
        }
    }
}
