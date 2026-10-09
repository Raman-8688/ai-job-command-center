package com.jobcommandcenter.application.api.dto;

import java.util.UUID;

public record LinkResumeRequest(
    UUID resumeId,
    UUID tailoredResumeId,
    String notes
) {}
