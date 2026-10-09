package com.jobcommandcenter.application.api.dto;

import java.util.List;

public record ApplicationPageResponse(
    List<JobApplicationSummaryResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
