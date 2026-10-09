package com.jobcommandcenter.email.application;

import com.jobcommandcenter.ai.domain.AIEmailClassificationRequest;
import com.jobcommandcenter.ai.domain.AIEmailClassificationResponse;
import com.jobcommandcenter.ai.domain.AIEmailJobExtractionRequest;
import com.jobcommandcenter.ai.domain.AIEmailJobExtractionResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.email.domain.*;
import com.jobcommandcenter.job.api.JobResponse;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobRepository;
import com.jobcommandcenter.job.domain.UserJob;
import com.jobcommandcenter.job.domain.UserJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("EmailService Application Service Unit Tests")
class EmailServiceUnitTest {

    private EmailRepository emailRepository;
    private JobRepository jobRepository;
    private UserJobRepository userJobRepository;
    private AIProvider aiProvider;
    private EmailService emailService;

    private UUID userId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() {
        emailRepository = Mockito.mock(EmailRepository.class);
        jobRepository = Mockito.mock(JobRepository.class);
        userJobRepository = Mockito.mock(UserJobRepository.class);
        aiProvider = Mockito.mock(AIProvider.class);
        emailService = new EmailService(emailRepository, jobRepository, userJobRepository, aiProvider);

        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getEmailById throws ResourceNotFoundException if user is not the owner")
    void testOwnershipIsolationOnGetEmail() {
        UUID emailId = UUID.randomUUID();
        Email email = Email.createNew(
            otherUserId,
            UUID.randomUUID(),
            "msg-123",
            "thread-123",
            "sender@example.com",
            "recipient@example.com",
            "Subject",
            "Snippet",
            "Body",
            "<p>Body</p>",
            Instant.now()
        );

        when(emailRepository.findById(emailId)).thenReturn(Optional.of(email));

        assertThrows(ResourceNotFoundException.class, () -> emailService.getEmailById(userId, emailId));
    }

    @Test
    @DisplayName("reclassifyEmail triggers AIProvider and updates email classification")
    void testReclassifyEmail() {
        UUID emailId = UUID.randomUUID();
        Email email = Email.createNew(
            userId,
            UUID.randomUUID(),
            "msg-123",
            "thread-123",
            "recruiter@google.com",
            "alex@example.com",
            "Google Interview",
            "Interview scheduled",
            "Body text",
            "<p>Body text</p>",
            Instant.now()
        );

        when(emailRepository.findById(emailId)).thenReturn(Optional.of(email));
        when(aiProvider.classifyEmail(any(AIEmailClassificationRequest.class)))
            .thenReturn(new AIEmailClassificationResponse("INTERVIEW_INVITATION", new BigDecimal("0.960"), "Matched interview keywords"));
        when(emailRepository.save(any(Email.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Email updated = emailService.reclassifyEmail(userId, emailId);

        assertEquals(EmailClassification.INTERVIEW_INVITATION, updated.getClassification());
        assertEquals(new BigDecimal("0.960"), updated.getClassificationConfidence());
        verify(emailRepository).save(email);
    }

    @Test
    @DisplayName("createJobFromEmail extracts details, creates Job, tracking UserJob, and associates email")
    void testCreateJobFromEmail() {
        UUID emailId = UUID.randomUUID();
        Email email = Email.createNew(
            userId,
            UUID.randomUUID(),
            "msg-123",
            "thread-123",
            "jobs@stripe.com",
            "alex@example.com",
            "Stripe Staff Engineer Opportunity",
            "We are hiring...",
            "Job description text for Stripe Staff Engineer",
            "<p>HTML</p>",
            Instant.now()
        );

        when(emailRepository.findById(emailId)).thenReturn(Optional.of(email));
        when(aiProvider.extractJobFromEmail(any(AIEmailJobExtractionRequest.class)))
            .thenReturn(new AIEmailJobExtractionResponse("Stripe", "Staff Engineer", "REQ-999", "Next step", "Notes"));
        when(jobRepository.findByDeduplicationHash(anyString())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userJobRepository.existsByUserIdAndJobId(eq(userId), any(UUID.class))).thenReturn(false);
        when(userJobRepository.save(any(UserJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailRepository.save(any(Email.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = emailService.createJobFromEmail(userId, emailId);

        assertNotNull(response);
        assertEquals("Stripe", response.companyName());
        assertEquals("Staff Engineer", response.title());
        assertNotNull(email.getAssociatedJobId());
        verify(jobRepository).save(any(Job.class));
        verify(userJobRepository).save(any(UserJob.class));
        verify(emailRepository).save(email);
    }
}
