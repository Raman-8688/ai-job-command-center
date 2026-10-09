package com.jobcommandcenter.email.api.dto;

import com.jobcommandcenter.email.domain.Email;
import com.jobcommandcenter.email.domain.EmailClassification;
import com.jobcommandcenter.email.domain.EmailProcessingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Detailed DTO for viewing full email contents and extracted intelligence.
 */
public record EmailDetailResponse(
    UUID id,
    UUID connectionId,
    String externalMessageId,
    String externalThreadId,
    String sender,
    String recipient,
    String subject,
    String snippet,
    String bodyPlain,
    String bodyHtml,
    Instant receivedAt,
    EmailClassification classification,
    BigDecimal classificationConfidence,
    String classificationReason,
    EmailProcessingStatus processingStatus,
    String extractedCompanyName,
    String extractedJobTitle,
    String extractedExternalId,
    String extractedNotes,
    UUID associatedJobId,
    Instant createdAt,
    Instant updatedAt
) {
    public static EmailDetailResponse fromDomain(Email domain) {
        return new EmailDetailResponse(
            domain.getId(),
            domain.getConnectionId(),
            domain.getExternalMessageId(),
            domain.getExternalThreadId(),
            domain.getSender(),
            domain.getRecipient(),
            domain.getSubject(),
            domain.getSnippet(),
            domain.getBodyPlain(),
            domain.getBodyHtml(),
            domain.getReceivedAt(),
            domain.getClassification(),
            domain.getClassificationConfidence(),
            domain.getClassificationReason(),
            domain.getProcessingStatus(),
            domain.getExtractedCompanyName(),
            domain.getExtractedJobTitle(),
            domain.getExtractedExternalId(),
            domain.getExtractedNotes(),
            domain.getAssociatedJobId(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
