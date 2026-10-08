package com.jobcommandcenter.ai.api.dto;

import com.jobcommandcenter.ai.domain.JobAiAnalysis;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record JobAiAnalysisResponseDto(
    UUID id,
    UUID jobId,
    int version,
    String provider,
    String model,
    String promptVersion,
    String status,
    String normalizedTitle,
    String seniorityLevel,
    List<String> coreResponsibilities,
    List<String> inferredResponsibilities,
    List<AnalyzedTechnologyDto> technologies,
    List<String> educationRequirements,
    List<String> certificationRequirements,
    String experienceExpectations,
    List<String> potentialRedFlags,
    BigDecimal confidence,
    String errorMessage,
    Instant createdAt,
    Instant completedAt
) {
    public static JobAiAnalysisResponseDto fromDomain(JobAiAnalysis domain) {
        List<AnalyzedTechnologyDto> techDtos = domain.getTechnologies().stream()
            .map(t -> new AnalyzedTechnologyDto(t.technology(), t.category().name(), t.required()))
            .collect(Collectors.toList());

        return new JobAiAnalysisResponseDto(
            domain.getId(),
            domain.getJobId(),
            domain.getVersion(),
            domain.getProvider(),
            domain.getModel(),
            domain.getPromptVersion(),
            domain.getStatus().name(),
            domain.getNormalizedTitle(),
            domain.getSeniorityLevel(),
            domain.getCoreResponsibilities(),
            domain.getInferredResponsibilities(),
            techDtos,
            domain.getEducationRequirements(),
            domain.getCertificationRequirements(),
            domain.getExperienceExpectations(),
            domain.getPotentialRedFlags(),
            domain.getConfidence(),
            domain.getErrorMessage(),
            domain.getCreatedAt(),
            domain.getCompletedAt()
        );
    }
}
