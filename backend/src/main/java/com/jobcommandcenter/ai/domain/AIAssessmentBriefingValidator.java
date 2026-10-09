package com.jobcommandcenter.ai.domain;

import com.jobcommandcenter.assessment.domain.AssessmentChecklistItem;

import java.util.*;

/**
 * Domain validator and merge utility for AI-generated assessment briefings and study checklists.
 * Ensures strict bounds, valid categories, and preserves existing candidate checklist completion state.
 */
public final class AIAssessmentBriefingValidator {

    public static final int MIN_CHECKLIST_ITEMS = 1;
    public static final int MAX_CHECKLIST_ITEMS = 25;
    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_CATEGORY_LENGTH = 50;

    private AIAssessmentBriefingValidator() {}

    /**
     * Validates structural invariants of an AI assessment briefing response.
     * Throws IllegalArgumentException if any boundary or required field constraint is violated.
     */
    public static void validateBriefingResponse(AIAssessmentBriefingResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("Assessment briefing response must not be null");
        }
        if (response.platformGuidance() == null || response.platformGuidance().isBlank()) {
            throw new IllegalArgumentException("Platform guidance must not be blank");
        }
        List<AIAssessmentChecklistItem> items = response.checklistItems();
        if (items == null || items.size() < MIN_CHECKLIST_ITEMS) {
            throw new IllegalArgumentException("Checklist items must contain at least " + MIN_CHECKLIST_ITEMS + " item");
        }
        if (items.size() > MAX_CHECKLIST_ITEMS) {
            throw new IllegalArgumentException("Checklist items must not exceed " + MAX_CHECKLIST_ITEMS + " items");
        }

        Set<String> seenTitles = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            AIAssessmentChecklistItem item = items.get(i);
            if (item == null) {
                throw new IllegalArgumentException("Checklist item at index " + i + " must not be null");
            }
            if (item.title() == null || item.title().isBlank()) {
                throw new IllegalArgumentException("Checklist item title at index " + i + " must not be blank");
            }
            if (item.title().length() > MAX_TITLE_LENGTH) {
                throw new IllegalArgumentException("Checklist item title at index " + i + " exceeds max length of " + MAX_TITLE_LENGTH);
            }
            if (item.topicCategory() != null && item.topicCategory().length() > MAX_CATEGORY_LENGTH) {
                throw new IllegalArgumentException("Checklist item category at index " + i + " exceeds max length of " + MAX_CATEGORY_LENGTH);
            }
            if (item.sortOrder() < 0) {
                throw new IllegalArgumentException("Checklist item sortOrder at index " + i + " must be non-negative");
            }

            String normalizedTitle = item.title().trim().toLowerCase(Locale.ROOT);
            if (!seenTitles.add(normalizedTitle)) {
                throw new IllegalArgumentException("Duplicate checklist item title detected: '" + item.title() + "'");
            }
        }
    }

    /**
     * Merges newly recommended AI checklist items into existing items for an assessment.
     * Strictly preserves existing candidate progress (completed status and IDs), preventing duplicates.
     */
    public static List<AssessmentChecklistItem> mergeWithoutOverwriting(
            List<AssessmentChecklistItem> existingItems,
            List<AIAssessmentChecklistItem> recommendedItems,
            UUID assessmentId) {

        List<AssessmentChecklistItem> result = new ArrayList<>();
        Set<String> existingNormalizedTitles = new HashSet<>();
        int maxSortOrder = 0;

        if (existingItems != null) {
            for (AssessmentChecklistItem existing : existingItems) {
                result.add(existing);
                existingNormalizedTitles.add(existing.getTitle().trim().toLowerCase(Locale.ROOT));
                if (existing.getSortOrder() > maxSortOrder) {
                    maxSortOrder = existing.getSortOrder();
                }
            }
        }

        if (recommendedItems != null) {
            for (AIAssessmentChecklistItem rec : recommendedItems) {
                String normalized = rec.title().trim().toLowerCase(Locale.ROOT);
                if (!existingNormalizedTitles.contains(normalized)) {
                    maxSortOrder++;
                    result.add(AssessmentChecklistItem.create(
                        assessmentId,
                        rec.topicCategory(),
                        rec.title().trim(),
                        rec.description(),
                        maxSortOrder
                    ));
                    existingNormalizedTitles.add(normalized);
                }
            }
        }

        return result;
    }
}
