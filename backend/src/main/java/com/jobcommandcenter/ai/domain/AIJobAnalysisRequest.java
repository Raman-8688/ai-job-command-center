package com.jobcommandcenter.ai.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Request payload dispatched to an AIProvider SPI.
 */
public record AIJobAnalysisRequest(
    UUID jobId,
    String title,
    String companyName,
    String rawDescription,
    String promptVersion
) {
    public AIJobAnalysisRequest {
        Objects.requireNonNull(jobId, "Job ID cannot be null");
        Objects.requireNonNull(rawDescription, "Job description cannot be null");
    }
}
