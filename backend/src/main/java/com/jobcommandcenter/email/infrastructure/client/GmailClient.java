package com.jobcommandcenter.email.infrastructure.client;

import java.util.List;

/**
 * Service Provider Interface for interacting with Gmail API and OAuth 2.0.
 */
public interface GmailClient {

    /**
     * Builds the Google OAuth 2.0 authorization URL for user consent.
     */
    String buildAuthorizationUrl(String state);

    /**
     * Exchanges the authorization code received from Google for access/refresh tokens.
     */
    OAuthTokens exchangeCodeForTokens(String code);

    /**
     * Refreshes expired access tokens using the refresh token.
     */
    OAuthTokens refreshTokens(String refreshToken);

    /**
     * Fetches the authenticated user's email address from Google.
     */
    String getUserEmailAddress(String accessToken);

    /**
     * Lists recent messages from the user's Gmail inbox.
     */
    List<RemoteEmailMessage> fetchMessages(String accessToken, String query, int maxResults);

    /**
     * Retrieves full payload and body of a specific email message.
     */
    RemoteEmailDetails fetchMessageDetails(String accessToken, String messageId);
}
