package com.jobcommandcenter.assessment.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.assessment.api.dto.CompanyDossierResponse;
import com.jobcommandcenter.assessment.api.dto.GenerateCompanyDossierRequest;
import com.jobcommandcenter.assessment.domain.CompanyDossier;
import com.jobcommandcenter.assessment.domain.CompanyDossierRepository;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobRepository;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillRepository;
import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyDossierService {

    private static final Logger log = LoggerFactory.getLogger(CompanyDossierService.class);

    private final CompanyDossierRepository dossierRepository;
    private final JobRepository jobRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final AIProvider aiProvider;

    public CompanyDossierService(CompanyDossierRepository dossierRepository,
                                JobRepository jobRepository,
                                UserSkillRepository userSkillRepository,
                                SkillRepository skillRepository,
                                AIProvider aiProvider) {
        this.dossierRepository = dossierRepository;
        this.jobRepository = jobRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
        this.aiProvider = aiProvider;
    }

    @Transactional(readOnly = true)
    public CompanyDossierResponse getDossierForJob(UUID userId, UUID jobId) {
        getJobOrThrow(jobId);
        CompanyDossier dossier = dossierRepository.findByUserIdAndJobId(userId, jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Company dossier not found for job: " + jobId));

        return CompanyDossierResponse.fromDomain(
            dossier,
            "Grounded in target job requirements and candidate profile.",
            new BigDecimal("0.90")
        );
    }

    public CompanyDossierResponse generateOrRefreshDossier(UUID userId, UUID jobId, GenerateCompanyDossierRequest request) {
        Job job = getJobOrThrow(jobId);

        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        List<String> verifiedSkillNames = userSkills.stream()
            .map(us -> skillRepository.findById(us.getSkillId()).map(Skill::getName).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<String> canonicalRequirements = job.getJobSkills().stream()
            .map(js -> skillRepository.findById(js.getSkillId()).map(Skill::getName).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        String rawResearch = (request != null) ? request.rawCompanyResearch() : null;

        AICompanyDossierRequest aiRequest = new AICompanyDossierRequest(
            job.getCompanyName(),
            job.getTitle(),
            job.getDescription(),
            canonicalRequirements,
            verifiedSkillNames,
            List.of(),
            job.getCompanyWebsite(),
            rawResearch
        );

        AICompanyDossierResponse aiResponse = aiProvider.generateCompanyDossier(aiRequest);

        Optional<CompanyDossier> existingOpt = dossierRepository.findByUserIdAndJobId(userId, jobId);
        CompanyDossier toSave;
        if (existingOpt.isPresent()) {
            toSave = existingOpt.get();
            toSave.updateBriefing(
                aiResponse.companyTier(),
                aiResponse.overview(),
                aiResponse.engineeringScale(),
                aiResponse.coreTechStack(),
                aiResponse.engineeringCulture(),
                aiResponse.architectureFocus(),
                aiResponse.tailoredTalkingPoints(),
                aiResponse.interviewerQuestions()
            );
            log.info("Refreshed existing company dossier {} for user {} on job {}", toSave.getId(), userId, jobId);
        } else {
            toSave = CompanyDossier.create(
                userId,
                jobId,
                aiResponse.companyName(),
                aiResponse.companyTier(),
                aiResponse.overview(),
                aiResponse.engineeringScale(),
                aiResponse.coreTechStack(),
                aiResponse.engineeringCulture(),
                aiResponse.architectureFocus(),
                aiResponse.tailoredTalkingPoints(),
                aiResponse.interviewerQuestions()
            );
            log.info("Created new company dossier {} for user {} on job {}", toSave.getId(), userId, jobId);
        }

        CompanyDossier saved = dossierRepository.save(toSave);
        return CompanyDossierResponse.fromDomain(saved, aiResponse.provenanceSummary(), aiResponse.confidenceScore());
    }

    private Job getJobOrThrow(UUID jobId) {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
    }
}
