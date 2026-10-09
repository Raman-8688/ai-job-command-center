package com.jobcommandcenter.email.domain;

/**
 * Synchronization lifecycle status for email accounts.
 */
public enum SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    FAILED
}
