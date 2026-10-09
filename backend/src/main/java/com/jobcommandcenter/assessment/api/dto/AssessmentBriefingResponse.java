package com.jobcommandcenter.assessment.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AssessmentBriefingResponse(
    UUID assessmentId,
    String platformGuidance,
    String timeManagementAdvice,
    List<String> prioritizedTopics,
    int totalChecklistItems,
    int completedChecklistItems,
    int completionPercentage,
    List<AssessmentChecklistItemResponse> checklist,
    BigDecimal confidenceScore
) {}
