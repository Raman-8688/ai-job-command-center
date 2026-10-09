package com.jobcommandcenter.email.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring configuration providing the active GmailClient bean.
 */
@Configuration
public class GmailClientConfig {

    @Bean
    @Primary
    public GmailClient activeGmailClient(
        MockGmailClient mockGmailClient,
        @Value("${app.gmail.client-id:mock-client-id}") String clientId
    ) {
        // If mock credentials or local testing, provide MockGmailClient
        return mockGmailClient;
    }
}
