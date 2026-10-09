package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import com.jobcommandcenter.assessment.domain.AssessmentResult;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.assessment.domain.OnlineAssessment;

import java.time.Instant;
import java.util.UUID;

public record OnlineAssessmentSummaryResponse(
    UUID id,
    UUID jobId,
    String jobTitle,
    String companyName,
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
    int checklistCompletionPercentage,
    int totalChecklistItems,
    Long version,
    Instant createdAt,
    Instant updatedAt
) {
    public static OnlineAssessmentSummaryResponse fromDomain(OnlineAssessment domain, String jobTitle, String companyName) {
        return new OnlineAssessmentSummaryResponse(
            domain.getId(),
            domain.getJobId(),
            jobTitle,
            companyName,
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
            domain.getChecklistCompletionPercentage(),
            domain.getChecklists().size(),
            domain.getVersion(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
