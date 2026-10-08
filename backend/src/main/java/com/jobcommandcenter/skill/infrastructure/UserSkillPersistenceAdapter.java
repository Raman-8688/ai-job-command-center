package com.jobcommandcenter.skill.infrastructure;

import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserSkillPersistenceAdapter implements UserSkillRepository {

    private final SpringDataUserSkillRepository springDataUserSkillRepository;

    public UserSkillPersistenceAdapter(SpringDataUserSkillRepository springDataUserSkillRepository) {
        this.springDataUserSkillRepository = springDataUserSkillRepository;
    }

    @Override
    public Optional<UserSkill> findById(UUID id) {
        return springDataUserSkillRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId) {
        return springDataUserSkillRepository.findByUserIdAndSkillId(userId, skillId).map(this::toDomain);
    }

    @Override
    public List<UserSkill> findByUserId(UUID userId) {
        return springDataUserSkillRepository.findByUserId(userId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public UserSkill save(UserSkill userSkill) {
        UserSkillJpaEntity entity = toEntity(userSkill);
        UserSkillJpaEntity saved = springDataUserSkillRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataUserSkillRepository.deleteById(id);
    }

    @Override
    public boolean existsByUserIdAndSkillId(UUID userId, UUID skillId) {
        return springDataUserSkillRepository.existsByUserIdAndSkillId(userId, skillId);
    }

    private UserSkill toDomain(UserSkillJpaEntity entity) {
        return new UserSkill(
            entity.getId(),
            entity.getUserId(),
            entity.getSkillId(),
            entity.getProficiency(),
            entity.getYearsExperience(),
            entity.isVerified(),
            entity.getVerificationSource(),
            entity.getNotes(),
            entity.getLastVerifiedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private UserSkillJpaEntity toEntity(UserSkill userSkill) {
        return new UserSkillJpaEntity(
            userSkill.getId(),
            userSkill.getUserId(),
            userSkill.getSkillId(),
            userSkill.getProficiency(),
            userSkill.getYearsExperience(),
            userSkill.isVerified(),
            userSkill.getVerificationSource(),
            userSkill.getNotes(),
            userSkill.getLastVerifiedAt(),
            userSkill.getCreatedAt(),
            userSkill.getUpdatedAt()
        );
    }
}
