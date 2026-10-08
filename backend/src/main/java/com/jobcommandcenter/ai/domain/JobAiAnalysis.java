package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Domain entity representing an AI-derived analysis of a canonical job posting.
 * Distinct from canonical job source truth.
 */
public class JobAiAnalysis {

    private final UUID id;
    private final UUID jobId;
    private final int version;
    private final String provider;
    private final String model;
    private final String promptVersion;
    private AIAnalysisStatus status;
    private String normalizedTitle;
    private String seniorityLevel;
    private List<String> coreResponsibilities;
    private List<String> inferredResponsibilities;
    private List<AnalyzedTechnology> technologies;
    private List<String> educationRequirements;
    private List<String> certificationRequirements;
    private String experienceExpectations;
    private List<String> potentialRedFlags;
    private BigDecimal confidence;
    private String errorMessage;
    private final Instant createdAt;
    private Instant completedAt;

    public JobAiAnalysis(UUID id,
                         UUID jobId,
                         int version,
                         String provider,
                         String model,
                         String promptVersion,
                         AIAnalysisStatus status,
                         String normalizedTitle,
                         String seniorityLevel,
                         List<String> coreResponsibilities,
                         List<String> inferredResponsibilities,
                         List<AnalyzedTechnology> technologies,
                         List<String> educationRequirements,
                         List<String> certificationRequirements,
                         String experienceExpectations,
                         List<String> potentialRedFlags,
                         BigDecimal confidence,
                         String errorMessage,
                         Instant createdAt,
                         Instant completedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.jobId = Objects.requireNonNull(jobId, "Job ID cannot be null");
        this.version = Math.max(1, version);
        this.provider = Objects.requireNonNull(provider, "Provider cannot be null");
        this.model = Objects.requireNonNull(model, "Model cannot be null");
        this.promptVersion = Objects.requireNonNull(promptVersion, "Prompt version cannot be null");
        this.status = status != null ? status : AIAnalysisStatus.PENDING;
        this.normalizedTitle = normalizedTitle;
        this.seniorityLevel = seniorityLevel != null ? seniorityLevel : "UNKNOWN";
        this.coreResponsibilities = coreResponsibilities != null ? new ArrayList<>(coreResponsibilities) : new ArrayList<>();
        this.inferredResponsibilities = inferredResponsibilities != null ? new ArrayList<>(inferredResponsibilities) : new ArrayList<>();
        this.technologies = technologies != null ? new ArrayList<>(technologies) : new ArrayList<>();
        this.educationRequirements = educationRequirements != null ? new ArrayList<>(educationRequirements) : new ArrayList<>();
        this.certificationRequirements = certificationRequirements != null ? new ArrayList<>(certificationRequirements) : new ArrayList<>();
        this.experienceExpectations = experienceExpectations;
        this.potentialRedFlags = potentialRedFlags != null ? new ArrayList<>(potentialRedFlags) : new ArrayList<>();
        this.confidence = confidence != null ? confidence : BigDecimal.ZERO;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.completedAt = completedAt;
    }

    public static JobAiAnalysis createPending(UUID jobId,
                                              int version,
                                              String provider,
                                              String model,
                                              String promptVersion) {
        return new JobAiAnalysis(
            UUID.randomUUID(),
            jobId,
            version,
            provider,
            model,
            promptVersion,
            AIAnalysisStatus.PENDING,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            null,
            List.of(),
            BigDecimal.ZERO,
            null,
            Instant.now(),
            null
        );
    }

    public void complete(AIJobAnalysisResponse response) {
        this.status = AIAnalysisStatus.COMPLETED;
        this.normalizedTitle = response.normalizedTitle();
        this.seniorityLevel = response.seniorityLevel() != null ? response.seniorityLevel() : "UNKNOWN";
        this.coreResponsibilities = new ArrayList<>(response.coreResponsibilities());
        this.inferredResponsibilities = new ArrayList<>(response.inferredResponsibilities());
        this.technologies = new ArrayList<>(response.technologies());
        this.educationRequirements = new ArrayList<>(response.educationRequirements());
        this.certificationRequirements = new ArrayList<>(response.certificationRequirements());
        this.experienceExpectations = response.experienceExpectations();
        this.potentialRedFlags = new ArrayList<>(response.potentialRedFlags());
        this.confidence = response.confidence();
        this.errorMessage = null;
        this.completedAt = Instant.now();
    }

    public void fail(String sanitizedMessage) {
        this.status = AIAnalysisStatus.FAILED;
        this.errorMessage = sanitizedMessage;
        this.completedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getJobId() { return jobId; }
    public int getVersion() { return version; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public String getPromptVersion() { return promptVersion; }
    public AIAnalysisStatus getStatus() { return status; }
    public String getNormalizedTitle() { return normalizedTitle; }
    public String getSeniorityLevel() { return seniorityLevel; }
    public List<String> getCoreResponsibilities() { return Collections.unmodifiableList(coreResponsibilities); }
    public List<String> getInferredResponsibilities() { return Collections.unmodifiableList(inferredResponsibilities); }
    public List<AnalyzedTechnology> getTechnologies() { return Collections.unmodifiableList(technologies); }
    public List<String> getEducationRequirements() { return Collections.unmodifiableList(educationRequirements); }
    public List<String> getCertificationRequirements() { return Collections.unmodifiableList(certificationRequirements); }
    public String getExperienceExpectations() { return experienceExpectations; }
    public List<String> getPotentialRedFlags() { return Collections.unmodifiableList(potentialRedFlags); }
    public BigDecimal getConfidence() { return confidence; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
