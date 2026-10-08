package com.jobcommandcenter.resume.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resume_projects")
public class ResumeProjectJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "resume_id", insertable = false, updatable = false)
    private UUID resumeId;

    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "role", length = 255)
    private String role;

    @Column(name = "technologies", columnDefinition = "TEXT")
    private String technologies;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "duration", length = 100)
    private String duration;

    @Column(name = "project_url", length = 1000)
    private String projectUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ResumeProjectJpaEntity() {}

    public ResumeProjectJpaEntity(UUID id,
                                 UUID resumeId,
                                 String projectName,
                                 String description,
                                 String role,
                                 String technologies,
                                 String responsibilities,
                                 String achievements,
                                 String duration,
                                 String projectUrl,
                                 int displayOrder,
                                 Instant createdAt) {
        this.id = id;
        this.resumeId = resumeId;
        this.projectName = projectName;
        this.description = description;
        this.role = role;
        this.technologies = technologies;
        this.responsibilities = responsibilities;
        this.achievements = achievements;
        this.duration = duration;
        this.projectUrl = projectUrl;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }

    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getProjectUrl() { return projectUrl; }
    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
