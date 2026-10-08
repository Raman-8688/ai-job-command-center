package com.jobcommandcenter.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class JobAiRequirementEmbeddable {

    @Column(name = "requirement_type", nullable = false, length = 50)
    private String requirementType;

    @Column(name = "requirement", nullable = false, length = 255)
    private String requirement;

    public JobAiRequirementEmbeddable() {}

    public JobAiRequirementEmbeddable(String requirementType, String requirement) {
        this.requirementType = requirementType;
        this.requirement = requirement;
    }

    public String getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobAiRequirementEmbeddable that = (JobAiRequirementEmbeddable) o;
        return Objects.equals(requirementType, that.requirementType) &&
               Objects.equals(requirement, that.requirement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirementType, requirement);
    }
}
