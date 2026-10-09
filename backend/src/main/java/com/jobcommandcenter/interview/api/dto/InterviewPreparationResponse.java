package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewPreparation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InterviewPreparationResponse(
    UUID id,
    UUID interviewId,
    String topicCategory,
    String question,
    String talkingPoints,
    String suggestedAnswerStar,
    String userAnswerNotes,
    BigDecimal confidenceScore,
    boolean isReviewed,
    Instant createdAt,
    Instant updatedAt
) {
    public static InterviewPreparationResponse fromDomain(InterviewPreparation domain) {
        return new InterviewPreparationResponse(
            domain.getId(),
            domain.getInterviewId(),
            domain.getTopicCategory(),
            domain.getQuestion(),
            domain.getTalkingPoints(),
            domain.getSuggestedAnswerStar(),
            domain.getUserAnswerNotes(),
            domain.getConfidenceScore(),
            domain.isReviewed(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
