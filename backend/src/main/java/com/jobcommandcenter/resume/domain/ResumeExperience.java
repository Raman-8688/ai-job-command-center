package com.jobcommandcenter.resume.domain;

import java.time.LocalDate;
import java.util.*;

/**
 * Domain entity representing a work experience entry on a resume.
 */
public class ResumeExperience {

    private final UUID id;
    private String company;
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean currentlyWorking;
    private String location;
    private String description;
    private List<String> achievements;
    private List<String> technologies;
    private int displayOrder;

    public ResumeExperience(UUID id,
                            String company,
                            String jobTitle,
                            LocalDate startDate,
                            LocalDate endDate,
                            boolean currentlyWorking,
                            String location,
                            String description,
                            List<String> achievements,
                            List<String> technologies,
                            int displayOrder) {
        this.id = Objects.requireNonNull(id, "Experience ID cannot be null");
        this.company = Objects.requireNonNull(company, "Company cannot be null").trim();
        this.jobTitle = Objects.requireNonNull(jobTitle, "Job title cannot be null").trim();
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentlyWorking = currentlyWorking;
        this.location = location;
        this.description = description;
        this.achievements = achievements != null ? new ArrayList<>(achievements) : new ArrayList<>();
        this.technologies = technologies != null ? new ArrayList<>(technologies) : new ArrayList<>();
        this.displayOrder = displayOrder;
    }

    public static ResumeExperience create(String company,
                                          String jobTitle,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          boolean currentlyWorking,
                                          String location,
                                          String description,
                                          List<String> achievements,
                                          List<String> technologies,
                                          int displayOrder) {
        return new ResumeExperience(
            UUID.randomUUID(),
            company,
            jobTitle,
            startDate,
            endDate,
            currentlyWorking,
            location,
            description,
            achievements,
            technologies,
            displayOrder
        );
    }

    public UUID getId() { return id; }
    public String getCompany() { return company; }
    public String getJobTitle() { return jobTitle; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isCurrentlyWorking() { return currentlyWorking; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public List<String> getAchievements() { return Collections.unmodifiableList(achievements); }
    public List<String> getTechnologies() { return Collections.unmodifiableList(technologies); }
    public int getDisplayOrder() { return displayOrder; }

    public void update(String company,
                       String jobTitle,
                       LocalDate startDate,
                       LocalDate endDate,
                       boolean currentlyWorking,
                       String location,
                       String description,
                       List<String> achievements,
                       List<String> technologies,
                       int displayOrder) {
        this.company = Objects.requireNonNull(company, "Company cannot be null").trim();
        this.jobTitle = Objects.requireNonNull(jobTitle, "Job title cannot be null").trim();
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentlyWorking = currentlyWorking;
        this.location = location;
        this.description = description;
        this.achievements = achievements != null ? new ArrayList<>(achievements) : new ArrayList<>();
        this.technologies = technologies != null ? new ArrayList<>(technologies) : new ArrayList<>();
        this.displayOrder = displayOrder;
    }
}
