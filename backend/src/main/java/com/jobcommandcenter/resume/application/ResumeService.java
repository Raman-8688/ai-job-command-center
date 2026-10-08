package com.jobcommandcenter.resume.application;

import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.resume.api.dto.*;
import com.jobcommandcenter.resume.domain.*;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final SkillRepository skillRepository;

    public ResumeService(ResumeRepository resumeRepository, SkillRepository skillRepository) {
        this.resumeRepository = resumeRepository;
        this.skillRepository = skillRepository;
    }

    public ResumeResponse createResume(UUID userId, CreateResumeRequest request) {
        Resume resume = Resume.create(
            userId,
            request.name(),
            request.title(),
            request.summary(),
            request.yearsOfExperience(),
            request.location(),
            request.contactEmail(),
            request.contactPhone()
        );
        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(UUID userId, UUID resumeId) {
        Resume resume = getOwnedResume(userId, resumeId);
        return ResumeResponse.fromDomain(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeSummaryResponse> getUserResumes(UUID userId) {
        return resumeRepository.findByUserId(userId).stream()
            .map(ResumeSummaryResponse::fromDomain)
            .collect(Collectors.toList());
    }

    public ResumeResponse updateResume(UUID userId, UUID resumeId, UpdateResumeRequest request) {
        Resume resume = getOwnedResume(userId, resumeId);

        resume.updateMetadata(
            request.name(),
            request.title(),
            request.summary(),
            request.yearsOfExperience(),
            request.location(),
            request.contactEmail(),
            request.contactPhone()
        );

        if (request.experiences() != null) {
            List<ResumeExperience> experiences = request.experiences().stream()
                .map(e -> new ResumeExperience(
                    e.id() != null ? e.id() : UUID.randomUUID(),
                    e.company(),
                    e.jobTitle(),
                    e.startDate(),
                    e.endDate(),
                    e.currentlyWorking(),
                    e.location(),
                    e.description(),
                    e.achievements(),
                    e.technologies(),
                    e.displayOrder()
                ))
                .collect(Collectors.toList());
            resume.setExperiences(experiences);
        }

        if (request.projects() != null) {
            List<ResumeProject> projects = request.projects().stream()
                .map(p -> new ResumeProject(
                    p.id() != null ? p.id() : UUID.randomUUID(),
                    p.projectName(),
                    p.description(),
                    p.role(),
                    p.technologies(),
                    p.responsibilities(),
                    p.achievements(),
                    p.duration(),
                    p.projectUrl(),
                    p.displayOrder()
                ))
                .collect(Collectors.toList());
            resume.setProjects(projects);
        }

        if (request.skills() != null) {
            Map<UUID, String> skillCatalog = skillRepository.findAll().stream()
                .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

            List<ResumeSkill> skills = request.skills().stream()
                .map(s -> {
                    String resolvedName = skillCatalog.getOrDefault(s.skillId(), s.skillName());
                    return new ResumeSkill(
                        s.id() != null ? s.id() : UUID.randomUUID(),
                        s.skillId(),
                        resolvedName != null ? resolvedName : "Skill",
                        s.proficiency(),
                        s.yearsExperience()
                    );
                })
                .collect(Collectors.toList());
            resume.setSkills(skills);
        }

        if (request.education() != null) {
            List<ResumeEducation> education = request.education().stream()
                .map(ed -> new ResumeEducation(
                    ed.id() != null ? ed.id() : UUID.randomUUID(),
                    ed.institution(),
                    ed.degree(),
                    ed.fieldOfStudy(),
                    ed.startYear(),
                    ed.endYear(),
                    ed.displayOrder()
                ))
                .collect(Collectors.toList());
            resume.setEducation(education);
        }

        if (request.certifications() != null) {
            List<ResumeCertification> certifications = request.certifications().stream()
                .map(c -> new ResumeCertification(
                    c.id() != null ? c.id() : UUID.randomUUID(),
                    c.name(),
                    c.issuingOrganization(),
                    c.issueDate(),
                    c.expiryDate(),
                    c.credentialId(),
                    c.credentialUrl(),
                    c.displayOrder()
                ))
                .collect(Collectors.toList());
            resume.setCertifications(certifications);
        }

        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.fromDomain(saved);
    }

    public void deleteResume(UUID userId, UUID resumeId) {
        getOwnedResume(userId, resumeId);
        resumeRepository.deleteById(resumeId);
    }

    public ResumeResponse activateResume(UUID userId, UUID resumeId) {
        Resume resume = getOwnedResume(userId, resumeId);
        resume.activate();
        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.fromDomain(saved);
    }

    public ResumeResponse archiveResume(UUID userId, UUID resumeId) {
        Resume resume = getOwnedResume(userId, resumeId);
        resume.archive();
        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.fromDomain(saved);
    }

    public Resume getOwnedResume(UUID userId, UUID resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + resumeId));

        if (!resume.isOwnedBy(userId)) {
            // Strict ownership isolation: do not reveal existence of another user's resume
            throw new ResourceNotFoundException("Resume not found with ID: " + resumeId);
        }
        return resume;
    }
}
