package com.jobcommandcenter.assessment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an individual technical study checklist item
 * generated for an online assessment.
 */
public class AssessmentChecklistItem {

    private final UUID id;
    private final UUID assessmentId;
    private String topicCategory;
    private String title;
    private String description;
    private boolean isCompleted;
    private int sortOrder;
    private final Instant createdAt;
    private Instant updatedAt;

    public AssessmentChecklistItem(UUID id, UUID assessmentId, String topicCategory, String title,
                                   String description, boolean isCompleted, int sortOrder,
                                   Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Checklist item id must not be null");
        this.assessmentId = Objects.requireNonNull(assessmentId, "Assessment id must not be null");
        this.title = Objects.requireNonNull(title, "Checklist title must not be null");
        this.topicCategory = (topicCategory != null && !topicCategory.isBlank()) ? topicCategory.toUpperCase() : "GENERAL";
        this.description = description;
        this.isCompleted = isCompleted;
        this.sortOrder = Math.max(0, sortOrder);
        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.createdAt;
    }

    public static AssessmentChecklistItem create(UUID assessmentId, String topicCategory,
                                                String title, String description, int sortOrder) {
        Instant now = Instant.now();
        return new AssessmentChecklistItem(
            UUID.randomUUID(),
            assessmentId,
            topicCategory,
            title,
            description,
            false,
            sortOrder,
            now,
            now
        );
    }

    public void toggleCompleted() {
        this.isCompleted = !this.isCompleted;
        this.updatedAt = Instant.now();
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getAssessmentId() { return assessmentId; }
    public String getTopicCategory() { return topicCategory; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return isCompleted; }
    public int getSortOrder() { return sortOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
