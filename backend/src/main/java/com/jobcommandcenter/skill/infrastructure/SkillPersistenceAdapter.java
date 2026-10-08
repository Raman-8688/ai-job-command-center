package com.jobcommandcenter.skill.infrastructure;

import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SkillPersistenceAdapter implements SkillRepository {

    private final SpringDataSkillRepository springDataSkillRepository;

    public SkillPersistenceAdapter(SpringDataSkillRepository springDataSkillRepository) {
        this.springDataSkillRepository = springDataSkillRepository;
    }

    @Override
    public Optional<Skill> findById(UUID id) {
        return springDataSkillRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Skill> findByNormalizedName(String normalizedName) {
        return springDataSkillRepository.findByNormalizedName(normalizedName).map(this::toDomain);
    }

    @Override
    public List<Skill> findAll() {
        return springDataSkillRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Skill> findByCategory(SkillCategory category) {
        return springDataSkillRepository.findByCategory(category).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Skill> searchByName(String query) {
        return springDataSkillRepository.findByNameContainingIgnoreCase(query).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Skill save(Skill skill) {
        SkillJpaEntity entity = toEntity(skill);
        SkillJpaEntity saved = springDataSkillRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByNormalizedName(String normalizedName) {
        return springDataSkillRepository.existsByNormalizedName(normalizedName);
    }

    private Skill toDomain(SkillJpaEntity entity) {
        return new Skill(
            entity.getId(),
            entity.getName(),
            entity.getNormalizedName(),
            entity.getCategory(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private SkillJpaEntity toEntity(Skill skill) {
        return new SkillJpaEntity(
            skill.getId(),
            skill.getName(),
            skill.getNormalizedName(),
            skill.getCategory(),
            skill.getCreatedAt(),
            skill.getUpdatedAt()
        );
    }
}
