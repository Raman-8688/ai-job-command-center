package com.jobcommandcenter.application.domain;

import java.util.UUID;

/**
 * Criteria for querying and filtering a candidate's job applications.
 */
public record JobApplicationSearchCriteria(
    UUID userId,
    ApplicationStatus status,
    String search,
    int page,
    int size
) {
    public JobApplicationSearchCriteria {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
    }
}
