package com.jobcommandcenter.ai.domain;

/**
 * Structured AI study checklist recommendation for online assessment preparation.
 */
public record AIAssessmentChecklistItem(
    String topicCategory,
    String title,
    String description,
    int sortOrder
) {
    public AIAssessmentChecklistItem {
        if (topicCategory == null || topicCategory.isBlank()) {
            topicCategory = "GENERAL";
        }
        if (title == null || title.isBlank()) {
            title = "Preparation Topic";
        }
        if (description == null) {
            description = "";
        }
    }
}
