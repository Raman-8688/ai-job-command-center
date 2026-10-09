package com.jobcommandcenter.assessment.api.dto;

import java.util.List;
import java.util.UUID;

public record AssessmentChecklistBundleResponse(
    UUID assessmentId,
    int totalItems,
    int completedItems,
    int completionPercentage,
    List<AssessmentChecklistItemResponse> items
) {}
