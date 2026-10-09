package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.application.domain.ApplicationStatus;

import java.util.Map;

public record ApplicationDashboardSummaryResponse(
    Map<ApplicationStatus, Long> countsByStatus,
    long activeApplicationsCount,
    long followUpsDueCount,
    long totalApplicationsCount
) {}
