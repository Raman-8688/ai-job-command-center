package com.jobcommandcenter.application.application;

import com.jobcommandcenter.ai.domain.AIApplicationGuidanceRequest;
import com.jobcommandcenter.ai.domain.AIApplicationGuidanceResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.application.api.dto.*;
import com.jobcommandcenter.application.domain.*;
import com.jobcommandcenter.common.error.BusinessException;
import com.jobcommandcenter.common.error.ConflictException;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.resume.domain.ResumeRepository;
import com.jobcommandcenter.resume.domain.TailoredResumeRepository;
import com.jobcommandcenter.user.domain.Role;
import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceUnitTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private TailoredResumeRepository tailoredResumeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIProvider aiProvider;

    private JobApplicationService service;

    private UUID userId;
    private UUID otherUserId;
    private UUID jobId;
    private Job sampleJob;

    @BeforeEach
    void setUp() {
        service = new JobApplicationService(
            jobApplicationRepository,
            jobRepository,
            resumeRepository,
            tailoredResumeRepository,
            userRepository,
            aiProvider
        );

        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        sampleJob = Job.createNew(
            "ext-123",
            "Senior Backend Engineer",
            "Stripe",
            "https://stripe.com",
            "https://stripe.com/jobs/123",
            "Build payments infrastructure",
            "San Francisco, CA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            new BigDecimal("10.0"),
            new BigDecimal("160000"),
            new BigDecimal("210000"),
            "USD",
            JobSource.LINKEDIN,
            "https://linkedin.com/jobs/123",
            Instant.now(),
            null
        );
        jobId = sampleJob.getId();
    }

    @Test
    @DisplayName("createApplication throws ResourceNotFoundException when job does not exist")
    void createApplicationThrowsWhenJobMissing() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        CreateApplicationRequest request = new CreateApplicationRequest(
            jobId, ApplicationStatus.DRAFT, ApplicationSource.LINKEDIN, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> service.createApplication(userId, request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Job not found");
    }

    @Test
    @DisplayName("createApplication throws ConflictException when user already applied for job")
    void createApplicationThrowsWhenDuplicate() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(sampleJob));
        when(jobApplicationRepository.existsByUserIdAndJobId(userId, jobId)).thenReturn(true);

        CreateApplicationRequest request = new CreateApplicationRequest(
            jobId, ApplicationStatus.DRAFT, ApplicationSource.LINKEDIN, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> service.createApplication(userId, request))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("An application already exists for this job");
    }

    @Test
    @DisplayName("createApplication creates and persists application with valid input")
    void createApplicationSucceeds() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(sampleJob));
        when(jobApplicationRepository.existsByUserIdAndJobId(userId, jobId)).thenReturn(false);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateApplicationRequest request = new CreateApplicationRequest(
            jobId, ApplicationStatus.APPLIED, ApplicationSource.LINKEDIN, Instant.now(), "REF-APP-1", "Applied on company portal", null, null, null
        );

        JobApplicationResponse response = service.createApplication(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.jobTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(response.companyName()).isEqualTo("Stripe");
        assertThat(response.status()).isEqualTo(ApplicationStatus.APPLIED);
        verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("getApplicationById throws ResourceNotFoundException if application belongs to another user")
    void getApplicationByIdEnforcesMultiTenantIsolation() {
        UUID appId = UUID.randomUUID();
        JobApplication foreignApp = JobApplication.createNew(
            otherUserId, jobId, ApplicationStatus.DRAFT, null, ApplicationSource.MANUAL, null, null, null, null
        );
        when(jobApplicationRepository.findById(appId)).thenReturn(Optional.of(foreignApp));

        assertThatThrownBy(() -> service.getApplicationById(userId, appId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Job application not found");
    }

    @Test
    @DisplayName("deleteApplication rejects active applications in SCREENING or INTERVIEW")
    void deleteApplicationRejectsActiveState() {
        UUID appId = UUID.randomUUID();
        JobApplication activeApp = new JobApplication(
            appId,
            userId,
            jobId,
            null,
            null,
            ApplicationStatus.INTERVIEW,
            Instant.now(),
            ApplicationSource.MANUAL,
            null,
            null,
            null,
            Instant.now(),
            Instant.now(),
            0L,
            List.of()
        );
        when(jobApplicationRepository.findById(appId)).thenReturn(Optional.of(activeApp));

        assertThatThrownBy(() -> service.deleteApplication(userId, appId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only DRAFT or WITHDRAWN applications can be deleted");
    }

    @Test
    @DisplayName("getApplicationGuidance calls AI provider and returns advisory response")
    void getApplicationGuidanceCallsAi() {
        UUID appId = UUID.randomUUID();
        JobApplication app = JobApplication.createNew(
            userId, jobId, ApplicationStatus.APPLIED, Instant.now(), ApplicationSource.COMPANY_WEBSITE, null, null, null, null
        );
        when(jobApplicationRepository.findById(appId)).thenReturn(Optional.of(app));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(sampleJob));

        User user = User.createNew("user@test.com", "hash", "Alex", "Smith", "Alex S.", Role.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AIApplicationGuidanceResponse mockAi = new AIApplicationGuidanceResponse(
            "Send courteous status inquiry to talent acquisition team",
            "Over 5 days have elapsed since submission without a formal response.",
            "Dear Stripe Recruiting Team..."
        );
        when(aiProvider.generateApplicationGuidance(any(AIApplicationGuidanceRequest.class))).thenReturn(mockAi);

        ApplicationGuidanceResponse guidance = service.getApplicationGuidance(userId, appId);

        assertThat(guidance).isNotNull();
        assertThat(guidance.recommendedAction()).contains("status inquiry");
        assertThat(guidance.draftedFollowUpMessage()).contains("Stripe Recruiting Team");
    }
}
