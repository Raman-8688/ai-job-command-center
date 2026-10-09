package com.jobcommandcenter.email.api;

import com.jobcommandcenter.email.api.dto.EmailSummaryResponse;
import com.jobcommandcenter.email.application.EmailService;
import com.jobcommandcenter.email.domain.Email;
import com.jobcommandcenter.security.jwt.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for retrieving emails linked to a specific job.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobEmailController {

    private final EmailService emailService;

    public JobEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/{jobId}/emails")
    public ResponseEntity<List<EmailSummaryResponse>> getEmailsForJob(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID jobId
    ) {
        List<Email> emails = emailService.getEmailsForJob(securityUser.getId(), jobId);
        List<EmailSummaryResponse> responses = emails.stream()
            .map(EmailSummaryResponse::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
