package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewRound;

import java.util.List;
import java.util.UUID;

public record InterviewPrepBundleResponse(
    UUID interviewId,
    String jobTitle,
    String companyName,
    InterviewRound round,
    int readinessScore,
    String strategySummary,
    List<InterviewPreparationResponse> questions
) {}
