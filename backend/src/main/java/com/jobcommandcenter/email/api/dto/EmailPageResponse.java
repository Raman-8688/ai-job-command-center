package com.jobcommandcenter.email.api.dto;

import java.util.List;

/**
 * Paginated response container for email listings.
 */
public record EmailPageResponse(
    List<EmailSummaryResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {
    public static EmailPageResponse of(List<EmailSummaryResponse> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        boolean hasNext = (long) (page + 1) * size < totalElements;
        return new EmailPageResponse(content, page, size, totalElements, totalPages, hasNext);
    }
}
