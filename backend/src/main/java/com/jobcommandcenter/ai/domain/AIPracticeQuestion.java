package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;

/**
 * Structured AI practice question with STAR answer guideline and talking points.
 */
public record AIPracticeQuestion(
    String topicCategory,
    String question,
    String talkingPoints,
    String suggestedAnswerStar,
    BigDecimal confidenceScore
) {
    public AIPracticeQuestion {
        if (confidenceScore == null) {
            confidenceScore = new BigDecimal("0.85");
        }
    }
}
