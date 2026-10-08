package com.jobcommandcenter.resume.domain;

import java.util.*;

/**
 * Domain entity representing a personal or professional project on a resume.
 */
public class ResumeProject {

    private final UUID id;
    private String projectName;
    private String description;
    private String role;
    private List<String> technologies;
    private List<String> responsibilities;
    private List<String> achievements;
    private String duration;
    private String projectUrl;
    private int displayOrder;

    public ResumeProject(UUID id,
                         String projectName,
                         String description,
                         String role,
                         List<String> technologies,
                         List<String> responsibilities,
                         List<String> achievements,
                         String duration,
                         String projectUrl,
                         int displayOrder) {
        this.id = Objects.requireNonNull(id, "Project ID cannot be null");
        this.projectName = Objects.requireNonNull(projectName, "Project name cannot be null").trim();
        this.description = description;
        this.role = role;
        this.technologies = technologies != null ? new ArrayList<>(technologies) : new ArrayList<>();
        this.responsibilities = responsibilities != null ? new ArrayList<>(responsibilities) : new ArrayList<>();
        this.achievements = achievements != null ? new ArrayList<>(achievements) : new ArrayList<>();
        this.duration = duration;
        this.projectUrl = projectUrl;
        this.displayOrder = displayOrder;
    }

    public static ResumeProject create(String projectName,
                                       String description,
                                       String role,
                                       List<String> technologies,
                                       List<String> responsibilities,
                                       List<String> achievements,
                                       String duration,
                                       String projectUrl,
                                       int displayOrder) {
        return new ResumeProject(
            UUID.randomUUID(),
            projectName,
            description,
            role,
            technologies,
            responsibilities,
            achievements,
            duration,
            projectUrl,
            displayOrder
        );
    }

    public UUID getId() { return id; }
    public String getProjectName() { return projectName; }
    public String getDescription() { return description; }
    public String getRole() { return role; }
    public List<String> getTechnologies() { return Collections.unmodifiableList(technologies); }
    public List<String> getResponsibilities() { return Collections.unmodifiableList(responsibilities); }
    public List<String> getAchievements() { return Collections.unmodifiableList(achievements); }
    public String getDuration() { return duration; }
    public String getProjectUrl() { return projectUrl; }
    public int getDisplayOrder() { return displayOrder; }

    public void update(String projectName,
                       String description,
                       String role,
                       List<String> technologies,
                       List<String> responsibilities,
                       List<String> achievements,
                       String duration,
                       String projectUrl,
                       int displayOrder) {
        this.projectName = Objects.requireNonNull(projectName, "Project name cannot be null").trim();
        this.description = description;
        this.role = role;
        this.technologies = technologies != null ? new ArrayList<>(technologies) : new ArrayList<>();
        this.responsibilities = responsibilities != null ? new ArrayList<>(responsibilities) : new ArrayList<>();
        this.achievements = achievements != null ? new ArrayList<>(achievements) : new ArrayList<>();
        this.duration = duration;
        this.projectUrl = projectUrl;
        this.displayOrder = displayOrder;
    }
}
