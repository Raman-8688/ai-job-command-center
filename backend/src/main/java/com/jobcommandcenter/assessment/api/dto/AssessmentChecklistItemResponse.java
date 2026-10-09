package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.AssessmentChecklistItem;

import java.time.Instant;
import java.util.UUID;

public record AssessmentChecklistItemResponse(
    UUID id,
    UUID assessmentId,
    String topicCategory,
    String title,
    String description,
    boolean isCompleted,
    int sortOrder,
    Instant createdAt,
    Instant updatedAt
) {
    public static AssessmentChecklistItemResponse fromDomain(AssessmentChecklistItem item) {
        return new AssessmentChecklistItemResponse(
            item.getId(),
            item.getAssessmentId(),
            item.getTopicCategory(),
            item.getTitle(),
            item.getDescription(),
            item.isCompleted(),
            item.getSortOrder(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
