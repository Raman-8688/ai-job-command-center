package com.jobcommandcenter.email.application;

import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.email.domain.EmailConnection;
import com.jobcommandcenter.email.domain.EmailConnectionRepository;
import com.jobcommandcenter.email.domain.EmailProviderType;
import com.jobcommandcenter.email.infrastructure.client.GmailClient;
import com.jobcommandcenter.email.infrastructure.client.OAuthTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Application service managing email provider OAuth connection lifecycle.
 */
@Service
@Transactional
public class EmailConnectionService {

    private static final Logger log = LoggerFactory.getLogger(EmailConnectionService.class);

    private final EmailConnectionRepository emailConnectionRepository;
    private final GmailClient gmailClient;

    public EmailConnectionService(
        EmailConnectionRepository emailConnectionRepository,
        GmailClient gmailClient
    ) {
        this.emailConnectionRepository = emailConnectionRepository;
        this.gmailClient = gmailClient;
    }

    /**
     * Generates OAuth 2.0 authorization URL for connecting Gmail.
     */
    public String generateAuthorizationUrl(UUID userId) {
        String state = UUID.randomUUID().toString();
        log.info("Generating Gmail authorization URL for userId={}", userId);
        return gmailClient.buildAuthorizationUrl(state);
    }

    /**
     * Handles OAuth 2.0 callback, exchanging the authorization code for tokens and persisting the connection.
     */
    public EmailConnection handleOAuthCallback(UUID userId, String code, String state) {
        log.info("Processing Gmail OAuth callback for userId={}", userId);
        OAuthTokens tokens = gmailClient.exchangeCodeForTokens(code);
        String emailAddress = gmailClient.getUserEmailAddress(tokens.accessToken());

        Optional<EmailConnection> existing = emailConnectionRepository.findByUserIdAndProvider(userId, EmailProviderType.GMAIL);
        EmailConnection connection;
        if (existing.isPresent()) {
            connection = existing.get();
            connection.updateOAuthTokens(emailAddress, tokens.accessToken(), tokens.refreshToken(), tokens.expiresAt(), tokens.scopes());
        } else {
            connection = EmailConnection.createNew(
                userId,
                EmailProviderType.GMAIL,
                emailAddress,
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresAt(),
                tokens.scopes()
            );
        }

        return emailConnectionRepository.save(connection);
    }

    /**
     * Retrieves the active connection for a user. Throws 404 if no connection exists.
     */
    @Transactional(readOnly = true)
    public EmailConnection getConnection(UUID userId) {
        return emailConnectionRepository.findByUserIdAndProvider(userId, EmailProviderType.GMAIL)
            .orElseThrow(() -> new ResourceNotFoundException("No Gmail connection found for user"));
    }

    /**
     * Retrieves connection if present, without throwing exception.
     */
    @Transactional(readOnly = true)
    public Optional<EmailConnection> findConnection(UUID userId) {
        return emailConnectionRepository.findByUserIdAndProvider(userId, EmailProviderType.GMAIL);
    }

    /**
     * Disconnects and invalidates tokens for the user's Gmail connection.
     */
    public void disconnect(UUID userId) {
        log.info("Disconnecting Gmail for userId={}", userId);
        EmailConnection connection = getConnection(userId);
        connection.disconnect();
        emailConnectionRepository.save(connection);
    }
}
