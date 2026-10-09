package com.jobcommandcenter.email.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.email.api.dto.EmailPageResponse;
import com.jobcommandcenter.email.api.dto.EmailSummaryResponse;
import com.jobcommandcenter.email.domain.*;
import com.jobcommandcenter.job.api.JobResponse;
import com.jobcommandcenter.job.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for email searching, classification override, AI processing, and job association.
 */
@Service
@Transactional
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailRepository emailRepository;
    private final JobRepository jobRepository;
    private final UserJobRepository userJobRepository;
    private final AIProvider aiProvider;

    public EmailService(
        EmailRepository emailRepository,
        JobRepository jobRepository,
        UserJobRepository userJobRepository,
        AIProvider aiProvider
    ) {
        this.emailRepository = emailRepository;
        this.jobRepository = jobRepository;
        this.userJobRepository = userJobRepository;
        this.aiProvider = aiProvider;
    }

    /**
     * Retrieves an email by ID, strictly enforcing user ownership.
     */
    @Transactional(readOnly = true)
    public Email getEmailById(UUID userId, UUID emailId) {
        Email email = emailRepository.findById(emailId)
            .orElseThrow(() -> new ResourceNotFoundException("Email not found with ID: " + emailId));

        if (!email.getUserId().equals(userId)) {
            // Enforce tenant isolation with 404
            throw new ResourceNotFoundException("Email not found with ID: " + emailId);
        }
        return email;
    }

    /**
     * Queries and paginates emails for the authenticated user based on search and filter criteria.
     */
    @Transactional(readOnly = true)
    public EmailPageResponse searchEmails(EmailSearchCriteria criteria) {
        List<Email> content = emailRepository.search(criteria);
        long total = emailRepository.count(criteria);
        List<EmailSummaryResponse> summaries = content.stream()
            .map(EmailSummaryResponse::fromDomain)
            .collect(Collectors.toList());
        return EmailPageResponse.of(summaries, criteria.page(), criteria.size(), total);
    }

    /**
     * Manually overrides the classification of an email.
     */
    public Email updateClassification(UUID userId, UUID emailId, EmailClassification classification) {
        Email email = getEmailById(userId, emailId);
        email.updateClassification(classification, BigDecimal.ONE, "Manually updated by candidate");
        return emailRepository.save(email);
    }

    /**
     * Manually updates the workflow processing status.
     */
    public Email updateProcessingStatus(UUID userId, UUID emailId, EmailProcessingStatus status) {
        Email email = getEmailById(userId, emailId);
        email.updateProcessingStatus(status);
        return emailRepository.save(email);
    }

    /**
     * Re-triggers AI classification on an existing email.
     */
    public Email reclassifyEmail(UUID userId, UUID emailId) {
        Email email = getEmailById(userId, emailId);
        AIEmailClassificationResponse response = aiProvider.classifyEmail(
            new AIEmailClassificationRequest(email.getSubject(), email.getSender(), email.getSnippet(), email.getBodyPlain())
        );
        EmailClassification classification = EmailClassification.valueOf(response.classification());
        email.updateClassification(classification, response.confidence(), response.rationale());
        return emailRepository.save(email);
    }

    /**
     * Runs full AI job information extraction and marks email as processed.
     */
    public Email processEmail(UUID userId, UUID emailId) {
        Email email = getEmailById(userId, emailId);
        AIEmailJobExtractionResponse response = aiProvider.extractJobFromEmail(
            new AIEmailJobExtractionRequest(email.getSubject(), email.getSender(), email.getBodyPlain())
        );
        email.setExtractedDetails(
            response.companyName(),
            response.jobTitle(),
            response.externalJobId(),
            response.notes()
        );
        email.updateProcessingStatus(EmailProcessingStatus.PROCESSED);
        return emailRepository.save(email);
    }

    /**
     * Associates an email with an existing job.
     */
    public Email associateJob(UUID userId, UUID emailId, UUID jobId) {
        Email email = getEmailById(userId, emailId);

        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with ID: " + jobId);
        }

        email.associateJob(jobId);
        return emailRepository.save(email);
    }

    /**
     * Disassociates an email from its currently linked job.
     */
    public Email disassociateJob(UUID userId, UUID emailId) {
        Email email = getEmailById(userId, emailId);
        email.disassociateJob();
        return emailRepository.save(email);
    }

    /**
     * Automatically extracts job information from an email, creates or finds the Job, creates tracking UserJob, and associates the email.
     */
    public JobResponse createJobFromEmail(UUID userId, UUID emailId) {
        Email email = getEmailById(userId, emailId);

        String companyName = email.getExtractedCompanyName();
        String jobTitle = email.getExtractedJobTitle();

        // If extraction hasn't run yet, run it now
        if (companyName == null || companyName.isBlank() || jobTitle == null || jobTitle.isBlank()) {
            AIEmailJobExtractionResponse extracted = aiProvider.extractJobFromEmail(
                new AIEmailJobExtractionRequest(email.getSubject(), email.getSender(), email.getBodyPlain())
            );
            companyName = extracted.companyName();
            jobTitle = extracted.jobTitle();
            email.setExtractedDetails(companyName, jobTitle, extracted.externalJobId(), extracted.notes());
        }

        if (companyName == null || companyName.isBlank()) {
            companyName = "Company from Email";
        }
        if (jobTitle == null || jobTitle.isBlank()) {
            jobTitle = email.getSubject();
        }

        String location = "Remote / Unspecified";
        String dedupHash = Job.calculateDeduplicationHash(companyName, jobTitle, location);

        Optional<Job> existingJob = jobRepository.findByDeduplicationHash(dedupHash);
        Job job;
        if (existingJob.isPresent()) {
            job = existingJob.get();
            log.info("Found existing job for deduplicationHash={}: jobId={}", dedupHash, job.getId());
        } else {
            String description = email.getBodyPlain() != null && !email.getBodyPlain().isBlank()
                ? email.getBodyPlain()
                : (email.getSnippet() != null ? email.getSnippet() : email.getSubject());

            job = Job.createNew(
                email.getExtractedExternalId(),
                jobTitle,
                companyName,
                null,
                null,
                description,
                location,
                WorkMode.UNKNOWN,
                EmploymentType.FULL_TIME,
                null,
                null,
                null,
                null,
                null,
                JobSource.EMAIL,
                null,
                email.getReceivedAt(),
                null
            );
            job = jobRepository.save(job);
            log.info("Created new Job from email: jobId={}, title={}, company={}", job.getId(), jobTitle, companyName);
        }

        // Ensure UserJob tracking record exists
        if (!userJobRepository.existsByUserIdAndJobId(userId, job.getId())) {
            UserJob userJob = UserJob.create(userId, job.getId());
            userJobRepository.save(userJob);
        }

        // Link email to job
        email.associateJob(job.getId());
        emailRepository.save(email);

        return JobResponse.of(job, List.of());
    }


    /**
     * Lists all emails linked to a specific job for the user.
     */
    @Transactional(readOnly = true)
    public List<Email> getEmailsForJob(UUID userId, UUID jobId) {
        return emailRepository.findByUserIdAndAssociatedJobId(userId, jobId);
    }

    /**
     * Deletes an email record for the user.
     */
    public void deleteEmail(UUID userId, UUID emailId) {
        Email email = getEmailById(userId, emailId);
        emailRepository.delete(email);
    }
}
