package com.jobcommandcenter.email.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity / aggregate representing a user's authenticated email provider connection.
 */
public class EmailConnection {

    private final UUID id;
    private final UUID userId;
    private final EmailProviderType provider;
    private String emailAddress;
    private boolean connected;
    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiresAt;
    private String scopes;
    private Instant lastSyncAt;
    private String lastHistoryId;
    private SyncStatus syncStatus;
    private String syncErrorMessage;
    private int emailsSyncedCount;
    private final Instant createdAt;
    private Instant updatedAt;

    public EmailConnection(
        UUID id,
        UUID userId,
        EmailProviderType provider,
        String emailAddress,
        boolean connected,
        String accessToken,
        String refreshToken,
        Instant tokenExpiresAt,
        String scopes,
        Instant lastSyncAt,
        String lastHistoryId,
        SyncStatus syncStatus,
        String syncErrorMessage,
        int emailsSyncedCount,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.userId = Objects.requireNonNull(userId, "User ID must not be null");
        this.provider = Objects.requireNonNull(provider, "Provider must not be null");
        this.emailAddress = Objects.requireNonNull(emailAddress, "Email address must not be null");
        this.connected = connected;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
        this.scopes = scopes;
        this.lastSyncAt = lastSyncAt;
        this.lastHistoryId = lastHistoryId;
        this.syncStatus = syncStatus != null ? syncStatus : SyncStatus.IDLE;
        this.syncErrorMessage = syncErrorMessage;
        this.emailsSyncedCount = emailsSyncedCount;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static EmailConnection createNew(
        UUID userId,
        EmailProviderType provider,
        String emailAddress,
        String accessToken,
        String refreshToken,
        Instant tokenExpiresAt,
        String scopes
    ) {
        Instant now = Instant.now();
        return new EmailConnection(
            UUID.randomUUID(),
            userId,
            provider,
            emailAddress,
            true,
            accessToken,
            refreshToken,
            tokenExpiresAt,
            scopes,
            null,
            null,
            SyncStatus.IDLE,
            null,
            0,
            now,
            now
        );
    }

    public void updateOAuthTokens(String emailAddress, String accessToken, String refreshToken, Instant tokenExpiresAt, String scopes) {
        this.emailAddress = Objects.requireNonNull(emailAddress, "Email address must not be null");
        this.accessToken = accessToken;
        if (refreshToken != null && !refreshToken.isBlank()) {
            this.refreshToken = refreshToken;
        }
        this.tokenExpiresAt = tokenExpiresAt;
        this.scopes = scopes;
        this.connected = true;
        this.updatedAt = Instant.now();
    }

    public void disconnect() {
        this.connected = false;
        this.accessToken = null;
        this.refreshToken = null;
        this.tokenExpiresAt = null;
        this.syncStatus = SyncStatus.IDLE;
        this.syncErrorMessage = null;
        this.updatedAt = Instant.now();
    }

    public void startSync() {
        this.syncStatus = SyncStatus.SYNCING;
        this.syncErrorMessage = null;
        this.updatedAt = Instant.now();
    }

    public void recordSyncSuccess(String historyId, int newMessagesCount) {
        this.syncStatus = SyncStatus.SUCCESS;
        this.syncErrorMessage = null;
        this.lastSyncAt = Instant.now();
        if (historyId != null && !historyId.isBlank()) {
            this.lastHistoryId = historyId;
        }
        this.emailsSyncedCount += newMessagesCount;
        this.updatedAt = Instant.now();
    }

    public void recordSyncFailure(String errorMessage) {
        this.syncStatus = SyncStatus.FAILED;
        this.syncErrorMessage = errorMessage;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public EmailProviderType getProvider() { return provider; }
    public String getEmailAddress() { return emailAddress; }
    public boolean isConnected() { return connected; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getTokenExpiresAt() { return tokenExpiresAt; }
    public String getScopes() { return scopes; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public String getLastHistoryId() { return lastHistoryId; }
    public SyncStatus getSyncStatus() { return syncStatus; }
    public String getSyncErrorMessage() { return syncErrorMessage; }
    public int getEmailsSyncedCount() { return emailsSyncedCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
