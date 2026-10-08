package com.jobcommandcenter.job.application;

import com.jobcommandcenter.common.error.ConflictException;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.api.*;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class JobService {

    private final JobRepository jobRepository;
    private final UserJobRepository userJobRepository;
    private final SkillRepository skillRepository;

    public JobService(JobRepository jobRepository,
                      UserJobRepository userJobRepository,
                      SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.userJobRepository = userJobRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public JobResponse createJob(CreateJobRequest request, UUID requestingUserId) {
        // 1. Deduplication checks
        if (request.source() != null && request.externalJobId() != null && !request.externalJobId().isBlank()) {
            if (jobRepository.existsBySourceAndExternalJobId(request.source(), request.externalJobId().trim())) {
                throw new ConflictException("Duplicate job: Posting with external ID '" +
                    request.externalJobId() + "' from " + request.source() + " already exists");
            }
        }

        if (request.jobUrl() != null && !request.jobUrl().isBlank()) {
            if (jobRepository.existsByJobUrl(request.jobUrl().trim())) {
                throw new ConflictException("Duplicate job: Posting with URL '" + request.jobUrl() + "' already exists");
            }
        }

        String dedupHash = Job.calculateDeduplicationHash(request.companyName(), request.title(), request.location());
        if (jobRepository.existsByDeduplicationHash(dedupHash)) {
            throw new ConflictException("Duplicate job: Posting for company '" +
                request.companyName() + "' with title '" + request.title() + "' already exists");
        }

        // 2. Instantiate canonical job entity
        Job job = Job.createNew(
            request.externalJobId() != null ? request.externalJobId().trim() : null,
            request.title(),
            request.companyName(),
            request.companyWebsite(),
            request.jobUrl() != null ? request.jobUrl().trim() : null,
            request.description(),
            request.location(),
            request.workMode(),
            request.employmentType(),
            request.experienceMinYears(),
            request.experienceMaxYears(),
            request.salaryMin(),
            request.salaryMax(),
            request.salaryCurrency(),
            request.source(),
            request.sourceUrl(),
            request.postedAt(),
            request.applicationDeadline()
        );

        // 3. Resolve skill associations
        List<JobSkill> jobSkills = new ArrayList<>();
        if (request.requiredSkillIds() != null) {
            for (UUID skillId : request.requiredSkillIds()) {
                verifySkillExists(skillId);
                jobSkills.add(JobSkill.create(job.getId(), skillId, SkillRequirementType.REQUIRED, null));
            }
        }
        if (request.preferredSkillIds() != null) {
            for (UUID skillId : request.preferredSkillIds()) {
                verifySkillExists(skillId);
                jobSkills.add(JobSkill.create(job.getId(), skillId, SkillRequirementType.PREFERRED, null));
            }
        }
        job.setJobSkills(jobSkills);

        Job savedJob = jobRepository.save(job);

        // 4. Associate with candidate private tracker
        if (requestingUserId != null) {
            UserJob userJob = UserJob.create(requestingUserId, savedJob.getId());
            userJobRepository.save(userJob);
        }

        return toJobResponse(savedJob);
    }

    public JobResponse getJobById(UUID jobId) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        return toJobResponse(job);
    }

    @Transactional
    public JobResponse updateJob(UUID jobId, UpdateJobRequest request) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        job.updateDetails(
            request.externalJobId() != null ? request.externalJobId().trim() : null,
            request.title(),
            request.companyName(),
            request.companyWebsite(),
            request.jobUrl() != null ? request.jobUrl().trim() : null,
            request.description(),
            request.location(),
            request.workMode(),
            request.employmentType(),
            request.experienceMinYears(),
            request.experienceMaxYears(),
            request.salaryMin(),
            request.salaryMax(),
            request.salaryCurrency(),
            request.source(),
            request.sourceUrl(),
            request.postedAt(),
            request.applicationDeadline(),
            request.status()
        );

        if (request.requiredSkillIds() != null || request.preferredSkillIds() != null) {
            List<JobSkill> jobSkills = new ArrayList<>();
            if (request.requiredSkillIds() != null) {
                for (UUID skillId : request.requiredSkillIds()) {
                    verifySkillExists(skillId);
                    jobSkills.add(JobSkill.create(job.getId(), skillId, SkillRequirementType.REQUIRED, null));
                }
            }
            if (request.preferredSkillIds() != null) {
                for (UUID skillId : request.preferredSkillIds()) {
                    verifySkillExists(skillId);
                    jobSkills.add(JobSkill.create(job.getId(), skillId, SkillRequirementType.PREFERRED, null));
                }
            }
            job.setJobSkills(jobSkills);
        }

        Job saved = jobRepository.save(job);
        return toJobResponse(saved);
    }

    @Transactional
    public void deleteJob(UUID jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with ID: " + jobId);
        }
        jobRepository.deleteById(jobId);
    }

    public PageResponse<JobSummaryResponse> searchJobs(JobSearchCriteria criteria, int page, int size) {
        List<Job> jobs = jobRepository.search(criteria, page, size);
        long total = jobRepository.count(criteria);

        List<JobSummaryResponse> content = jobs.stream()
            .map(JobSummaryResponse::fromDomain)
            .collect(Collectors.toList());

        return PageResponse.of(content, page, size, total);
    }

    @Transactional
    public void updateUserJobStatus(UUID userId, UUID jobId, UserJobStatus status, String notes) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with ID: " + jobId);
        }

        UserJob userJob = userJobRepository.findByUserIdAndJobId(userId, jobId)
            .orElseGet(() -> UserJob.create(userId, jobId));

        userJob.updateStatus(status);
        if (notes != null) {
            userJob.updateNotes(notes);
        }

        userJobRepository.save(userJob);
    }

    private void verifySkillExists(UUID skillId) {
        if (skillRepository.findById(skillId).isEmpty()) {
            throw new ResourceNotFoundException("Skill not found in catalog with ID: " + skillId);
        }
    }

    private JobResponse toJobResponse(Job job) {
        Map<UUID, String> skillMap = skillRepository.findAll().stream()
            .collect(Collectors.toMap(Skill::getId, Skill::getName, (a, b) -> a));

        List<JobSkillDto> skillDtos = job.getJobSkills().stream()
            .map(js -> new JobSkillDto(
                js.getSkillId(),
                skillMap.getOrDefault(js.getSkillId(), "Unknown"),
                js.getRequirementType(),
                js.getYearsExperienceRequired()
            ))
            .collect(Collectors.toList());

        return JobResponse.of(job, skillDtos);
    }
}
