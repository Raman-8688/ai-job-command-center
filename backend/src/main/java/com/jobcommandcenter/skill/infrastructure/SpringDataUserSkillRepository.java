package com.jobcommandcenter.skill.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserSkillRepository extends JpaRepository<UserSkillJpaEntity, UUID> {

    List<UserSkillJpaEntity> findByUserId(UUID userId);

    Optional<UserSkillJpaEntity> findByUserIdAndSkillId(UUID userId, UUID skillId);

    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);
}
