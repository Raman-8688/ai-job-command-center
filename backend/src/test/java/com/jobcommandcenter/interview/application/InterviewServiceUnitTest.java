package com.jobcommandcenter.interview.application;

import com.jobcommandcenter.ai.domain.AIInterviewPrepResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.ai.domain.AIPracticeQuestion;
import com.jobcommandcenter.application.domain.JobApplicationRepository;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.interview.api.dto.InterviewDashboardSummaryResponse;
import com.jobcommandcenter.interview.api.dto.InterviewPrepBundleResponse;
import com.jobcommandcenter.interview.api.dto.InterviewResponse;
import com.jobcommandcenter.interview.api.dto.ScheduleInterviewRequest;
import com.jobcommandcenter.interview.domain.*;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.skill.domain.SkillRepository;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceUnitTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AIProvider aiProvider;

    private InterviewService interviewService;

    private final UUID userId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();
    private Job testJob;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(
            interviewRepository,
            jobRepository,
            jobApplicationRepository,
            userSkillRepository,
            skillRepository,
            aiProvider
        );

        testJob = Job.createNew(
            "ext-1",
            "Staff Backend Engineer",
            "Google",
            "https://google.com",
            "https://careers.google.com/1",
            "Design large scale infrastructure",
            "Mountain View, CA",
            WorkMode.HYBRID,
            EmploymentType.FULL_TIME,
            new BigDecimal("6.0"),
            new BigDecimal("12.0"),
            new BigDecimal("220000"),
            new BigDecimal("280000"),
            "USD",
            JobSource.LINKEDIN,
            "https://careers.google.com/1",
            Instant.now(),
            null
        );
    }

    @Test
    @DisplayName("Schedule interview verifies job existence and saves aggregate")
    void scheduleInterviewSuccess() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            jobId,
            null,
            InterviewRound.SYSTEM_DESIGN,
            1,
            InterviewFormat.VIDEO_CALL,
            Instant.now().plus(2, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS).plus(45, ChronoUnit.MINUTES),
            "PST",
            "https://meet.google.com/xyz",
            null,
            "Alex Smith",
            "Staff Engineer",
            "Prepare distributed caching concepts"
        );

        InterviewResponse response = interviewService.scheduleInterview(userId, request);

        assertNotNull(response);
        assertEquals(InterviewRound.SYSTEM_DESIGN, response.round());
        assertEquals("Staff Backend Engineer", response.jobTitle());
        assertEquals("Google", response.companyName());
        assertEquals(InterviewStatus.SCHEDULED, response.status());
        verify(interviewRepository, times(1)).save(any(Interview.class));
    }

    @Test
    @DisplayName("Schedule interview throws ResourceNotFoundException if job not found")
    void scheduleInterviewJobNotFound() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        ScheduleInterviewRequest request = new ScheduleInterviewRequest(
            jobId, null, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            Instant.now().plus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            "UTC", null, null, null, null, null
        );

        assertThrows(ResourceNotFoundException.class, () ->
            interviewService.scheduleInterview(userId, request)
        );
    }

    @Test
    @DisplayName("Generate AI Prep invokes AI provider and attaches questions")
    void generateAiPrepSuccess() {
        Instant start = Instant.now().plus(3, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        Interview interview = Interview.create(
            userId, null, jobId, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            start, end, "UTC", null, null, null, null, null
        );

        when(interviewRepository.findByIdAndUserId(interview.getId(), userId)).thenReturn(Optional.of(interview));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of());

        AIInterviewPrepResponse mockResponse = new AIInterviewPrepResponse(
            88,
            "Comprehensive technical prep strategy",
            List.of(
                new AIPracticeQuestion("TECH", "How does Kafka handle partitioning?", "Topic layout", "STAR answer", new BigDecimal("0.95")),
                new AIPracticeQuestion("BEHAVIORAL", "Tell me about a tough deadline.", "Prioritization", "STAR answer", new BigDecimal("0.90"))
            )
        );
        when(aiProvider.generateInterviewPrep(any())).thenReturn(mockResponse);

        InterviewPrepBundleResponse bundle = interviewService.generateAiPrep(userId, interview.getId());

        assertNotNull(bundle);
        assertEquals(2, bundle.questions().size());
        assertEquals("Staff Backend Engineer", bundle.jobTitle());
        verify(interviewRepository, times(1)).save(interview);
    }

    @Test
    @DisplayName("getDashboardSummary returns 0 readiness score when user has no interviews")
    void getDashboardSummaryNoInterviewsReturnsZeroReadiness() {
        when(interviewRepository.countByUserId(userId)).thenReturn(0L);
        when(interviewRepository.countByUserIdAndStatus(userId, InterviewStatus.COMPLETED)).thenReturn(0L);
        when(interviewRepository.findUpcomingByUserId(eq(userId), any())).thenReturn(List.of());
        when(interviewRepository.findByUserId(eq(userId), any())).thenReturn(List.of());

        InterviewDashboardSummaryResponse summary = interviewService.getDashboardSummary(userId);

        assertNotNull(summary);
        assertEquals(0, summary.totalInterviews());
        assertEquals(0, summary.upcomingInterviews());
        assertEquals(0, summary.overallReadinessScore());
        assertNull(summary.nextUpcomingInterview());
    }

    @Test
    @DisplayName("getDashboardSummary returns 0 readiness score when interviews have no preparation questions")
    void getDashboardSummaryInterviewsWithoutPrepReturnsZeroReadiness() {
        Interview interview = Interview.create(
            userId, null, jobId, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            Instant.now().plus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            "UTC", null, null, null, null, null
        );

        when(interviewRepository.countByUserId(userId)).thenReturn(1L);
        when(interviewRepository.countByUserIdAndStatus(userId, InterviewStatus.COMPLETED)).thenReturn(0L);
        when(interviewRepository.findUpcomingByUserId(eq(userId), any())).thenReturn(List.of(interview));
        when(interviewRepository.findByUserId(eq(userId), any())).thenReturn(List.of(interview));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));

        InterviewDashboardSummaryResponse summary = interviewService.getDashboardSummary(userId);

        assertNotNull(summary);
        assertEquals(1, summary.totalInterviews());
        assertEquals(1, summary.upcomingInterviews());
        assertEquals(0, summary.overallReadinessScore());
        assertNotNull(summary.nextUpcomingInterview());
    }

    @Test
    @DisplayName("getDashboardSummary calculates readiness score accurately from persisted reviewed questions")
    void getDashboardSummaryCalculatesReadinessAccurately() {
        Interview interview = Interview.create(
            userId, null, jobId, InterviewRound.TECHNICAL_SCREEN, 1, InterviewFormat.VIDEO_CALL,
            Instant.now().plus(2, ChronoUnit.DAYS), Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            "UTC", null, null, null, null, null
        );

        InterviewPreparation prep1 = InterviewPreparation.create(interview.getId(), "TECH", "Q1", "TP1", "STAR1", new BigDecimal("0.90"));
        prep1.updateCandidateNotes("Answered 1", true); // reviewed

        InterviewPreparation prep2 = InterviewPreparation.create(interview.getId(), "SYSTEM_DESIGN", "Q2", "TP2", "STAR2", new BigDecimal("0.90"));
        prep2.updateCandidateNotes("Answered 2", true); // reviewed

        InterviewPreparation prep3 = InterviewPreparation.create(interview.getId(), "BEHAVIORAL", "Q3", "TP3", "STAR3", new BigDecimal("0.90"));
        prep3.updateCandidateNotes("Answered 3", true); // reviewed

        InterviewPreparation prep4 = InterviewPreparation.create(interview.getId(), "LEADERSHIP", "Q4", "TP4", "STAR4", new BigDecimal("0.90"));
        // not reviewed: isReviewed = false

        interview.addPreparations(List.of(prep1, prep2, prep3, prep4));

        when(interviewRepository.countByUserId(userId)).thenReturn(1L);
        when(interviewRepository.countByUserIdAndStatus(userId, InterviewStatus.COMPLETED)).thenReturn(0L);
        when(interviewRepository.findUpcomingByUserId(eq(userId), any())).thenReturn(List.of(interview));
        when(interviewRepository.findByUserId(eq(userId), any())).thenReturn(List.of(interview));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));

        InterviewDashboardSummaryResponse summary = interviewService.getDashboardSummary(userId);

        assertNotNull(summary);
        // 3 out of 4 reviewed => 75%
        assertEquals(75, summary.overallReadinessScore());
    }
}
