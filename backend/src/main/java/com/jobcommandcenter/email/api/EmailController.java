package com.jobcommandcenter.email.api;

import com.jobcommandcenter.email.api.dto.*;
import com.jobcommandcenter.email.application.EmailService;
import com.jobcommandcenter.email.domain.*;
import com.jobcommandcenter.job.api.JobResponse;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing, viewing, classifying, processing, and associating emails.
 */
@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<EmailPageResponse> listEmails(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required = false) EmailClassification classification,
        @RequestParam(required = false) EmailProcessingStatus processingStatus,
        @RequestParam(required = false) UUID associatedJobId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        EmailSearchCriteria criteria = new EmailSearchCriteria(
            securityUser.getId(),
            classification,
            processingStatus,
            associatedJobId,
            search,
            page,
            size
        );
        EmailPageResponse response = emailService.searchEmails(criteria);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailDetailResponse> getEmail(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        Email email = emailService.getEmailById(securityUser.getId(), id);
        return ResponseEntity.ok(EmailDetailResponse.fromDomain(email));
    }

    @PostMapping("/{id}/classify")
    public ResponseEntity<EmailDetailResponse> triggerReclassify(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        Email email = emailService.reclassifyEmail(securityUser.getId(), id);
        return ResponseEntity.ok(EmailDetailResponse.fromDomain(email));
    }

    @PatchMapping("/{id}/classification")
    public ResponseEntity<EmailDetailResponse> updateClassification(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateEmailClassificationRequest request
    ) {
        Email email = emailService.updateClassification(securityUser.getId(), id, request.classification());
        return ResponseEntity.ok(EmailDetailResponse.fromDomain(email));
    }

    @PatchMapping("/{id}/processing-status")
    public ResponseEntity<EmailDetailResponse> updateProcessingStatus(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProcessingStatusRequest request
    ) {
        Email email = emailService.updateProcessingStatus(securityUser.getId(), id, request.processingStatus());
        return ResponseEntity.ok(EmailDetailResponse.fromDomain(email));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<EmailDetailResponse> processEmail(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        Email email = emailService.processEmail(securityUser.getId(), id);
        return ResponseEntity.ok(EmailDetailResponse.fromDomain(email));
    }

    @PostMapping("/{id}/associate-job/{jobId}")
    public ResponseEntity<EmailDetailResponse> associateJob(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @PathVariable UUID jobId
    ) {
        Email email = emailService.associateJob(securityUser.getId(), id, jobId);
        return ResponseEntity.ok(EmailDetailResponse.fromDomain(email));
    }

    @DeleteMapping("/{id}/associate-job")
    public ResponseEntity<EmailDetailResponse> disassociateJob(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        Email email = emailService.disassociateJob(securityUser.getId(), id);
        return ResponseEntity.ok(EmailDetailResponse.fromDomain(email));
    }

    @PostMapping("/{id}/create-job")
    public ResponseEntity<JobResponse> createJobFromEmail(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        JobResponse job = emailService.createJobFromEmail(securityUser.getId(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmail(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        emailService.deleteEmail(securityUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
