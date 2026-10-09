package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewRound;
import com.jobcommandcenter.interview.domain.InterviewStatus;

import java.util.Map;

public record InterviewDashboardSummaryResponse(
    long totalInterviews,
    long upcomingInterviews,
    long completedInterviews,
    Map<InterviewRound, Long> countByRound,
    Map<InterviewStatus, Long> countByStatus,
    InterviewSummaryResponse nextUpcomingInterview,
    int overallReadinessScore
) {}
