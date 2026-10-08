package com.jobcommandcenter.ai.domain;

import java.util.Objects;

/**
 * Value object representing a technology identified in a job description.
 */
public record AnalyzedTechnology(
    String technology,
    TechnologyCategory category,
    boolean required
) {
    public AnalyzedTechnology {
        Objects.requireNonNull(technology, "Technology name cannot be null");
        technology = technology.trim();
        category = category != null ? category : TechnologyCategory.OTHER;
    }
}
