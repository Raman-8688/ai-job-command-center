package com.jobcommandcenter.ai.infrastructure;

import com.jobcommandcenter.ai.domain.AIAnalysisStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "job_ai_analyses", uniqueConstraints = {
    @UniqueConstraint(name = "uq_job_ai_analyses_job_version", columnNames = {"job_id", "version"})
})
public class JobAiAnalysisJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "prompt_version", nullable = false, length = 50)
    private String promptVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AIAnalysisStatus status;

    @Column(name = "normalized_title", length = 255)
    private String normalizedTitle;

    @Column(name = "seniority_level", length = 50)
    private String seniorityLevel;

    @Column(name = "experience_expectations", length = 255)
    private String experienceExpectations;

    @Column(name = "confidence", precision = 3, scale = 2)
    private BigDecimal confidence;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_ai_responsibilities", joinColumns = @JoinColumn(name = "analysis_id"))
    private Set<JobAiResponsibilityEmbeddable> responsibilities = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_ai_technologies", joinColumns = @JoinColumn(name = "analysis_id"))
    private Set<JobAiTechnologyEmbeddable> technologies = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_ai_requirements", joinColumns = @JoinColumn(name = "analysis_id"))
    private Set<JobAiRequirementEmbeddable> requirements = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_ai_red_flags", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "red_flag", nullable = false)
    private Set<String> redFlags = new HashSet<>();

    public JobAiAnalysisJpaEntity() {}

    public JobAiAnalysisJpaEntity(UUID id,
                                  UUID jobId,
                                  int version,
                                  String provider,
                                  String model,
                                  String promptVersion,
                                  AIAnalysisStatus status,
                                  String normalizedTitle,
                                  String seniorityLevel,
                                  String experienceExpectations,
                                  BigDecimal confidence,
                                  String errorMessage,
                                  Instant createdAt,
                                  Instant completedAt,
                                  Set<JobAiResponsibilityEmbeddable> responsibilities,
                                  Set<JobAiTechnologyEmbeddable> technologies,
                                  Set<JobAiRequirementEmbeddable> requirements,
                                  Set<String> redFlags) {
        this.id = id;
        this.jobId = jobId;
        this.version = version;
        this.provider = provider;
        this.model = model;
        this.promptVersion = promptVersion;
        this.status = status;
        this.normalizedTitle = normalizedTitle;
        this.seniorityLevel = seniorityLevel;
        this.experienceExpectations = experienceExpectations;
        this.confidence = confidence;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.responsibilities = responsibilities != null ? responsibilities : new HashSet<>();
        this.technologies = technologies != null ? technologies : new HashSet<>();
        this.requirements = requirements != null ? requirements : new HashSet<>();
        this.redFlags = redFlags != null ? redFlags : new HashSet<>();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPromptVersion() { return promptVersion; }
    public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }

    public AIAnalysisStatus getStatus() { return status; }
    public void setStatus(AIAnalysisStatus status) { this.status = status; }

    public String getNormalizedTitle() { return normalizedTitle; }
    public void setNormalizedTitle(String normalizedTitle) { this.normalizedTitle = normalizedTitle; }

    public String getSeniorityLevel() { return seniorityLevel; }
    public void setSeniorityLevel(String seniorityLevel) { this.seniorityLevel = seniorityLevel; }

    public String getExperienceExpectations() { return experienceExpectations; }
    public void setExperienceExpectations(String experienceExpectations) { this.experienceExpectations = experienceExpectations; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Set<JobAiResponsibilityEmbeddable> getResponsibilities() { return responsibilities; }
    public void setResponsibilities(Set<JobAiResponsibilityEmbeddable> responsibilities) { this.responsibilities = responsibilities; }

    public Set<JobAiTechnologyEmbeddable> getTechnologies() { return technologies; }
    public void setTechnologies(Set<JobAiTechnologyEmbeddable> technologies) { this.technologies = technologies; }

    public Set<JobAiRequirementEmbeddable> getRequirements() { return requirements; }
    public void setRequirements(Set<JobAiRequirementEmbeddable> requirements) { this.requirements = requirements; }

    public Set<String> getRedFlags() { return redFlags; }
    public void setRedFlags(Set<String> redFlags) { this.redFlags = redFlags; }
}
