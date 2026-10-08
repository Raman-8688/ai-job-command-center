package com.jobcommandcenter.skill.application;

import com.jobcommandcenter.common.error.ConflictException;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.skill.api.CreateSkillRequest;
import com.jobcommandcenter.skill.api.SkillResponse;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SkillCatalogService {

    private final SkillRepository skillRepository;

    public SkillCatalogService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional
    public SkillResponse createSkill(CreateSkillRequest request) {
        String normalized = Skill.normalize(request.name());
        if (skillRepository.existsByNormalizedName(normalized)) {
            throw new ConflictException("Skill '" + request.name() + "' already exists in catalog");
        }

        Skill skill = Skill.createNew(request.name(), request.category());
        Skill saved = skillRepository.save(skill);
        return SkillResponse.fromDomain(saved);
    }

    public Skill getSkillEntityById(UUID id) {
        return skillRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Skill not found with ID: " + id));
    }

    public SkillResponse getSkillById(UUID id) {
        return SkillResponse.fromDomain(getSkillEntityById(id));
    }

    public List<SkillResponse> searchSkills(String query, SkillCategory category) {
        List<Skill> skills;

        if (query != null && !query.isBlank() && category != null) {
            skills = skillRepository.searchByName(query).stream()
                .filter(s -> s.getCategory() == category)
                .collect(Collectors.toList());
        } else if (query != null && !query.isBlank()) {
            skills = skillRepository.searchByName(query);
        } else if (category != null) {
            skills = skillRepository.findByCategory(category);
        } else {
            skills = skillRepository.findAll();
        }

        return skills.stream()
            .map(SkillResponse::fromDomain)
            .collect(Collectors.toList());
    }
}
