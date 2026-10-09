package com.jobcommandcenter.interview.infrastructure;

import com.jobcommandcenter.interview.domain.InterviewPreparation;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_preparations")
public class InterviewPreparationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "interview_id", nullable = false, insertable = false, updatable = false)
    private UUID interviewId;

    @Column(name = "topic_category", nullable = false, length = 50)
    private String topicCategory;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "talking_points", columnDefinition = "TEXT")
    private String talkingPoints;

    @Column(name = "suggested_answer_star", columnDefinition = "TEXT")
    private String suggestedAnswerStar;

    @Column(name = "user_answer_notes", columnDefinition = "TEXT")
    private String userAnswerNotes;

    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "is_reviewed", nullable = false)
    private boolean isReviewed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InterviewPreparationJpaEntity() {}

    public static InterviewPreparationJpaEntity fromDomain(InterviewPreparation domain) {
        InterviewPreparationJpaEntity entity = new InterviewPreparationJpaEntity();
        entity.id = domain.getId();
        entity.interviewId = domain.getInterviewId();
        entity.topicCategory = domain.getTopicCategory();
        entity.question = domain.getQuestion();
        entity.talkingPoints = domain.getTalkingPoints();
        entity.suggestedAnswerStar = domain.getSuggestedAnswerStar();
        entity.userAnswerNotes = domain.getUserAnswerNotes();
        entity.confidenceScore = domain.getConfidenceScore();
        entity.isReviewed = domain.isReviewed();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    public InterviewPreparation toDomain() {
        return new InterviewPreparation(
            this.id,
            this.interviewId,
            this.topicCategory,
            this.question,
            this.talkingPoints,
            this.suggestedAnswerStar,
            this.userAnswerNotes,
            this.confidenceScore,
            this.isReviewed,
            this.createdAt,
            this.updatedAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getInterviewId() { return interviewId; }
    public void setInterviewId(UUID interviewId) { this.interviewId = interviewId; }
    public String getTopicCategory() { return topicCategory; }
    public void setTopicCategory(String topicCategory) { this.topicCategory = topicCategory; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getTalkingPoints() { return talkingPoints; }
    public void setTalkingPoints(String talkingPoints) { this.talkingPoints = talkingPoints; }
    public String getSuggestedAnswerStar() { return suggestedAnswerStar; }
    public void setSuggestedAnswerStar(String suggestedAnswerStar) { this.suggestedAnswerStar = suggestedAnswerStar; }
    public String getUserAnswerNotes() { return userAnswerNotes; }
    public void setUserAnswerNotes(String userAnswerNotes) { this.userAnswerNotes = userAnswerNotes; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public boolean isReviewed() { return isReviewed; }
    public void setReviewed(boolean reviewed) { isReviewed = reviewed; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
