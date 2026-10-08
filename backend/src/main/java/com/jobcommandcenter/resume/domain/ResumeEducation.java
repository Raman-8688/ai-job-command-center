package com.jobcommandcenter.resume.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an education entry on a resume.
 */
public class ResumeEducation {

    private final UUID id;
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    private int displayOrder;

    public ResumeEducation(UUID id,
                           String institution,
                           String degree,
                           String fieldOfStudy,
                           Integer startYear,
                           Integer endYear,
                           int displayOrder) {
        this.id = Objects.requireNonNull(id, "Education ID cannot be null");
        this.institution = Objects.requireNonNull(institution, "Institution cannot be null").trim();
        this.degree = degree;
        this.fieldOfStudy = fieldOfStudy;
        this.startYear = startYear;
        this.endYear = endYear;
        this.displayOrder = displayOrder;
    }

    public static ResumeEducation create(String institution,
                                         String degree,
                                         String fieldOfStudy,
                                         Integer startYear,
                                         Integer endYear,
                                         int displayOrder) {
        return new ResumeEducation(
            UUID.randomUUID(),
            institution,
            degree,
            fieldOfStudy,
            startYear,
            endYear,
            displayOrder
        );
    }

    public UUID getId() { return id; }
    public String getInstitution() { return institution; }
    public String getDegree() { return degree; }
    public String getFieldOfStudy() { return fieldOfStudy; }
    public Integer getStartYear() { return startYear; }
    public Integer getEndYear() { return endYear; }
    public int getDisplayOrder() { return displayOrder; }

    public void update(String institution,
                       String degree,
                       String fieldOfStudy,
                       Integer startYear,
                       Integer endYear,
                       int displayOrder) {
        this.institution = Objects.requireNonNull(institution, "Institution cannot be null").trim();
        this.degree = degree;
        this.fieldOfStudy = fieldOfStudy;
        this.startYear = startYear;
        this.endYear = endYear;
        this.displayOrder = displayOrder;
    }
}
