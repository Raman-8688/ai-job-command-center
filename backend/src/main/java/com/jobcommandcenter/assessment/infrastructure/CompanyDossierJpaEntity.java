package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.CompanyDossier;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "company_dossiers")
public class CompanyDossierJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_tier", length = 50)
    private String companyTier;

    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;

    @Column(name = "engineering_scale", columnDefinition = "TEXT")
    private String engineeringScale;

    @Column(name = "core_tech_stack", columnDefinition = "TEXT")
    private String coreTechStack;

    @Column(name = "engineering_culture", columnDefinition = "TEXT")
    private String engineeringCulture;

    @Column(name = "architecture_focus", columnDefinition = "TEXT")
    private String architectureFocus;

    @Column(name = "tailored_talking_points", columnDefinition = "TEXT")
    private String tailoredTalkingPoints;

    @Column(name = "interviewer_questions", columnDefinition = "TEXT")
    private String interviewerQuestions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CompanyDossierJpaEntity() {}

    public static CompanyDossierJpaEntity fromDomain(CompanyDossier domain) {
        CompanyDossierJpaEntity entity = new CompanyDossierJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.jobId = domain.getJobId();
        entity.companyName = domain.getCompanyName();
        entity.companyTier = domain.getCompanyTier();
        entity.overview = domain.getOverview();
        entity.engineeringScale = domain.getEngineeringScale();
        entity.coreTechStack = domain.getCoreTechStack();
        entity.engineeringCulture = domain.getEngineeringCulture();
        entity.architectureFocus = domain.getArchitectureFocus();
        entity.tailoredTalkingPoints = domain.getTailoredTalkingPoints();
        entity.interviewerQuestions = domain.getInterviewerQuestions();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    public CompanyDossier toDomain() {
        return new CompanyDossier(
            this.id,
            this.userId,
            this.jobId,
            this.companyName,
            this.companyTier,
            this.overview,
            this.engineeringScale,
            this.coreTechStack,
            this.engineeringCulture,
            this.architectureFocus,
            this.tailoredTalkingPoints,
            this.interviewerQuestions,
            this.createdAt,
            this.updatedAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyTier() { return companyTier; }
    public void setCompanyTier(String companyTier) { this.companyTier = companyTier; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public String getEngineeringScale() { return engineeringScale; }
    public void setEngineeringScale(String engineeringScale) { this.engineeringScale = engineeringScale; }
    public String getCoreTechStack() { return coreTechStack; }
    public void setCoreTechStack(String coreTechStack) { this.coreTechStack = coreTechStack; }
    public String getEngineeringCulture() { return engineeringCulture; }
    public void setEngineeringCulture(String engineeringCulture) { this.engineeringCulture = engineeringCulture; }
    public String getArchitectureFocus() { return architectureFocus; }
    public void setArchitectureFocus(String architectureFocus) { this.architectureFocus = architectureFocus; }
    public String getTailoredTalkingPoints() { return tailoredTalkingPoints; }
    public void setTailoredTalkingPoints(String tailoredTalkingPoints) { this.tailoredTalkingPoints = tailoredTalkingPoints; }
    public String getInterviewerQuestions() { return interviewerQuestions; }
    public void setInterviewerQuestions(String interviewerQuestions) { this.interviewerQuestions = interviewerQuestions; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
