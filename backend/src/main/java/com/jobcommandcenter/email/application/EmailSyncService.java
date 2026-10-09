package com.jobcommandcenter.email.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.email.api.dto.SyncEmailsResponse;
import com.jobcommandcenter.email.api.dto.SyncStatusResponse;
import com.jobcommandcenter.email.domain.*;
import com.jobcommandcenter.email.infrastructure.client.GmailClient;
import com.jobcommandcenter.email.infrastructure.client.RemoteEmailDetails;
import com.jobcommandcenter.email.infrastructure.client.RemoteEmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service managing email background synchronization and inbox ingestion.
 */
@Service
@Transactional
public class EmailSyncService {

    private static final Logger log = LoggerFactory.getLogger(EmailSyncService.class);

    private final EmailConnectionRepository emailConnectionRepository;
    private final EmailRepository emailRepository;
    private final GmailClient gmailClient;
    private final AIProvider aiProvider;

    public EmailSyncService(
        EmailConnectionRepository emailConnectionRepository,
        EmailRepository emailRepository,
        GmailClient gmailClient,
        AIProvider aiProvider
    ) {
        this.emailConnectionRepository = emailConnectionRepository;
        this.emailRepository = emailRepository;
        this.gmailClient = gmailClient;
        this.aiProvider = aiProvider;
    }

    /**
     * Executes synchronization: fetches remote messages, deduplicates, persists, and performs initial AI classification.
     */
    public SyncEmailsResponse syncEmails(UUID userId) {
        log.info("Starting email sync for userId={}", userId);
        EmailConnection connection = emailConnectionRepository.findByUserIdAndProvider(userId, EmailProviderType.GMAIL)
            .orElseThrow(() -> new ResourceNotFoundException("No Gmail connection found. Please connect your Gmail account first."));

        if (!connection.isConnected() || connection.getAccessToken() == null) {
            throw new IllegalStateException("Gmail account is disconnected. Please re-authenticate.");
        }

        connection.startSync();
        emailConnectionRepository.save(connection);

        try {
            List<RemoteEmailMessage> messages = gmailClient.fetchMessages(connection.getAccessToken(), "label:INBOX", 25);
            int newCount = 0;

            for (RemoteEmailMessage remoteMsg : messages) {
                // Deduplicate against existing emails for this user
                if (emailRepository.findByUserIdAndExternalMessageId(userId, remoteMsg.messageId()).isPresent()) {
                    continue;
                }

                RemoteEmailDetails details = gmailClient.fetchMessageDetails(connection.getAccessToken(), remoteMsg.messageId());
                if (details == null) {
                    continue;
                }

                Email email = Email.createNew(
                    userId,
                    connection.getId(),
                    details.messageId(),
                    details.threadId(),
                    details.sender(),
                    details.recipient(),
                    details.subject(),
                    details.snippet(),
                    details.bodyPlain(),
                    details.bodyHtml(),
                    details.receivedAt()
                );

                // Perform automated AI classification
                try {
                    AIEmailClassificationResponse classResp = aiProvider.classifyEmail(
                        new AIEmailClassificationRequest(details.subject(), details.sender(), details.snippet(), details.bodyPlain())
                    );
                    EmailClassification classification = EmailClassification.valueOf(classResp.classification());
                    email.updateClassification(classification, classResp.confidence(), classResp.rationale());
                } catch (Exception e) {
                    log.warn("AI email classification failed for messageId={}, defaulting to UNCLASSIFIED: {}", details.messageId(), e.getMessage());
                }

                // Extract job opportunity details
                try {
                    AIEmailJobExtractionResponse jobResp = aiProvider.extractJobFromEmail(
                        new AIEmailJobExtractionRequest(details.subject(), details.sender(), details.bodyPlain())
                    );
                    email.setExtractedDetails(jobResp.companyName(), jobResp.jobTitle(), jobResp.externalJobId(), jobResp.notes());
                } catch (Exception e) {
                    log.warn("AI job extraction failed for messageId={}: {}", details.messageId(), e.getMessage());
                }

                emailRepository.save(email);
                newCount++;
            }

            connection.recordSyncSuccess(UUID.randomUUID().toString(), newCount);
            emailConnectionRepository.save(connection);

            log.info("Email sync complete for userId={}: {} fetched, {} newly stored", userId, messages.size(), newCount);
            return new SyncEmailsResponse(
                messages.size(),
                newCount,
                SyncStatus.SUCCESS,
                Instant.now(),
                "Successfully synchronized " + newCount + " new emails"
            );
        } catch (Exception e) {
            log.error("Email sync failed for userId={}: {}", userId, e.getMessage(), e);
            connection.recordSyncFailure(e.getMessage());
            emailConnectionRepository.save(connection);
            return new SyncEmailsResponse(
                0,
                0,
                SyncStatus.FAILED,
                Instant.now(),
                "Email sync failed: " + e.getMessage()
            );
        }
    }

    /**
     * Retrieves synchronization health and statistics.
     */
    @Transactional(readOnly = true)
    public SyncStatusResponse getSyncStatus(UUID userId) {
        Optional<EmailConnection> opt = emailConnectionRepository.findByUserIdAndProvider(userId, EmailProviderType.GMAIL);
        long count = emailRepository.countByUserId(userId);

        if (opt.isEmpty()) {
            return new SyncStatusResponse(
                false,
                EmailProviderType.GMAIL,
                null,
                SyncStatus.IDLE,
                null,
                null,
                count
            );
        }

        EmailConnection conn = opt.get();
        return new SyncStatusResponse(
            conn.isConnected(),
            conn.getProvider(),
            conn.getEmailAddress(),
            conn.getSyncStatus(),
            conn.getLastSyncAt(),
            conn.getSyncErrorMessage(),
            count
        );
    }
}
