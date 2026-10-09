package com.jobcommandcenter.email.api.dto;

import com.jobcommandcenter.email.domain.Email;
import com.jobcommandcenter.email.domain.EmailClassification;
import com.jobcommandcenter.email.domain.EmailProcessingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Summary DTO for displaying email messages in lists and tables.
 */
public record EmailSummaryResponse(
    UUID id,
    String externalMessageId,
    String sender,
    String recipient,
    String subject,
    String snippet,
    Instant receivedAt,
    EmailClassification classification,
    BigDecimal classificationConfidence,
    EmailProcessingStatus processingStatus,
    String extractedCompanyName,
    String extractedJobTitle,
    UUID associatedJobId
) {
    public static EmailSummaryResponse fromDomain(Email domain) {
        return new EmailSummaryResponse(
            domain.getId(),
            domain.getExternalMessageId(),
            domain.getSender(),
            domain.getRecipient(),
            domain.getSubject(),
            domain.getSnippet(),
            domain.getReceivedAt(),
            domain.getClassification(),
            domain.getClassificationConfidence(),
            domain.getProcessingStatus(),
            domain.getExtractedCompanyName(),
            domain.getExtractedJobTitle(),
            domain.getAssociatedJobId()
        );
    }
}
