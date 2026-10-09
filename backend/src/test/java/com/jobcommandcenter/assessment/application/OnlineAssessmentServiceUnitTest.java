package com.jobcommandcenter.assessment.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.application.domain.JobApplicationRepository;
import com.jobcommandcenter.assessment.api.dto.*;
import com.jobcommandcenter.assessment.domain.*;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.interview.domain.Interview;
import com.jobcommandcenter.interview.domain.InterviewFormat;
import com.jobcommandcenter.interview.domain.InterviewRepository;
import com.jobcommandcenter.interview.domain.InterviewRound;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillRepository;
import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnlineAssessmentServiceUnitTest {

    @Mock
    private OnlineAssessmentRepository assessmentRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AIProvider aiProvider;

    private OnlineAssessmentService assessmentService;

    private final UUID userId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();
    private Job testJob;

    @BeforeEach
    void setUp() {
        assessmentService = new OnlineAssessmentService(
            assessmentRepository,
            jobRepository,
            jobApplicationRepository,
            interviewRepository,
            userSkillRepository,
            skillRepository,
            aiProvider
        );

        testJob = Job.createNew(
            "ext-1",
            "Senior Backend Engineer",
            "Netflix",
            "https://netflix.com",
            "https://jobs.netflix.com/1",
            "Build distributed streaming engines",
            "Los Gatos, CA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            new BigDecimal("10.0"),
            new BigDecimal("180000"),
            new BigDecimal("240000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://jobs.netflix.com",
            Instant.now().minus(2, ChronoUnit.DAYS),
            Instant.now().plus(30, ChronoUnit.DAYS)
        );
    }

    @Test
    @DisplayName("Create assessment succeeds with valid job and populates domain aggregate")
    void createAssessmentSuccess() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(assessmentRepository.save(any(OnlineAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateAssessmentRequest request = new CreateAssessmentRequest(
            jobId,
            null,
            null,
            AssessmentPlatform.HACKERRANK,
            "Core Algorithmic Screen",
            90,
            Instant.now(),
            Instant.now().plus(5, ChronoUnit.DAYS),
            null,
            "https://hackerrank.com/tests/123",
            "SECRET123",
            "Notes"
        );

        OnlineAssessmentResponse response = assessmentService.createAssessment(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.jobTitle()).isEqualTo(testJob.getTitle());
        assertThat(response.companyName()).isEqualTo(testJob.getCompanyName());
        assertThat(response.platform()).isEqualTo(AssessmentPlatform.HACKERRANK);
        assertThat(response.status()).isEqualTo(AssessmentStatus.INVITED);
        assertThat(response.result()).isEqualTo(AssessmentResult.PENDING);
        assertThat(response.durationMinutes()).isEqualTo(90);
        assertThat(response.recentEvents()).hasSize(1);
        assertThat(response.recentEvents().get(0).eventType()).isEqualTo(AssessmentEventType.INVITED);
    }

    @Test
    @DisplayName("Create assessment rejects non-existent job")
    void createAssessmentRejectsMissingJob() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        CreateAssessmentRequest request = new CreateAssessmentRequest(
            jobId, null, null, AssessmentPlatform.LEETCODE, "Title", 60, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> assessmentService.createAssessment(userId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Job not found");
    }

    @Test
    @DisplayName("Create assessment rejects application belonging to another user")
    void createAssessmentRejectsUnownedApplication() {
        UUID appId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        JobApplication otherApp = JobApplication.createNew(
            otherUser, jobId, ApplicationStatus.APPLIED, Instant.now(), ApplicationSource.COMPANY_WEBSITE, "ref-1", null, null, null
        );

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(jobApplicationRepository.findById(appId)).thenReturn(Optional.of(otherApp));

        CreateAssessmentRequest request = new CreateAssessmentRequest(
            jobId, appId, null, AssessmentPlatform.CODESIGNAL, "Title", 60, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> assessmentService.createAssessment(userId, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Create assessment rejects application for a different job")
    void createAssessmentRejectsMismatchedApplicationJob() {
        UUID appId = UUID.randomUUID();
        UUID differentJobId = UUID.randomUUID();
        JobApplication mismatchedApp = JobApplication.createNew(
            userId, differentJobId, ApplicationStatus.APPLIED, Instant.now(), ApplicationSource.COMPANY_WEBSITE, "ref-1", null, null, null
        );

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(jobApplicationRepository.findById(appId)).thenReturn(Optional.of(mismatchedApp));

        CreateAssessmentRequest request = new CreateAssessmentRequest(
            jobId, appId, null, AssessmentPlatform.CODESIGNAL, "Title", 60, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> assessmentService.createAssessment(userId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Job application does not match the specified job");
    }

    @Test
    @DisplayName("Get assessment by ID enforces user isolation")
    void getAssessmentEnforcesUserIsolation() {
        UUID assessmentId = UUID.randomUUID();
        when(assessmentRepository.findByIdAndUserId(assessmentId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assessmentService.getAssessmentById(userId, assessmentId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Online assessment not found");
    }

    @Test
    @DisplayName("Update assessment detects version mismatch and throws OptimisticLockingFailureException")
    void updateAssessmentDetectsOptimisticLockMismatch() {
        UUID assessmentId = UUID.randomUUID();
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "Title", 60, null, null, null, null
        );

        when(assessmentRepository.findByIdAndUserId(assessmentId, userId)).thenReturn(Optional.of(assessment));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));

        UpdateAssessmentRequest staleUpdate = new UpdateAssessmentRequest(
            AssessmentPlatform.HACKERRANK, "Updated", 60, null, null, null, null, null, null, 999L
        );

        assertThatThrownBy(() -> assessmentService.updateAssessment(userId, assessmentId, staleUpdate))
            .isInstanceOf(OptimisticLockingFailureException.class)
            .hasMessageContaining("Stale assessment update detected");
    }

    @Test
    @DisplayName("Start and submit assessment lifecycle transitions succeed")
    void startAndSubmitAssessment() {
        UUID assessmentId = UUID.randomUUID();
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "Title", 60, null, null, null, null
        );

        when(assessmentRepository.findByIdAndUserId(assessmentId, userId)).thenReturn(Optional.of(assessment));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(assessmentRepository.save(any(OnlineAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Start
        OnlineAssessmentResponse startedResp = assessmentService.startAssessment(userId, assessmentId, new StartAssessmentRequest(Instant.now(), "Starting"));
        assertThat(startedResp.status()).isEqualTo(AssessmentStatus.IN_PROGRESS);

        // 2. Submit
        SubmitAssessmentRequest submitReq = new SubmitAssessmentRequest(
            new BigDecimal("95.0"), new BigDecimal("100.0"), "Passed all test cases", "https://github.com/solution", Instant.now()
        );
        OnlineAssessmentResponse submittedResp = assessmentService.submitAssessment(userId, assessmentId, submitReq);
        assertThat(submittedResp.status()).isEqualTo(AssessmentStatus.SUBMITTED);
        assertThat(submittedResp.score()).isEqualByComparingTo("95.0");
        assertThat(submittedResp.maxScore()).isEqualByComparingTo("100.0");
        assertThat(submittedResp.submissionRepoUrl()).isEqualTo("https://github.com/solution");
    }

    @Test
    @DisplayName("Record result transitions active assessment to SUBMITTED when passed")
    void recordResultTransitionsStatus() {
        UUID assessmentId = UUID.randomUUID();
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "Title", 60, null, null, null, null
        );

        when(assessmentRepository.findByIdAndUserId(assessmentId, userId)).thenReturn(Optional.of(assessment));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(assessmentRepository.save(any(OnlineAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

        RecordAssessmentResultRequest req = new RecordAssessmentResultRequest(
            AssessmentResult.PASSED, new BigDecimal("98.0"), new BigDecimal("100.0"), "Confirmed pass"
        );

        OnlineAssessmentResponse resp = assessmentService.recordResult(userId, assessmentId, req);
        assertThat(resp.result()).isEqualTo(AssessmentResult.PASSED);
        assertThat(resp.status()).isEqualTo(AssessmentStatus.SUBMITTED);
        assertThat(resp.score()).isEqualByComparingTo("98.0");
    }

    @Test
    @DisplayName("Checklist toggling and explicit state update work accurately")
    void checklistToggleAndState() {
        UUID assessmentId = UUID.randomUUID();
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "Title", 60, null, null, null, null
        );
        AssessmentChecklistItem item = AssessmentChecklistItem.create(assessmentId, "ALGORITHMS", "Practice Trees", "Desc", 1);
        assessment.addChecklistItem(item);

        when(assessmentRepository.findByIdAndUserId(assessmentId, userId)).thenReturn(Optional.of(assessment));
        when(assessmentRepository.save(any(OnlineAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Toggle to true
        AssessmentChecklistBundleResponse bundle1 = assessmentService.toggleChecklistItem(userId, assessmentId, item.getId(), null);
        assertThat(bundle1.completedItems()).isEqualTo(1);
        assertThat(bundle1.completionPercentage()).isEqualTo(100);

        // Explicit set to false
        AssessmentChecklistBundleResponse bundle2 = assessmentService.toggleChecklistItem(userId, assessmentId, item.getId(), new ToggleChecklistItemRequest(false));
        assertThat(bundle2.completedItems()).isEqualTo(0);
        assertThat(bundle2.completionPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Generate briefing invokes AIProvider, merges checklist without duplicate titles, and preserves progress")
    void generateBriefingMergesPreservingProgress() {
        UUID assessmentId = UUID.randomUUID();
        OnlineAssessment assessment = OnlineAssessment.create(
            userId, jobId, null, null, AssessmentPlatform.HACKERRANK, "Title", 60, null, null, null, null
        );

        // Existing completed checklist item
        AssessmentChecklistItem existingCompleted = AssessmentChecklistItem.create(
            assessmentId, "ENVIRONMENT", "Validate Platform Workspace and Keyboard Shortcuts", "Existing description", 1
        );
        existingCompleted.setCompleted(true);
        assessment.addChecklistItem(existingCompleted);

        when(assessmentRepository.findByIdAndUserId(assessmentId, userId)).thenReturn(Optional.of(assessment));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of());
        when(assessmentRepository.save(any(OnlineAssessment.class))).thenAnswer(inv -> inv.getArgument(0));

        // AI Provider mock returns recommendations including the same title and novel titles
        AIAssessmentBriefingResponse aiResponse = new AIAssessmentBriefingResponse(
            "HackerRank guidance",
            "Pacing advice",
            List.of("Algorithms"),
            List.of(
                new AIAssessmentChecklistItem("ENVIRONMENT", "Validate Platform Workspace and Keyboard Shortcuts", "New description", 1),
                new AIAssessmentChecklistItem("ALGORITHMS", "Review Graph Traversals", "Novel description", 2)
            ),
            new BigDecimal("0.90")
        );
        when(aiProvider.generateAssessmentBriefing(any())).thenReturn(aiResponse);

        AssessmentBriefingResponse briefing = assessmentService.generateBriefing(userId, assessmentId, new GenerateBriefingRequest("Notes"));

        assertThat(briefing).isNotNull();
        assertThat(briefing.totalChecklistItems()).isEqualTo(2);
        // Existing item preserved with completed state
        assertThat(briefing.completedChecklistItems()).isEqualTo(1);
        assertThat(briefing.completionPercentage()).isEqualTo(50);

        AssessmentChecklistItemResponse item1 = briefing.checklist().get(0);
        assertThat(item1.id()).isEqualTo(existingCompleted.getId());
        assertThat(item1.isCompleted()).isTrue();

        AssessmentChecklistItemResponse item2 = briefing.checklist().get(1);
        assertThat(item2.title()).isEqualTo("Review Graph Traversals");
        assertThat(item2.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("Dashboard summary computes counts scoped strictly to authenticated user")
    void dashboardSummaryScopedToUser() {
        when(assessmentRepository.countByUserId(userId)).thenReturn(10L);
        when(assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.INVITED)).thenReturn(3L);
        when(assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.IN_PROGRESS)).thenReturn(2L);
        when(assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.SUBMITTED)).thenReturn(4L);
        when(assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.EXPIRED)).thenReturn(1L);
        when(assessmentRepository.countByUserIdAndStatus(userId, AssessmentStatus.ABANDONED)).thenReturn(0L);
        when(assessmentRepository.countDueSoonByUserId(eq(userId), any(), any())).thenReturn(2L);
        when(assessmentRepository.findActiveByUserId(userId)).thenReturn(List.of());

        AssessmentDashboardSummaryResponse summary = assessmentService.getDashboardSummary(userId);

        assertThat(summary.totalAssessments()).isEqualTo(10L);
        assertThat(summary.invited()).isEqualTo(3L);
        assertThat(summary.inProgress()).isEqualTo(2L);
        assertThat(summary.submitted()).isEqualTo(4L);
        assertThat(summary.dueSoon()).isEqualTo(2L);
        assertThat(summary.overdue()).isEqualTo(0L);
    }
}
