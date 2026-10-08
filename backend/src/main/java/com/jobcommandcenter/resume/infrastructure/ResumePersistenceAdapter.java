package com.jobcommandcenter.resume.infrastructure;

import com.jobcommandcenter.resume.domain.*;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ResumePersistenceAdapter implements ResumeRepository {

    private final SpringDataResumeRepository repository;
    private final SkillRepository skillRepository;

    public ResumePersistenceAdapter(SpringDataResumeRepository repository,
                                    SkillRepository skillRepository) {
        this.repository = repository;
        this.skillRepository = skillRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Resume> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resume> findByUserId(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Resume> findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    @Transactional
    public Resume save(Resume resume) {
        ResumeJpaEntity entity = toEntity(resume);
        ResumeJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndUserId(UUID id, UUID userId) {
        return repository.existsByIdAndUserId(id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    private Resume toDomain(ResumeJpaEntity entity) {
        Map<UUID, String> skillNames = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        List<ResumeExperience> experiences = entity.getExperiences().stream()
            .map(e -> new ResumeExperience(
                e.getId(),
                e.getCompany(),
                e.getJobTitle(),
                e.getStartDate(),
                e.getEndDate(),
                e.isCurrentlyWorking(),
                e.getLocation(),
                e.getDescription(),
                deserializeLines(e.getAchievements()),
                deserializeCommaSeparated(e.getTechnologies()),
                e.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        List<ResumeProject> projects = entity.getProjects().stream()
            .map(p -> new ResumeProject(
                p.getId(),
                p.getProjectName(),
                p.getDescription(),
                p.getRole(),
                deserializeCommaSeparated(p.getTechnologies()),
                deserializeLines(p.getResponsibilities()),
                deserializeLines(p.getAchievements()),
                p.getDuration(),
                p.getProjectUrl(),
                p.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        List<ResumeSkill> skills = entity.getSkills().stream()
            .map(s -> new ResumeSkill(
                s.getId(),
                s.getSkillId(),
                skillNames.getOrDefault(s.getSkillId(), "Unknown Skill"),
                s.getProficiency(),
                s.getYearsExperience()
            ))
            .collect(Collectors.toList());

        List<ResumeEducation> education = entity.getEducation().stream()
            .map(ed -> new ResumeEducation(
                ed.getId(),
                ed.getInstitution(),
                ed.getDegree(),
                ed.getFieldOfStudy(),
                ed.getStartYear(),
                ed.getEndYear(),
                ed.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        List<ResumeCertification> certifications = entity.getCertifications().stream()
            .map(c -> new ResumeCertification(
                c.getId(),
                c.getName(),
                c.getIssuingOrganization(),
                c.getIssueDate(),
                c.getExpiryDate(),
                c.getCredentialId(),
                c.getCredentialUrl(),
                c.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        return new Resume(
            entity.getId(),
            entity.getUserId(),
            entity.getName(),
            entity.getTitle(),
            entity.getSummary(),
            entity.getYearsOfExperience(),
            entity.getLocation(),
            entity.getContactEmail(),
            entity.getContactPhone(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            experiences,
            projects,
            skills,
            education,
            certifications
        );
    }

    private ResumeJpaEntity toEntity(Resume domain) {
        List<ResumeExperienceJpaEntity> experiences = domain.getExperiences().stream()
            .map(e -> new ResumeExperienceJpaEntity(
                e.getId(),
                domain.getId(),
                e.getCompany(),
                e.getJobTitle(),
                e.getStartDate(),
                e.getEndDate(),
                e.isCurrentlyWorking(),
                e.getLocation(),
                e.getDescription(),
                serializeLines(e.getAchievements()),
                serializeCommaSeparated(e.getTechnologies()),
                e.getDisplayOrder(),
                Instant.now()
            ))
            .collect(Collectors.toList());

        List<ResumeProjectJpaEntity> projects = domain.getProjects().stream()
            .map(p -> new ResumeProjectJpaEntity(
                p.getId(),
                domain.getId(),
                p.getProjectName(),
                p.getDescription(),
                p.getRole(),
                serializeCommaSeparated(p.getTechnologies()),
                serializeLines(p.getResponsibilities()),
                serializeLines(p.getAchievements()),
                p.getDuration(),
                p.getProjectUrl(),
                p.getDisplayOrder(),
                Instant.now()
            ))
            .collect(Collectors.toList());

        List<ResumeSkillJpaEntity> skills = domain.getSkills().stream()
            .map(s -> new ResumeSkillJpaEntity(
                s.getId(),
                domain.getId(),
                s.getSkillId(),
                s.getProficiency(),
                s.getYearsExperience(),
                Instant.now()
            ))
            .collect(Collectors.toList());

        List<ResumeEducationJpaEntity> education = domain.getEducation().stream()
            .map(ed -> new ResumeEducationJpaEntity(
                ed.getId(),
                domain.getId(),
                ed.getInstitution(),
                ed.getDegree(),
                ed.getFieldOfStudy(),
                ed.getStartYear(),
                ed.getEndYear(),
                ed.getDisplayOrder(),
                Instant.now()
            ))
            .collect(Collectors.toList());

        List<ResumeCertificationJpaEntity> certifications = domain.getCertifications().stream()
            .map(c -> new ResumeCertificationJpaEntity(
                c.getId(),
                domain.getId(),
                c.getName(),
                c.getIssuingOrganization(),
                c.getIssueDate(),
                c.getExpiryDate(),
                c.getCredentialId(),
                c.getCredentialUrl(),
                c.getDisplayOrder(),
                Instant.now()
            ))
            .collect(Collectors.toList());

        return new ResumeJpaEntity(
            domain.getId(),
            domain.getUserId(),
            domain.getName(),
            domain.getTitle(),
            domain.getSummary(),
            domain.getYearsOfExperience(),
            domain.getLocation(),
            domain.getContactEmail(),
            domain.getContactPhone(),
            domain.getStatus(),
            domain.getCreatedAt(),
            domain.getUpdatedAt(),
            experiences,
            projects,
            skills,
            education,
            certifications
        );
    }

    private List<String> deserializeLines(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        return Arrays.stream(text.split("\\r?\\n"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private String serializeLines(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join("\n", list);
    }

    private List<String> deserializeCommaSeparated(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        return Arrays.stream(text.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private String serializeCommaSeparated(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(", ", list);
    }
}
