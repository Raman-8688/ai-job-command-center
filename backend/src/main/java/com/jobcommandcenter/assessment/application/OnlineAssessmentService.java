package com.jobcommandcenter.assessment.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.application.domain.JobApplicationRepository;
import com.jobcommandcenter.assessment.api.dto.*;
import com.jobcommandcenter.assessment.domain.*;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.interview.domain.Interview;
import com.jobcommandcenter.interview.domain.InterviewRepository;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobRepository;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillRepository;
import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OnlineAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(OnlineAssessmentService.class);

    private final OnlineAssessmentRepository assessmentRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewRepository interviewRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final AIProvider aiProvider;

    public OnlineAssessmentService(OnlineAssessmentRepository assessmentRepository,
                                  JobRepository jobRepository,
                                  JobApplicationRepository jobApplicationRepository,
                                  InterviewRepository interviewRepository,
                                  UserSkillRepository userSkillRepository,
                                  SkillRepository skillRepository,
                                  AIProvider aiProvider) {
        this.assessmentRepository = assessmentRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewRepository = interviewRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
        this.aiProvider = aiProvider;
    }

    public OnlineAssessmentResponse createAssessment(UUID userId, CreateAssessmentRequest request) {
        Job job = getJobOrThrow(request.jobId());

        if (request.applicationId() != null) {
            JobApplication app = jobApplicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + request.applicationId()));
            if (!app.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("Job application not found with id: " + request.applicationId());
            }
            if (!app.getJobId().equals(job.getId())) {
                throw new IllegalArgumentException("Job application does not match the specified job");
            }
        }

        if (request.interviewId() != null) {
            Interview interview = interviewRepository.findByIdAndUserId(request.interviewId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + request.interviewId()));
            if (!interview.getJobId().equals(job.getId())) {
                throw new IllegalArgumentException("Interview does not match the specified job");
            }
        }

        OnlineAssessment assessment = OnlineAssessment.create(
            userId,
            request.jobId(),
            request.applicationId(),
            request.interviewId(),
            request.platform(),
            request.title(),
            request.durationMinutes(),
            request.invitedAt(),
            request.expiresAt(),
            request.assessmentUrl(),
            request.accessCode()
        );

        if (request.scheduledStartTime() != null) {
            assessment.start(request.scheduledStartTime());
        }

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Created online assessment {} for user {} on job {}", saved.getId(), userId, job.getId());
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    @Transactional(readOnly = true)
    public List<OnlineAssessmentSummaryResponse> listAssessments(UUID userId, OnlineAssessmentSearchCriteria criteria) {
        List<OnlineAssessment> assessments = assessmentRepository.findByUserId(userId, criteria);
        Map<UUID, Job> jobCache = new HashMap<>();

        return assessments.stream().map(a -> {
            Job job = jobCache.computeIfAbsent(a.getJobId(), id -> jobRepository.findById(id).orElse(null));
            String title = job != null ? job.getTitle() : "Unknown Role";
            String company = job != null ? job.getCompanyName() : "Unknown Company";
            return OnlineAssessmentSummaryResponse.fromDomain(a, title, company);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OnlineAssessmentResponse getAssessmentById(UUID userId, UUID id) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());
        return OnlineAssessmentResponse.fromDomain(assessment, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public OnlineAssessmentResponse updateAssessment(UUID userId, UUID id, UpdateAssessmentRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        if (request.version() != null && !Objects.equals(request.version(), assessment.getVersion())) {
            throw new OptimisticLockingFailureException("Stale assessment update detected for id: " + id);
        }

        assessment.updateDetails(
            request.platform(),
            request.title(),
            request.durationMinutes(),
            request.expiresAt(),
            request.assessmentUrl(),
            request.accessCode()
        );

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Updated online assessment {} for user {}", id, userId);
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public OnlineAssessmentResponse startAssessment(UUID userId, UUID id, StartAssessmentRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        Instant startTime = (request != null && request.startTime() != null) ? request.startTime() : Instant.now();
        assessment.start(startTime);

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Started online assessment {} for user {}", id, userId);
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public OnlineAssessmentResponse submitAssessment(UUID userId, UUID id, SubmitAssessmentRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        assessment.submit(
            request.score(),
            request.maxScore(),
            request.submissionNotes(),
            request.submissionRepoUrl(),
            request.completedAt()
        );

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Submitted online assessment {} for user {}", id, userId);
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public OnlineAssessmentResponse recordResult(UUID userId, UUID id, RecordAssessmentResultRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        assessment.recordResult(
            request.result(),
            request.score(),
            request.maxScore(),
            request.notes()
        );

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Recorded result {} for online assessment {}", request.result(), id);
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public OnlineAssessmentResponse extendDeadline(UUID userId, UUID id, ExtendAssessmentDeadlineRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        assessment.extendDeadline(request.newExpiresAt(), request.reason());

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Extended deadline for online assessment {} to {}", id, request.newExpiresAt());
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public OnlineAssessmentResponse expireAssessment(UUID userId, UUID id, ExpireAssessmentRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        String reason = (request != null && request.reason() != null) ? request.reason() : "Assessment marked expired by user";
        assessment.expire(reason);

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Expired online assessment {}", id);
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public OnlineAssessmentResponse abandonAssessment(UUID userId, UUID id, AbandonAssessmentRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(id, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        String reason = (request != null && request.reason() != null) ? request.reason() : "Assessment abandoned by candidate";
        assessment.abandon(reason);

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Abandoned online assessment {}", id);
        return OnlineAssessmentResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    @Transactional(readOnly = true)
    public AssessmentChecklistBundleResponse getChecklist(UUID userId, UUID assessmentId) {
        OnlineAssessment assessment = getAssessmentOrThrow(assessmentId, userId);
        List<AssessmentChecklistItemResponse> items = assessment.getChecklists().stream()
            .map(AssessmentChecklistItemResponse::fromDomain)
            .collect(Collectors.toList());

        long completed = assessment.getChecklists().stream().filter(AssessmentChecklistItem::isCompleted).count();
        return new AssessmentChecklistBundleResponse(
            assessmentId,
            assessment.getChecklists().size(),
            (int) completed,
            assessment.getChecklistCompletionPercentage(),
            items
        );
    }

    public AssessmentChecklistBundleResponse toggleChecklistItem(UUID userId, UUID assessmentId, UUID itemId, ToggleChecklistItemRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(assessmentId, userId);

        boolean found;
        if (request != null && request.isCompleted() != null) {
            found = assessment.setChecklistItemCompleted(itemId, request.isCompleted());
        } else {
            found = assessment.toggleChecklistItem(itemId);
        }

        if (!found) {
            throw new ResourceNotFoundException("Checklist item not found with id: " + itemId);
        }

        OnlineAssessment saved = assessmentRepository.save(assessment);
        List<AssessmentChecklistItemResponse> items = saved.getChecklists().stream()
            .map(AssessmentChecklistItemResponse::fromDomain)
            .collect(Collectors.toList());

        long completed = saved.getChecklists().stream().filter(AssessmentChecklistItem::isCompleted).count();
        return new AssessmentChecklistBundleResponse(
            assessmentId,
            saved.getChecklists().size(),
            (int) completed,
            saved.getChecklistCompletionPercentage(),
            items
        );
    }

    @Transactional(readOnly = true)
    public List<OnlineAssessmentEventResponse> getEventHistory(UUID userId, UUID assessmentId) {
        OnlineAssessment assessment = getAssessmentOrThrow(assessmentId, userId);
        return assessment.getEvents().stream()
            .map(OnlineAssessmentEventResponse::fromDomain)
            .collect(Collectors.toList());
    }

    public AssessmentBriefingResponse generateBriefing(UUID userId, UUID assessmentId, GenerateBriefingRequest request) {
        OnlineAssessment assessment = getAssessmentOrThrow(assessmentId, userId);
        Job job = getJobOrThrow(assessment.getJobId());

        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        List<String> verifiedSkillNames = userSkills.stream()
            .map(us -> skillRepository.findById(us.getSkillId()).map(Skill::getName).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<String> canonicalRequirements = job.getJobSkills().stream()
            .map(js -> skillRepository.findById(js.getSkillId()).map(Skill::getName).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        AIAssessmentBriefingRequest briefingRequest = new AIAssessmentBriefingRequest(
            job.getTitle(),
            job.getCompanyName(),
            job.getDescription(),
            assessment.getPlatform().name(),
            assessment.getDurationMinutes(),
            canonicalRequirements,
            verifiedSkillNames,
            request != null ? request.candidateNotes() : ""
        );

        AIAssessmentBriefingResponse aiResponse = aiProvider.generateAssessmentBriefing(briefingRequest);
        AIAssessmentBriefingValidator.validateBriefingResponse(aiResponse);

        List<AssessmentChecklistItem> mergedItems = AIAssessmentBriefingValidator.mergeWithoutOverwriting(
            assessment.getChecklists(),
            aiResponse.checklistItems(),
            assessment.getId()
        );
        assessment.replaceChecklistItems(mergedItems);
        assessment.recordEvent(
            AssessmentEventType.NOTE_ADDED,
            "AI assessment briefing generated for platform: " + assessment.getPlatform(),
            "AI"
        );

        OnlineAssessment saved = assessmentRepository.save(assessment);
        log.info("Generated and merged AI briefing for assessment {}", assessmentId);

        List<AssessmentChecklistItemResponse> checklistResponses = saved.getChecklists().stream()
            .map(AssessmentChecklistItemResponse::fromDomain)
            .collect(Collectors.toList());

        long completed = saved.getChecklists().stream().filter(AssessmentChecklistItem::isCompleted).count();

        return new AssessmentBriefingResponse(
            saved.getId(),
            aiResponse.platformGuidance(),
            aiResponse.timeManagementAdvice(),
            aiResponse.prioritizedTopics(),
            saved.getChecklists().size(),
            (int) completed,
            saved.getChecklistCompletionPercentage(),
            checklistResponses,
            aiResponse.confidenceScore()
        );
    }

    @Transactional(readOnly = true)
    public AssessmentDashboardSummaryResponse getDashboardSummary(UUID userId) {
        long total = assessmentRepository.countByUserId(userId);
        long invited = assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.INVITED);
        long inProgress = assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.IN_PROGRESS);
        long submitted = assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.SUBMITTED);
        long expired = assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.EXPIRED);
        long abandoned = assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.ABANDONED);

        Instant now = Instant.now();
        long dueSoon = assessmentRepository.countDueSoonByUserId(userId, now, now.plus(48, ChronoUnit.HOURS));

        List<OnlineAssessment> active = assessmentRepository.findActiveByUserId(userId);
        long overdue = active.stream()
            .filter(a -> a.getExpiresAt() != null && a.getExpiresAt().isBefore(now))
            .count();

        return new AssessmentDashboardSummaryResponse(
            total,
            invited,
            inProgress,
            submitted,
            expired,
            abandoned,
            dueSoon,
            overdue
        );
    }

    private OnlineAssessment getAssessmentOrThrow(UUID id, UUID userId) {
        return assessmentRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Online assessment not found with id: " + id));
    }

    private Job getJobOrThrow(UUID jobId) {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
    }
}
