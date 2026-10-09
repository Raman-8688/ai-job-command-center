package com.jobcommandcenter.email.infrastructure.client;

/**
 * Summary message identifier from remote email API.
 */
public record RemoteEmailMessage(
    String messageId,
    String threadId
) {}
