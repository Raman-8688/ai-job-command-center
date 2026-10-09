package com.jobcommandcenter.email.api.dto;

import com.jobcommandcenter.email.domain.SyncStatus;

import java.time.Instant;

/**
 * Result payload of a sync operation.
 */
public record SyncEmailsResponse(
    int messagesFetched,
    int newMessagesSynced,
    SyncStatus syncStatus,
    Instant syncedAt,
    String message
) {}
