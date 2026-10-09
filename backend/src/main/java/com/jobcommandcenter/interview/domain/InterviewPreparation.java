package com.jobcommandcenter.interview.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an interview practice question, model STAR answer, and candidate notes.
 */
public class InterviewPreparation {

    private final UUID id;
    private final UUID interviewId;
    private String topicCategory;
    private String question;
    private String talkingPoints;
    private String suggestedAnswerStar;
    private String userAnswerNotes;
    private BigDecimal confidenceScore;
    private boolean isReviewed;
    private final Instant createdAt;
    private Instant updatedAt;

    public InterviewPreparation(UUID id, UUID interviewId, String topicCategory, String question,
                                String talkingPoints, String suggestedAnswerStar, String userAnswerNotes,
                                BigDecimal confidenceScore, boolean isReviewed, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Preparation id must not be null");
        this.interviewId = Objects.requireNonNull(interviewId, "Interview id must not be null");
        this.topicCategory = (topicCategory != null && !topicCategory.isBlank()) ? topicCategory.toUpperCase() : "TECH";
        this.question = Objects.requireNonNull(question, "Question must not be null");
        this.talkingPoints = talkingPoints;
        this.suggestedAnswerStar = suggestedAnswerStar;
        this.userAnswerNotes = userAnswerNotes;
        this.confidenceScore = (confidenceScore != null) ? confidenceScore : new BigDecimal("0.85");
        this.isReviewed = isReviewed;
        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.createdAt;
    }

    public static InterviewPreparation create(UUID interviewId, String topicCategory, String question,
                                              String talkingPoints, String suggestedAnswerStar, BigDecimal confidenceScore) {
        Instant now = Instant.now();
        return new InterviewPreparation(
            UUID.randomUUID(),
            interviewId,
            topicCategory,
            question,
            talkingPoints,
            suggestedAnswerStar,
            null,
            confidenceScore,
            false,
            now,
            now
        );
    }

    public void updateCandidateNotes(String notes, boolean reviewed) {
        this.userAnswerNotes = notes;
        this.isReviewed = reviewed;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getInterviewId() { return interviewId; }
    public String getTopicCategory() { return topicCategory; }
    public String getQuestion() { return question; }
    public String getTalkingPoints() { return talkingPoints; }
    public String getSuggestedAnswerStar() { return suggestedAnswerStar; }
    public String getUserAnswerNotes() { return userAnswerNotes; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public boolean isReviewed() { return isReviewed; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
