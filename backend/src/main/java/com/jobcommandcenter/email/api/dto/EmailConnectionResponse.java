package com.jobcommandcenter.email.api.dto;

import com.jobcommandcenter.email.domain.EmailConnection;
import com.jobcommandcenter.email.domain.EmailProviderType;
import com.jobcommandcenter.email.domain.SyncStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Public response representing email provider connection status.
 * Never leaks access/refresh tokens.
 */
public record EmailConnectionResponse(
    UUID id,
    EmailProviderType provider,
    String emailAddress,
    boolean connected,
    Instant lastSyncAt,
    SyncStatus syncStatus,
    String syncErrorMessage,
    int emailsSyncedCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static EmailConnectionResponse fromDomain(EmailConnection domain) {
        return new EmailConnectionResponse(
            domain.getId(),
            domain.getProvider(),
            domain.getEmailAddress(),
            domain.isConnected(),
            domain.getLastSyncAt(),
            domain.getSyncStatus(),
            domain.getSyncErrorMessage(),
            domain.getEmailsSyncedCount(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
