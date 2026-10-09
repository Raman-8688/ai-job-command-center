package com.jobcommandcenter.email.domain;

import java.util.UUID;

/**
 * Filter and pagination criteria for querying emails.
 */
public record EmailSearchCriteria(
    UUID userId,
    EmailClassification classification,
    EmailProcessingStatus processingStatus,
    UUID associatedJobId,
    String query,
    int page,
    int size
) {
    public EmailSearchCriteria {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
    }
}
