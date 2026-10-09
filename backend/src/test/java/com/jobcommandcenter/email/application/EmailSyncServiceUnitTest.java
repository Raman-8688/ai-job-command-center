package com.jobcommandcenter.email.application;

import com.jobcommandcenter.ai.domain.AIEmailClassificationRequest;
import com.jobcommandcenter.ai.domain.AIEmailClassificationResponse;
import com.jobcommandcenter.ai.domain.AIEmailJobExtractionRequest;
import com.jobcommandcenter.ai.domain.AIEmailJobExtractionResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.email.api.dto.SyncEmailsResponse;
import com.jobcommandcenter.email.domain.*;
import com.jobcommandcenter.email.infrastructure.client.GmailClient;
import com.jobcommandcenter.email.infrastructure.client.RemoteEmailDetails;
import com.jobcommandcenter.email.infrastructure.client.RemoteEmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("EmailSyncService Application Service Unit Tests")
class EmailSyncServiceUnitTest {

    private EmailConnectionRepository emailConnectionRepository;
    private EmailRepository emailRepository;
    private GmailClient gmailClient;
    private AIProvider aiProvider;
    private EmailSyncService syncService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        emailConnectionRepository = Mockito.mock(EmailConnectionRepository.class);
        emailRepository = Mockito.mock(EmailRepository.class);
        gmailClient = Mockito.mock(GmailClient.class);
        aiProvider = Mockito.mock(AIProvider.class);
        syncService = new EmailSyncService(emailConnectionRepository, emailRepository, gmailClient, aiProvider);

        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("syncEmails successfully fetches, deduplicates, and ingests new emails")
    void testSyncEmailsSuccess() {
        EmailConnection connection = EmailConnection.createNew(
            userId,
            EmailProviderType.GMAIL,
            "alex@example.com",
            "token-123",
            "refresh-123",
            Instant.now().plus(1, ChronoUnit.HOURS),
            "scopes"
        );

        when(emailConnectionRepository.findByUserIdAndProvider(userId, EmailProviderType.GMAIL))
            .thenReturn(Optional.of(connection));

        when(gmailClient.fetchMessages(eq("token-123"), anyString(), anyInt()))
            .thenReturn(List.of(
                new RemoteEmailMessage("msg-1", "thread-1"),
                new RemoteEmailMessage("msg-2", "thread-2")
            ));

        // msg-1 already exists in database (deduplication check)
        when(emailRepository.findByUserIdAndExternalMessageId(userId, "msg-1"))
            .thenReturn(Optional.of(Mockito.mock(Email.class)));
        // msg-2 is new
        when(emailRepository.findByUserIdAndExternalMessageId(userId, "msg-2"))
            .thenReturn(Optional.empty());

        RemoteEmailDetails details = new RemoteEmailDetails(
            "msg-2",
            "thread-2",
            "recruiting@meta.com",
            "alex@example.com",
            "Offer of Employment at Meta",
            "Snippet",
            "Plain body",
            "<p>HTML body</p>",
            Instant.now()
        );

        when(gmailClient.fetchMessageDetails("token-123", "msg-2")).thenReturn(details);
        when(aiProvider.classifyEmail(any(AIEmailClassificationRequest.class)))
            .thenReturn(new AIEmailClassificationResponse("OFFER", new BigDecimal("0.980"), "Offer matched"));
        when(aiProvider.extractJobFromEmail(any(AIEmailJobExtractionRequest.class)))
            .thenReturn(new AIEmailJobExtractionResponse("Meta", "Principal Engineer", "REQ-META", "Next step", "Notes"));

        SyncEmailsResponse response = syncService.syncEmails(userId);

        assertEquals(SyncStatus.SUCCESS, response.syncStatus());
        assertEquals(2, response.messagesFetched());
        assertEquals(1, response.newMessagesSynced());
        verify(emailRepository, times(1)).save(any(Email.class));
        verify(emailConnectionRepository, atLeastOnce()).save(connection);
    }
}
