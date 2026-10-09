package com.jobcommandcenter.email.api;

import com.jobcommandcenter.email.api.dto.SyncEmailsResponse;
import com.jobcommandcenter.email.api.dto.SyncStatusResponse;
import com.jobcommandcenter.email.application.EmailSyncService;
import com.jobcommandcenter.security.jwt.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for email synchronization triggers and status.
 */
@RestController
@RequestMapping("/api/email")
public class EmailSyncController {

    private final EmailSyncService syncService;

    public EmailSyncController(EmailSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncEmailsResponse> triggerSync(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        SyncEmailsResponse response = syncService.syncEmails(securityUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sync/status")
    public ResponseEntity<SyncStatusResponse> getSyncStatus(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        SyncStatusResponse response = syncService.getSyncStatus(securityUser.getId());
        return ResponseEntity.ok(response);
    }
}
