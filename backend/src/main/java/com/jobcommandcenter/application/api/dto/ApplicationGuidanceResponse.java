package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.ai.domain.AIApplicationGuidanceResponse;

import java.time.Instant;

public record ApplicationGuidanceResponse(
    String recommendedAction,
    String rationale,
    String draftedFollowUpMessage,
    String modelUsed,
    Instant generatedAt
) {
    public static ApplicationGuidanceResponse fromAiResponse(AIApplicationGuidanceResponse ai, String modelUsed) {
        return new ApplicationGuidanceResponse(
            ai.recommendedAction(),
            ai.rationale(),
            ai.draftedFollowUpMessage(),
            modelUsed != null ? modelUsed : "MockDeterministicAIProvider",
            Instant.now()
        );
    }
}
