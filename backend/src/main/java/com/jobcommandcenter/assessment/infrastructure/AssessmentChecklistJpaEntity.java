package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.AssessmentChecklistItem;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "online_assessment_checklists")
public class AssessmentChecklistJpaEntity {

    @Id
    private UUID id;

    @Column(name = "assessment_id", nullable = false, insertable = false, updatable = false)
    private UUID assessmentId;

    @Column(name = "topic_category", nullable = false, length = 50)
    private String topicCategory;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AssessmentChecklistJpaEntity() {}

    public static AssessmentChecklistJpaEntity fromDomain(AssessmentChecklistItem domain) {
        AssessmentChecklistJpaEntity entity = new AssessmentChecklistJpaEntity();
        entity.id = domain.getId();
        entity.assessmentId = domain.getAssessmentId();
        entity.topicCategory = domain.getTopicCategory();
        entity.title = domain.getTitle();
        entity.description = domain.getDescription();
        entity.isCompleted = domain.isCompleted();
        entity.sortOrder = domain.getSortOrder();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    public AssessmentChecklistItem toDomain() {
        return new AssessmentChecklistItem(
            this.id,
            this.assessmentId,
            this.topicCategory,
            this.title,
            this.description,
            this.isCompleted,
            this.sortOrder,
            this.createdAt,
            this.updatedAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }
    public String getTopicCategory() { return topicCategory; }
    public void setTopicCategory(String topicCategory) { this.topicCategory = topicCategory; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
