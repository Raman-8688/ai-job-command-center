package com.jobcommandcenter.email.api.dto;

import com.jobcommandcenter.email.domain.EmailProviderType;
import com.jobcommandcenter.email.domain.SyncStatus;

import java.time.Instant;

/**
 * Health and status response for user's email synchronization.
 */
public record SyncStatusResponse(
    boolean connected,
    EmailProviderType provider,
    String emailAddress,
    SyncStatus syncStatus,
    Instant lastSyncAt,
    String errorMessage,
    long totalEmailsCount
) {}
