package com.jobcommandcenter.ai.infrastructure;

import com.jobcommandcenter.ai.domain.TechnologyCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

@Embeddable
public class JobAiTechnologyEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private TechnologyCategory category;

    @Column(name = "technology", nullable = false, length = 100)
    private String technology;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    public JobAiTechnologyEmbeddable() {}

    public JobAiTechnologyEmbeddable(TechnologyCategory category, String technology, boolean isRequired) {
        this.category = category;
        this.technology = technology;
        this.isRequired = isRequired;
    }

    public TechnologyCategory getCategory() {
        return category;
    }

    public void setCategory(TechnologyCategory category) {
        this.category = category;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobAiTechnologyEmbeddable that = (JobAiTechnologyEmbeddable) o;
        return category == that.category && Objects.equals(technology, that.technology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, technology);
    }
}
