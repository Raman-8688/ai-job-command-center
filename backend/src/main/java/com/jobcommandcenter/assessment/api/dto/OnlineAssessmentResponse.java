package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import com.jobcommandcenter.assessment.domain.AssessmentResult;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.assessment.domain.OnlineAssessment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record OnlineAssessmentResponse(
    UUID id,
    UUID userId,
    UUID jobId,
    String jobTitle,
    String companyName,
    String jobLocation,
    UUID applicationId,
    UUID interviewId,
    AssessmentPlatform platform,
    String title,
    AssessmentStatus status,
    AssessmentResult result,
    Integer durationMinutes,
    Instant invitedAt,
    Instant expiresAt,
    Instant scheduledStartTime,
    Instant completedAt,
    BigDecimal score,
    BigDecimal maxScore,
    String assessmentUrl,
    String accessCode,
    String submissionNotes,
    String submissionRepoUrl,
    Long version,
    int checklistCompletionPercentage,
    int checklistTotalCount,
    int checklistCompletedCount,
    List<AssessmentChecklistItemResponse> checklist,
    List<OnlineAssessmentEventResponse> recentEvents,
    Instant createdAt,
    Instant updatedAt
) {
    public static OnlineAssessmentResponse fromDomain(OnlineAssessment domain, String jobTitle, String companyName, String jobLocation) {
        List<AssessmentChecklistItemResponse> checklistResponses = domain.getChecklists().stream()
            .map(AssessmentChecklistItemResponse::fromDomain)
            .collect(Collectors.toList());

        List<OnlineAssessmentEventResponse> eventResponses = domain.getEvents().stream()
            .map(OnlineAssessmentEventResponse::fromDomain)
            .collect(Collectors.toList());

        long completedCount = domain.getChecklists().stream()
            .filter(item -> item.isCompleted())
            .count();

        return new OnlineAssessmentResponse(
            domain.getId(),
            domain.getUserId(),
            domain.getJobId(),
            jobTitle,
            companyName,
            jobLocation,
            domain.getApplicationId(),
            domain.getInterviewId(),
            domain.getPlatform(),
            domain.getTitle(),
            domain.getStatus(),
            domain.getResult(),
            domain.getDurationMinutes(),
            domain.getInvitedAt(),
            domain.getExpiresAt(),
            domain.getScheduledStartTime(),
            domain.getCompletedAt(),
            domain.getScore(),
            domain.getMaxScore(),
            domain.getAssessmentUrl(),
            domain.getAccessCode(),
            domain.getSubmissionNotes(),
            domain.getSubmissionRepoUrl(),
            domain.getVersion(),
            domain.getChecklistCompletionPercentage(),
            domain.getChecklists().size(),
            (int) completedCount,
            checklistResponses,
            eventResponses,
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
