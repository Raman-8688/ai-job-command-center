package com.jobcommandcenter.email.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Deterministic Mock Gmail client for offline testing and local development.
 * Simulates OAuth authorization, token exchange, and provides realistic job application emails.
 */
@Component("mockGmailClient")
public class MockGmailClient implements GmailClient {

    private final String authUri;
    private final String redirectUri;
    private final String scopes;

    public MockGmailClient(
        @Value("${app.gmail.auth-uri:https://accounts.google.com/o/oauth2/v2/auth}") String authUri,
        @Value("${app.gmail.redirect-uri:http://localhost:4200/email/callback}") String redirectUri,
        @Value("${app.gmail.scopes:https://www.googleapis.com/auth/gmail.readonly}") String scopes
    ) {
        this.authUri = authUri;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        return authUri + "?response_type=code&client_id=mock-client-id&redirect_uri=" +
            redirectUri + "&scope=" + scopes + "&state=" + state + "&access_type=offline&prompt=consent";
    }

    @Override
    public OAuthTokens exchangeCodeForTokens(String code) {
        return new OAuthTokens(
            "mock-access-token-" + UUID.randomUUID(),
            "mock-refresh-token-" + UUID.randomUUID(),
            Instant.now().plus(3600, ChronoUnit.SECONDS),
            scopes
        );
    }

    @Override
    public OAuthTokens refreshTokens(String refreshToken) {
        return new OAuthTokens(
            "mock-refreshed-access-token-" + UUID.randomUUID(),
            refreshToken,
            Instant.now().plus(3600, ChronoUnit.SECONDS),
            scopes
        );
    }

    @Override
    public String getUserEmailAddress(String accessToken) {
        return "candidate.alex@gmail.com";
    }

    @Override
    public List<RemoteEmailMessage> fetchMessages(String accessToken, String query, int maxResults) {
        return List.of(
            new RemoteEmailMessage("msg-mock-101", "thread-mock-101"),
            new RemoteEmailMessage("msg-mock-102", "thread-mock-102"),
            new RemoteEmailMessage("msg-mock-103", "thread-mock-103"),
            new RemoteEmailMessage("msg-mock-104", "thread-mock-104"),
            new RemoteEmailMessage("msg-mock-105", "thread-mock-105")
        );
    }

    @Override
    public RemoteEmailDetails fetchMessageDetails(String accessToken, String messageId) {
        Instant now = Instant.now();
        return switch (messageId) {
            case "msg-mock-101" -> new RemoteEmailDetails(
                "msg-mock-101",
                "thread-mock-101",
                "Google Careers <jobs-noreply@google.com>",
                "candidate.alex@gmail.com",
                "Thank you for applying to Google: Senior Software Engineer (Backend)",
                "We have received your application for Senior Software Engineer (Backend) at Google. Our team will review your qualifications...",
                "Hi Alex,\n\nThank you for applying for Senior Software Engineer (Backend) at Google (Req #GOOG-98214).\n\nWe have received your application and resume. Our recruiting team is reviewing your background and will reach out if there is a match.\n\nBest regards,\nGoogle Talent Acquisition",
                "<p>Hi Alex,</p><p>Thank you for applying for Senior Software Engineer (Backend) at Google (Req #GOOG-98214).</p>",
                now.minus(2, ChronoUnit.DAYS)
            );
            case "msg-mock-102" -> new RemoteEmailDetails(
                "msg-mock-102",
                "thread-mock-102",
                "Amazon Recruiting <recruiting@amazon.com>",
                "candidate.alex@gmail.com",
                "Amazon Interview Invitation: Software Development Engineer II",
                "Congratulations! We would like to schedule a 60-minute technical phone screen with our engineering team for Software Development Engineer II...",
                "Hello Alex,\n\nWe were impressed by your profile and would love to invite you for a 60-minute technical interview with an Amazon engineering leader.\n\nPlease select your availability using the scheduling link below.\n\nLooking forward to speaking with you!\nAmazon Staffing Team",
                "<p>Hello Alex,</p><p>We would love to invite you for a 60-minute technical interview.</p>",
                now.minus(1, ChronoUnit.DAYS)
            );
            case "msg-mock-103" -> new RemoteEmailDetails(
                "msg-mock-103",
                "thread-mock-103",
                "Stripe Recruiting <assessments@stripe.com>",
                "candidate.alex@gmail.com",
                "Stripe Online Assessment: HackerRank Technical Challenge",
                "Please complete your Stripe online coding challenge within 5 days. Click here to begin your HackerRank assessment...",
                "Hi Alex,\n\nAs the next step for the Staff Backend Engineer role at Stripe, please complete this 90-minute HackerRank online technical coding challenge within 5 days.\n\nLink: https://hackerrank.com/test/stripe-eval-992\n\nGood luck!\nStripe University & Industry Recruiting",
                "<p>Please complete this 90-minute HackerRank technical assessment.</p>",
                now.minus(12, ChronoUnit.HOURS)
            );
            case "msg-mock-104" -> new RemoteEmailDetails(
                "msg-mock-104",
                "thread-mock-104",
                "Netflix Talent <careers@netflix.com>",
                "candidate.alex@gmail.com",
                "Update on your application for Senior Cloud Architect at Netflix",
                "Thank you for your interest in Netflix. After careful consideration, we have decided not to move forward with your application at this time...",
                "Dear Alex,\n\nThank you for taking the time to speak with our team regarding the Senior Cloud Architect position at Netflix.\n\nWhile your experience is impressive, we have decided to pursue other candidates whose experience aligns more closely with our current team priorities.\n\nWe wish you all the best in your job search.\nNetflix Talent Team",
                "<p>We have decided not to move forward with your application at this time.</p>",
                now.minus(6, ChronoUnit.HOURS)
            );
            case "msg-mock-105" -> new RemoteEmailDetails(
                "msg-mock-105",
                "thread-mock-105",
                "Meta Recruiting <offers@meta.com>",
                "candidate.alex@gmail.com",
                "Offer of Employment at Meta - Principal Engineer",
                "Congratulations Alex! We are pleased to extend a formal offer letter for the Principal Engineer position at Meta...",
                "Dear Alex,\n\nOn behalf of Meta, we are thrilled to extend a formal offer of employment for the Principal Engineer position at Meta!\n\nPlease review the attached offer letter and compensation package.\n\nWarm congratulations,\nMeta Leadership Recruiting",
                "<p>We are thrilled to extend a formal offer of employment for the Principal Engineer position at Meta.</p>",
                now.minus(2, ChronoUnit.HOURS)
            );
            default -> new RemoteEmailDetails(
                messageId,
                "thread-" + messageId,
                "recruiter@techcorp.com",
                "candidate.alex@gmail.com",
                "Career Opportunity at TechCorp: Software Engineer",
                "Reaching out regarding an exciting opportunity at TechCorp...",
                "Hi Alex,\n\nI came across your profile and noticed your strong background. We are actively hiring Software Engineers at TechCorp.\n\nLet me know if you would be open to a quick chat this week!\n\nBest,\nRecruiter",
                "<p>Reaching out regarding an exciting opportunity at TechCorp.</p>",
                now
            );
        };
    }
}
