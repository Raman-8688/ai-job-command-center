package com.jobcommandcenter.application.application;

import com.jobcommandcenter.ai.domain.AIApplicationGuidanceRequest;
import com.jobcommandcenter.ai.domain.AIApplicationGuidanceResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.application.api.dto.*;
import com.jobcommandcenter.application.domain.*;
import com.jobcommandcenter.common.error.BusinessException;
import com.jobcommandcenter.common.error.ConflictException;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobRepository;
import com.jobcommandcenter.resume.domain.ResumeRepository;
import com.jobcommandcenter.resume.domain.TailoredResumeRepository;
import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Primary domain service managing the application lifecycle, status transitions,
 * resume linkages, dashboard metrics, and AI next-step guidance.
 */
@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final UserRepository userRepository;
    private final AIProvider aiProvider;

    public JobApplicationService(
        JobApplicationRepository jobApplicationRepository,
        JobRepository jobRepository,
        ResumeRepository resumeRepository,
        TailoredResumeRepository tailoredResumeRepository,
        UserRepository userRepository,
        AIProvider aiProvider
    ) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.tailoredResumeRepository = tailoredResumeRepository;
        this.userRepository = userRepository;
        this.aiProvider = aiProvider;
    }

    @Transactional
    public JobApplicationResponse createApplication(UUID userId, CreateApplicationRequest request) {
        Job job = jobRepository.findById(request.jobId())
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + request.jobId()));

        if (jobApplicationRepository.existsByUserIdAndJobId(userId, request.jobId())) {
            throw new ConflictException("An application already exists for this job");
        }

        if (request.resumeId() != null && !resumeRepository.existsByIdAndUserId(request.resumeId(), userId)) {
            throw new ResourceNotFoundException("Resume not found or not owned by user: " + request.resumeId());
        }

        if (request.tailoredResumeId() != null && tailoredResumeRepository.findByIdAndUserId(request.tailoredResumeId(), userId).isEmpty()) {
            throw new ResourceNotFoundException("Tailored resume not found or not owned by user: " + request.tailoredResumeId());
        }

        JobApplication application = JobApplication.createNew(
            userId,
            request.jobId(),
            request.status(),
            request.appliedAt(),
            request.submissionSource(),
            request.externalReference(),
            request.resumeId(),
            request.tailoredResumeId(),
            request.notes()
        );

        if (request.nextFollowUpDate() != null) {
            application.updateEditableDetails(request.submissionSource(), request.externalReference(), request.appliedAt(), request.nextFollowUpDate(), request.notes());
        }

        JobApplication saved = jobApplicationRepository.save(application);
        return JobApplicationResponse.fromDomain(saved, job);
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(UUID userId, UUID id) {
        JobApplication application = getOwnedApplication(userId, id);
        Job job = jobRepository.findById(application.getJobId()).orElse(null);
        return JobApplicationResponse.fromDomain(application, job);
    }

    @Transactional(readOnly = true)
    public ApplicationPageResponse searchApplications(JobApplicationSearchCriteria criteria) {
        List<JobApplication> applications = jobApplicationRepository.search(criteria);
        long totalElements = jobApplicationRepository.count(criteria);

        List<JobApplicationSummaryResponse> content = applications.stream()
            .map(app -> {
                Job job = jobRepository.findById(app.getJobId()).orElse(null);
                return JobApplicationSummaryResponse.fromDomain(app, job);
            })
            .collect(Collectors.toList());

        int totalPages = criteria.size() > 0 ? (int) Math.ceil((double) totalElements / criteria.size()) : 1;
        return new ApplicationPageResponse(content, criteria.page(), criteria.size(), totalElements, totalPages);
    }

    @Transactional
    public JobApplicationResponse updateApplication(UUID userId, UUID id, UpdateApplicationRequest request) {
        JobApplication application = getOwnedApplication(userId, id);

        String oldNotes = application.getNotes();
        application.updateEditableDetails(
            request.submissionSource(),
            request.externalReference(),
            request.appliedAt(),
            request.nextFollowUpDate(),
            request.notes()
        );

        if (request.notes() != null && !request.notes().isBlank() && !request.notes().equals(oldNotes)) {
            application.addNote(request.notes(), EventSource.USER);
        }

        JobApplication saved = jobApplicationRepository.save(application);
        Job job = jobRepository.findById(saved.getJobId()).orElse(null);
        return JobApplicationResponse.fromDomain(saved, job);
    }

    @Transactional
    public JobApplicationResponse transitionStatus(UUID userId, UUID id, TransitionStatusRequest request) {
        JobApplication application = getOwnedApplication(userId, id);

        application.transitionTo(
            request.targetStatus(),
            request.notes(),
            request.eventSource() != null ? request.eventSource() : EventSource.USER,
            request.occurredAt()
        );

        JobApplication saved = jobApplicationRepository.save(application);
        Job job = jobRepository.findById(saved.getJobId()).orElse(null);
        return JobApplicationResponse.fromDomain(saved, job);
    }

    @Transactional
    public JobApplicationResponse linkResume(UUID userId, UUID id, LinkResumeRequest request) {
        JobApplication application = getOwnedApplication(userId, id);

        if (request.resumeId() != null && !resumeRepository.existsByIdAndUserId(request.resumeId(), userId)) {
            throw new ResourceNotFoundException("Resume not found or not owned by user: " + request.resumeId());
        }

        if (request.tailoredResumeId() != null && tailoredResumeRepository.findByIdAndUserId(request.tailoredResumeId(), userId).isEmpty()) {
            throw new ResourceNotFoundException("Tailored resume not found or not owned by user: " + request.tailoredResumeId());
        }

        application.linkResume(request.resumeId(), request.tailoredResumeId(), request.notes(), EventSource.USER);

        JobApplication saved = jobApplicationRepository.save(application);
        Job job = jobRepository.findById(saved.getJobId()).orElse(null);
        return JobApplicationResponse.fromDomain(saved, job);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationEventResponse> getTimeline(UUID userId, UUID id) {
        JobApplication application = getOwnedApplication(userId, id);
        return application.getEvents().stream()
            .map(JobApplicationEventResponse::fromDomain)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationDashboardSummaryResponse getDashboardSummary(UUID userId) {
        Map<ApplicationStatus, Long> counts = jobApplicationRepository.countByStatusForUser(userId);
        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        long closed = counts.getOrDefault(ApplicationStatus.OFFER, 0L)
            + counts.getOrDefault(ApplicationStatus.ACCEPTED, 0L)
            + counts.getOrDefault(ApplicationStatus.REJECTED, 0L)
            + counts.getOrDefault(ApplicationStatus.WITHDRAWN, 0L)
            + counts.getOrDefault(ApplicationStatus.ARCHIVED, 0L);

        long active = Math.max(0, total - closed);
        long followUpsDue = jobApplicationRepository.countFollowUpsDueForUser(userId);

        return new ApplicationDashboardSummaryResponse(counts, active, followUpsDue, total);
    }

    @Transactional(readOnly = true)
    public ApplicationGuidanceResponse getApplicationGuidance(UUID userId, UUID id) {
        JobApplication application = getOwnedApplication(userId, id);
        Job job = jobRepository.findById(application.getJobId())
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + application.getJobId()));

        String candidateName = userRepository.findById(userId)
            .map(User::getDisplayName)
            .orElse("Candidate");

        int daysSinceApplied = 0;
        if (application.getAppliedAt() != null) {
            daysSinceApplied = (int) java.time.temporal.ChronoUnit.DAYS.between(application.getAppliedAt(), java.time.Instant.now());
            if (daysSinceApplied < 0) daysSinceApplied = 0;
        }

        String lastNotes = application.getNotes() != null ? application.getNotes() : "";
        String followUpStr = application.getNextFollowUpDate() != null ? application.getNextFollowUpDate().toString() : "";

        AIApplicationGuidanceRequest aiReq = new AIApplicationGuidanceRequest(
            job.getCompanyName(),
            job.getTitle(),
            application.getStatus().name(),
            daysSinceApplied,
            lastNotes,
            followUpStr
        );

        AIApplicationGuidanceResponse aiRes = aiProvider.generateApplicationGuidance(aiReq);
        return ApplicationGuidanceResponse.fromAiResponse(aiRes, "MockDeterministicAIProvider");
    }

    @Transactional
    public void deleteApplication(UUID userId, UUID id) {
        JobApplication application = getOwnedApplication(userId, id);
        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.WITHDRAWN) {
            throw new BusinessException("Only DRAFT or WITHDRAWN applications can be deleted. Current status is: " + application.getStatus());
        }
        jobApplicationRepository.delete(application);
    }

    private JobApplication getOwnedApplication(UUID userId, UUID id) {
        JobApplication application = jobApplicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job application not found: " + id));

        if (!application.getUserId().equals(userId)) {
            // Multi-tenant isolation: do not disclose existence of cross-user records
            throw new ResourceNotFoundException("Job application not found: " + id);
        }
        return application;
    }
}
