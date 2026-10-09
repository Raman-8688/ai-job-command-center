package com.jobcommandcenter.email.infrastructure.client;

import java.time.Instant;

/**
 * Detailed email message retrieved from remote provider.
 */
public record RemoteEmailDetails(
    String messageId,
    String threadId,
    String sender,
    String recipient,
    String subject,
    String snippet,
    String bodyPlain,
    String bodyHtml,
    Instant receivedAt
) {}
