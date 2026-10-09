package com.jobcommandcenter.assessment.api.dto;

public record AssessmentDashboardSummaryResponse(
    long totalAssessments,
    long invited,
    long inProgress,
    long submitted,
    long expired,
    long abandoned,
    long dueSoon,
    long overdue
) {}
