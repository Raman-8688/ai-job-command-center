package com.jobcommandcenter.skill.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a standardized skill in the catalog.
 */
public class Skill {

    private final UUID id;
    private final String name;
    private final String normalizedName;
    private final SkillCategory category;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Skill(UUID id,
                 String name,
                 String normalizedName,
                 SkillCategory category,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Skill ID cannot be null");
        this.name = validateName(name);
        this.normalizedName = normalizedName != null ? normalizedName : normalize(name);
        this.category = Objects.requireNonNull(category, "Skill category cannot be null");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Skill createNew(String name, SkillCategory category) {
        String trimmed = validateName(name);
        Instant now = Instant.now();
        return new Skill(
            UUID.randomUUID(),
            trimmed,
            normalize(trimmed),
            category,
            now,
            now
        );
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().toLowerCase();
    }

    private static String validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Skill name cannot be blank");
        }
        return name.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
