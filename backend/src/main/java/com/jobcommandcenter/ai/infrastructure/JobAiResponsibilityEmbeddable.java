package com.jobcommandcenter.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class JobAiResponsibilityEmbeddable {

    @Column(name = "responsibility", nullable = false)
    private String responsibility;

    @Column(name = "is_inferred", nullable = false)
    private boolean isInferred;

    public JobAiResponsibilityEmbeddable() {}

    public JobAiResponsibilityEmbeddable(String responsibility, boolean isInferred) {
        this.responsibility = responsibility;
        this.isInferred = isInferred;
    }

    public String getResponsibility() {
        return responsibility;
    }

    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }

    public boolean isInferred() {
        return isInferred;
    }

    public void setInferred(boolean inferred) {
        isInferred = inferred;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobAiResponsibilityEmbeddable that = (JobAiResponsibilityEmbeddable) o;
        return Objects.equals(responsibility, that.responsibility);
    }

    @Override
    public int hashCode() {
        return Objects.hash(responsibility);
    }
}
