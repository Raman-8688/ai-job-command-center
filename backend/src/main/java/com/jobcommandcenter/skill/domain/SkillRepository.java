package com.jobcommandcenter.skill.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository {

    Optional<Skill> findById(UUID id);

    Optional<Skill> findByNormalizedName(String normalizedName);

    List<Skill> findAll();

    List<Skill> findByCategory(SkillCategory category);

    List<Skill> searchByName(String query);

    Skill save(Skill skill);

    boolean existsByNormalizedName(String normalizedName);
}
