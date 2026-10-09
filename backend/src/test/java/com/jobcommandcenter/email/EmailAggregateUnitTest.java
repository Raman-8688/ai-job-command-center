package com.jobcommandcenter.email;

import com.jobcommandcenter.email.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Aggregate & Value Object Unit Tests")
class EmailAggregateUnitTest {

    @Test
    @DisplayName("EmailConnection correctly manages connection lifecycle and sync status")
    void testEmailConnectionLifecycle() {
        UUID userId = UUID.randomUUID();
        EmailConnection connection = EmailConnection.createNew(
            userId,
            EmailProviderType.GMAIL,
            "alex@example.com",
            "access-123",
            "refresh-456",
            Instant.now().plus(1, ChronoUnit.HOURS),
            "gmail.readonly"
        );

        assertTrue(connection.isConnected());
        assertEquals("alex@example.com", connection.getEmailAddress());
        assertEquals(SyncStatus.IDLE, connection.getSyncStatus());
        assertEquals(0, connection.getEmailsSyncedCount());

        // Test start sync
        connection.startSync();
        assertEquals(SyncStatus.SYNCING, connection.getSyncStatus());

        // Test record success
        connection.recordSyncSuccess("hist-789", 5);
        assertEquals(SyncStatus.SUCCESS, connection.getSyncStatus());
        assertEquals("hist-789", connection.getLastHistoryId());
        assertEquals(5, connection.getEmailsSyncedCount());
        assertNotNull(connection.getLastSyncAt());

        // Test record failure
        connection.recordSyncFailure("Connection timeout");
        assertEquals(SyncStatus.FAILED, connection.getSyncStatus());
        assertEquals("Connection timeout", connection.getSyncErrorMessage());

        // Test disconnect
        connection.disconnect();
        assertFalse(connection.isConnected());
        assertNull(connection.getAccessToken());
        assertNull(connection.getRefreshToken());
        assertEquals(SyncStatus.IDLE, connection.getSyncStatus());
    }

    @Test
    @DisplayName("Email aggregate manages classification, extraction, and job association")
    void testEmailAggregateMutations() {
        UUID userId = UUID.randomUUID();
        UUID connectionId = UUID.randomUUID();
        Instant now = Instant.now();

        Email email = Email.createNew(
            userId,
            connectionId,
            "msg-001",
            "thread-001",
            "recruiter@amazon.com",
            "alex@example.com",
            "Interview invitation for SDE II",
            "We would like to invite you for an interview...",
            "Full email text",
            "<p>Full email text</p>",
            now
        );

        assertEquals(EmailClassification.UNCLASSIFIED, email.getClassification());
        assertEquals(EmailProcessingStatus.UNPROCESSED, email.getProcessingStatus());
        assertNull(email.getAssociatedJobId());

        // Test classification
        email.updateClassification(EmailClassification.INTERVIEW_INVITATION, new BigDecimal("0.950"), "Matched keyword interview");
        assertEquals(EmailClassification.INTERVIEW_INVITATION, email.getClassification());
        assertEquals(new BigDecimal("0.950"), email.getClassificationConfidence());

        // Test extracted details
        email.setExtractedDetails("Amazon", "Software Development Engineer II", "REQ-101", "Prepare system design");
        assertEquals("Amazon", email.getExtractedCompanyName());
        assertEquals("Software Development Engineer II", email.getExtractedJobTitle());
        assertEquals("REQ-101", email.getExtractedExternalId());

        // Test associate job
        UUID jobId = UUID.randomUUID();
        email.associateJob(jobId);
        assertEquals(jobId, email.getAssociatedJobId());
        assertEquals(EmailProcessingStatus.PROCESSED, email.getProcessingStatus());

        // Test disassociate job
        email.disassociateJob();
        assertNull(email.getAssociatedJobId());
    }
}
