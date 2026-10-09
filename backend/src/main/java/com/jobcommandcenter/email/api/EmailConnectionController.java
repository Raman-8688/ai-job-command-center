package com.jobcommandcenter.email.api;

import com.jobcommandcenter.email.api.dto.EmailConnectionResponse;
import com.jobcommandcenter.email.api.dto.GmailCallbackRequest;
import com.jobcommandcenter.email.api.dto.GmailConnectUrlResponse;
import com.jobcommandcenter.email.application.EmailConnectionService;
import com.jobcommandcenter.email.domain.EmailConnection;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Gmail OAuth 2.0 connection management.
 */
@RestController
@RequestMapping("/api/email/gmail")
public class EmailConnectionController {

    private final EmailConnectionService connectionService;

    public EmailConnectionController(EmailConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping("/connect-url")
    public ResponseEntity<GmailConnectUrlResponse> getConnectUrl(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        String authUrl = connectionService.generateAuthorizationUrl(securityUser.getId());
        return ResponseEntity.ok(new GmailConnectUrlResponse(authUrl, UUID.randomUUID().toString()));
    }

    @PostMapping("/callback")
    public ResponseEntity<EmailConnectionResponse> handleCallback(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody GmailCallbackRequest request
    ) {
        EmailConnection connection = connectionService.handleOAuthCallback(
            securityUser.getId(),
            request.code(),
            request.state()
        );
        return ResponseEntity.ok(EmailConnectionResponse.fromDomain(connection));
    }

    @GetMapping("/status")
    public ResponseEntity<EmailConnectionResponse> getConnectionStatus(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        EmailConnection connection = connectionService.getConnection(securityUser.getId());
        return ResponseEntity.ok(EmailConnectionResponse.fromDomain(connection));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        connectionService.disconnect(securityUser.getId());
        return ResponseEntity.noContent().build();
    }
}
