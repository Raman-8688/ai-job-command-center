package com.jobcommandcenter.job.domain;

/**
 * Lifecycle status of a canonical job posting.
 */
public enum JobStatus {
    /**
     * Currently open and accepting applications.
     */
    ACTIVE,

    /**
     * Application deadline or posting has expired.
     */
    EXPIRED,

    /**
     * Confirmed closed by recruiter or employer.
     */
    CLOSED,

    /**
     * Archived for candidate history and reporting.
     */
    ARCHIVED
}
