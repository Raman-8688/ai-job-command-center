package com.jobcommandcenter.interview.application;

import com.jobcommandcenter.ai.domain.AIInterviewPrepRequest;
import com.jobcommandcenter.ai.domain.AIInterviewPrepResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.ai.domain.AIPracticeQuestion;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.application.domain.JobApplicationRepository;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.interview.api.dto.*;
import com.jobcommandcenter.interview.domain.*;
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

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewRepository interviewRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final AIProvider aiProvider;

    public InterviewService(InterviewRepository interviewRepository,
                            JobRepository jobRepository,
                            JobApplicationRepository jobApplicationRepository,
                            UserSkillRepository userSkillRepository,
                            SkillRepository skillRepository,
                            AIProvider aiProvider) {
        this.interviewRepository = interviewRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
        this.aiProvider = aiProvider;
    }

    public InterviewResponse scheduleInterview(UUID userId, ScheduleInterviewRequest request) {
        Job job = jobRepository.findById(request.jobId())
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + request.jobId()));

        if (request.applicationId() != null) {
            JobApplication app = jobApplicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + request.applicationId()));
            if (!app.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("Job application not found with id: " + request.applicationId());
            }
        }

        Interview interview = Interview.create(
            userId,
            request.applicationId(),
            request.jobId(),
            request.round(),
            request.roundNumber() != null ? request.roundNumber() : 1,
            request.format(),
            request.scheduledStartTime(),
            request.scheduledEndTime(),
            request.timeZone(),
            request.meetingLink(),
            request.location(),
            request.interviewerNames(),
            request.interviewerRoles(),
            request.notes()
        );

        Interview saved = interviewRepository.save(interview);
        log.info("Scheduled new interview {} for user {} on job {}", saved.getId(), userId, job.getId());
        return InterviewResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public InterviewResponse rescheduleInterview(UUID userId, UUID interviewId, RescheduleInterviewRequest request) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        Job job = getJobOrThrow(interview.getJobId());

        interview.reschedule(
            request.scheduledStartTime(),
            request.scheduledEndTime(),
            request.timeZone(),
            request.reason(),
            "USER"
        );

        Interview saved = interviewRepository.save(interview);
        log.info("Rescheduled interview {} for user {}", interviewId, userId);
        return InterviewResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public InterviewResponse updateInterviewDetails(UUID userId, UUID interviewId, UpdateInterviewDetailsRequest request) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        Job job = getJobOrThrow(interview.getJobId());

        interview.updateDetails(
            request.round(),
            request.roundNumber(),
            request.format(),
            request.meetingLink(),
            request.location(),
            request.interviewerNames(),
            request.interviewerRoles(),
            request.notes()
        );

        Interview saved = interviewRepository.save(interview);
        return InterviewResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public InterviewResponse updateStatus(UUID userId, UUID interviewId, UpdateInterviewStatusRequest request) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        Job job = getJobOrThrow(interview.getJobId());

        String source = (request.source() != null && !request.source().isBlank()) ? request.source() : "USER";

        switch (request.status()) {
            case COMPLETED -> interview.complete(request.feedback(), request.outcome(), request.notes(), source);
            case CANCELLED -> interview.cancel(request.notes(), source);
            case NO_SHOW -> interview.markNoShow(request.notes(), source);
            case RESCHEDULED -> throw new InvalidInterviewStateException("Please use the reschedule endpoint to change interview time and status");
            case SCHEDULED -> throw new InvalidInterviewStateException("Cannot transition back to SCHEDULED status directly");
        }

        Interview saved = interviewRepository.save(interview);
        log.info("Updated status of interview {} to {}", interviewId, request.status());
        return InterviewResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    public InterviewResponse updateOutcome(UUID userId, UUID interviewId, UpdateInterviewOutcomeRequest request) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        Job job = getJobOrThrow(interview.getJobId());

        interview.updateOutcome(request.outcome(), request.notes(), "USER");
        Interview saved = interviewRepository.save(interview);
        return InterviewResponse.fromDomain(saved, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(UUID userId, UUID interviewId) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        Job job = getJobOrThrow(interview.getJobId());
        return InterviewResponse.fromDomain(interview, job.getTitle(), job.getCompanyName(), job.getLocation());
    }

    @Transactional(readOnly = true)
    public List<InterviewSummaryResponse> listInterviews(UUID userId, InterviewSearchCriteria criteria) {
        List<Interview> interviews = interviewRepository.findByUserId(userId, criteria != null ? criteria : InterviewSearchCriteria.empty());

        Map<UUID, Job> jobMap = new HashMap<>();
        for (Interview interview : interviews) {
            jobMap.computeIfAbsent(interview.getJobId(), id -> jobRepository.findById(id).orElse(null));
        }

        return interviews.stream().map(i -> {
            Job job = jobMap.get(i.getJobId());
            String jobTitle = job != null ? job.getTitle() : "Software Engineer";
            String company = job != null ? job.getCompanyName() : "Unknown Company";
            String location = job != null ? job.getLocation() : "Remote";
            return InterviewSummaryResponse.fromDomain(i, jobTitle, company, location);
        }).collect(Collectors.toList());
    }

    public void deleteInterview(UUID userId, UUID interviewId) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new InvalidInterviewStateException("Completed interviews cannot be deleted from history");
        }
        interviewRepository.delete(interview);
        log.info("Deleted interview {} for user {}", interviewId, userId);
    }

    public InterviewPrepBundleResponse generateAiPrep(UUID userId, UUID interviewId) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        Job job = getJobOrThrow(interview.getJobId());

        // Extract verified skills of candidate
        List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
        List<String> verifiedSkillNames = userSkills.stream()
            .map(us -> skillRepository.findById(us.getSkillId()).map(Skill::getName).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        AIInterviewPrepRequest prepRequest = new AIInterviewPrepRequest(
            job.getTitle(),
            job.getCompanyName(),
            job.getDescription(),
            interview.getRound().name(),
            verifiedSkillNames,
            List.of()
        );

        AIInterviewPrepResponse aiResponse = aiProvider.generateInterviewPrep(prepRequest);

        List<InterviewPreparation> newPreps = new ArrayList<>();
        for (AIPracticeQuestion q : aiResponse.questions()) {
            newPreps.add(InterviewPreparation.create(
                interview.getId(),
                q.topicCategory(),
                q.question(),
                q.talkingPoints(),
                q.suggestedAnswerStar(),
                q.confidenceScore()
            ));
        }

        interview.addPreparations(newPreps);
        interviewRepository.save(interview);
        log.info("Generated {} AI interview prep questions for interview {}", newPreps.size(), interviewId);

        return getInterviewPrepBundle(userId, interviewId);
    }

    @Transactional(readOnly = true)
    public InterviewPrepBundleResponse getInterviewPrepBundle(UUID userId, UUID interviewId) {
        Interview interview = getInterviewOrThrow(interviewId, userId);
        Job job = getJobOrThrow(interview.getJobId());

        List<InterviewPreparationResponse> prepResponses = interview.getPreparations().stream()
            .map(InterviewPreparationResponse::fromDomain)
            .collect(Collectors.toList());

        long reviewedCount = interview.getPreparations().stream().filter(InterviewPreparation::isReviewed).count();
        int readinessScore = interview.getPreparations().isEmpty() ? 70 :
            Math.min(100, (int) (65 + ((double) reviewedCount / interview.getPreparations().size()) * 35));

        String strategy = String.format(
            "Prepared strategy for %s round at %s (%s). Focus on system design fundamentals, trade-off explanations, and STAR behavioral answers.",
            interview.getRound(), job.getCompanyName(), job.getTitle()
        );

        return new InterviewPrepBundleResponse(
            interview.getId(),
            job.getTitle(),
            job.getCompanyName(),
            interview.getRound(),
            readinessScore,
            strategy,
            prepResponses
        );
    }

    public InterviewPreparationResponse updatePrepNotes(UUID userId, UUID interviewId, UUID prepId, UpdatePrepNotesRequest request) {
        getInterviewOrThrow(interviewId, userId);
        InterviewPreparation prep = interviewRepository.findPreparationById(interviewId, prepId)
            .orElseThrow(() -> new ResourceNotFoundException("Interview preparation question not found: " + prepId));

        boolean reviewed = request.isReviewed() != null ? request.isReviewed() : prep.isReviewed();
        prep.updateCandidateNotes(request.userAnswerNotes(), reviewed);

        InterviewPreparation saved = interviewRepository.savePreparation(prep);
        return InterviewPreparationResponse.fromDomain(saved);
    }

    public InterviewPreparationResponse addCustomPrepQuestion(UUID userId, UUID interviewId, SavePrepQuestionRequest request) {
        Interview interview = getInterviewOrThrow(interviewId, userId);

        InterviewPreparation prep = InterviewPreparation.create(
            interview.getId(),
            request.topicCategory(),
            request.question(),
            request.talkingPoints(),
            request.suggestedAnswerStar(),
            new java.math.BigDecimal("0.90")
        );

        interview.addPreparation(prep);
        interviewRepository.save(interview);
        return InterviewPreparationResponse.fromDomain(prep);
    }

    /**
     * Aggregates interview cockpit metrics for the authenticated candidate.
     * <p>
     * The overall readiness score is computed directly from actual persisted interview-preparation
     * questions across all interviews belonging to the candidate:
     * {@code (reviewed questions / total questions) * 100} (rounded to nearest integer).
     * If the candidate has no interviews or no preparation questions exist yet, the readiness score
     * returns 0% (transparent zero indicating no preparation data, rather than an invented synthetic fallback).
     *
     * @param userId The ID of the authenticated user
     * @return InterviewDashboardSummaryResponse containing counts and the computed readiness score
     */
    @Transactional(readOnly = true)
    public InterviewDashboardSummaryResponse getDashboardSummary(UUID userId) {
        long total = interviewRepository.countByUserId(userId);
        long completed = interviewRepository.countByUserIdAndStatus(userId, InterviewStatus.COMPLETED);

        Instant now = Instant.now();
        List<Interview> upcomingList = interviewRepository.findUpcomingByUserId(userId, now);
        long upcoming = upcomingList.size();

        Map<InterviewRound, Long> countByRound = new EnumMap<>(InterviewRound.class);
        Map<InterviewStatus, Long> countByStatus = new EnumMap<>(InterviewStatus.class);

        for (InterviewRound r : InterviewRound.values()) {
            countByRound.put(r, 0L);
        }
        for (InterviewStatus s : InterviewStatus.values()) {
            countByStatus.put(s, interviewRepository.countByUserIdAndStatus(userId, s));
        }

        List<Interview> allInterviews = interviewRepository.findByUserId(userId, InterviewSearchCriteria.empty());
        int totalPrepCount = 0;
        int reviewedPrepCount = 0;

        for (Interview i : allInterviews) {
            countByRound.put(i.getRound(), countByRound.getOrDefault(i.getRound(), 0L) + 1);
            if (i.getPreparations() != null) {
                for (InterviewPreparation prep : i.getPreparations()) {
                    totalPrepCount++;
                    if (prep.isReviewed()) {
                        reviewedPrepCount++;
                    }
                }
            }
        }

        InterviewSummaryResponse nextUpcoming = null;
        if (!upcomingList.isEmpty()) {
            Interview first = upcomingList.get(0);
            Job job = jobRepository.findById(first.getJobId()).orElse(null);
            String title = job != null ? job.getTitle() : "Software Engineer";
            String comp = job != null ? job.getCompanyName() : "Company";
            String loc = job != null ? job.getLocation() : "Remote";
            nextUpcoming = InterviewSummaryResponse.fromDomain(first, title, comp, loc);
        }

        // Calculate dashboard readiness strictly from actual persisted interview-preparation review data.
        // When there are no interviews or no preparation questions, readiness is 0% (never an invented synthetic fallback).
        int overallReadinessScore = totalPrepCount > 0
            ? (int) Math.round(((double) reviewedPrepCount / totalPrepCount) * 100.0)
            : 0;

        return new InterviewDashboardSummaryResponse(
            total,
            upcoming,
            completed,
            countByRound,
            countByStatus,
            nextUpcoming,
            overallReadinessScore
        );
    }

    private Interview getInterviewOrThrow(UUID interviewId, UUID userId) {
        return interviewRepository.findByIdAndUserId(interviewId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + interviewId));
    }

    private Job getJobOrThrow(UUID jobId) {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
    }
}
