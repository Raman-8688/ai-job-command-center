package com.jobcommandcenter.skill.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSkillRepository {

    Optional<UserSkill> findById(UUID id);

    Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);

    List<UserSkill> findByUserId(UUID userId);

    UserSkill save(UserSkill userSkill);

    void deleteById(UUID id);

    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);
}
