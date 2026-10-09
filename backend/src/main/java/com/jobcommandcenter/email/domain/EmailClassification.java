package com.jobcommandcenter.email.domain;

/**
 * Intelligent job-search domain classification categories for incoming emails.
 */
public enum EmailClassification {
    UNCLASSIFIED,
    APPLICATION_CONFIRMATION,
    INTERVIEW_INVITATION,
    ASSESSMENT,
    REJECTION,
    OFFER,
    NETWORKING_OUTREACH,
    STATUS_UPDATE,
    SPAM_OR_IRRELEVANT,
    OTHER
}
