package com.jobcommandcenter.email.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload containing OAuth code returned from Google.
 */
public record GmailCallbackRequest(
    @NotBlank(message = "Authorization code must not be blank")
    String code,

    String state
) {}
