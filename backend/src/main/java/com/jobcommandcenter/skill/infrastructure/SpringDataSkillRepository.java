package com.jobcommandcenter.skill.infrastructure;

import com.jobcommandcenter.skill.domain.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSkillRepository extends JpaRepository<SkillJpaEntity, UUID> {

    Optional<SkillJpaEntity> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);

    List<SkillJpaEntity> findByCategory(SkillCategory category);

    List<SkillJpaEntity> findByNameContainingIgnoreCase(String query);
}
