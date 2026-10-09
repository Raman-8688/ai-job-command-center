package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewFormat;
import com.jobcommandcenter.interview.domain.InterviewRound;

public record UpdateInterviewDetailsRequest(
    InterviewRound round,
    Integer roundNumber,
    InterviewFormat format,
    String meetingLink,
    String location,
    String interviewerNames,
    String interviewerRoles,
    String notes
) {}
