package com.jobcommandcenter.ai.api.dto;

public record AnalyzedTechnologyDto(
    String technology,
    String category,
    boolean required
) {}
