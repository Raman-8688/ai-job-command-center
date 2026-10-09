package com.jobcommandcenter.email.infrastructure.persistence;

import com.jobcommandcenter.email.domain.EmailConnection;
import com.jobcommandcenter.email.domain.EmailProviderType;
import com.jobcommandcenter.email.domain.SyncStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for email_connections table.
 */
@Entity
@Table(name = "email_connections")
public class EmailConnectionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private EmailProviderType provider;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "is_connected", nullable = false)
    private boolean connected;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "scopes", length = 500)
    private String scopes;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "last_history_id", length = 100)
    private String lastHistoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 50)
    private SyncStatus syncStatus;

    @Column(name = "sync_error_message", columnDefinition = "TEXT")
    private String syncErrorMessage;

    @Column(name = "emails_synced_count", nullable = false)
    private int emailsSyncedCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EmailConnectionJpaEntity() {}

    public static EmailConnectionJpaEntity fromDomain(EmailConnection domain) {
        EmailConnectionJpaEntity entity = new EmailConnectionJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.provider = domain.getProvider();
        entity.emailAddress = domain.getEmailAddress();
        entity.connected = domain.isConnected();
        entity.accessToken = domain.getAccessToken();
        entity.refreshToken = domain.getRefreshToken();
        entity.tokenExpiresAt = domain.getTokenExpiresAt();
        entity.scopes = domain.getScopes();
        entity.lastSyncAt = domain.getLastSyncAt();
        entity.lastHistoryId = domain.getLastHistoryId();
        entity.syncStatus = domain.getSyncStatus();
        entity.syncErrorMessage = domain.getSyncErrorMessage();
        entity.emailsSyncedCount = domain.getEmailsSyncedCount();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    public EmailConnection toDomain() {
        return new EmailConnection(
            this.id,
            this.userId,
            this.provider,
            this.emailAddress,
            this.connected,
            this.accessToken,
            this.refreshToken,
            this.tokenExpiresAt,
            this.scopes,
            this.lastSyncAt,
            this.lastHistoryId,
            this.syncStatus,
            this.syncErrorMessage,
            this.emailsSyncedCount,
            this.createdAt,
            this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public EmailProviderType getProvider() { return provider; }
    public void setProvider(EmailProviderType provider) { this.provider = provider; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Instant getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(Instant tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public String getLastHistoryId() { return lastHistoryId; }
    public void setLastHistoryId(String lastHistoryId) { this.lastHistoryId = lastHistoryId; }
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    public String getSyncErrorMessage() { return syncErrorMessage; }
    public void setSyncErrorMessage(String syncErrorMessage) { this.syncErrorMessage = syncErrorMessage; }
    public int getEmailsSyncedCount() { return emailsSyncedCount; }
    public void setEmailsSyncedCount(int emailsSyncedCount) { this.emailsSyncedCount = emailsSyncedCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
