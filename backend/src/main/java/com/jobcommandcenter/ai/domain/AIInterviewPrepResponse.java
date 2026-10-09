package com.jobcommandcenter.ai.domain;

import java.util.List;

/**
 * Structured AI response containing interview preparation questions and readiness strategy.
 */
public record AIInterviewPrepResponse(
    int recommendedReadinessScore,
    String strategySummary,
    List<AIPracticeQuestion> questions
) {
    public AIInterviewPrepResponse {
        if (questions == null) {
            questions = List.of();
        }
    }
}
