package com.jobcommandcenter.security.jwt;

import com.jobcommandcenter.user.domain.User;

import java.util.UUID;

/**
 * Service contract for JWT generation, parsing, and cryptographic verification.
 */
public interface JwtTokenService {

    String generateToken(User user);

    UUID extractUserId(String token);

    String extractEmail(String token);

    boolean validateToken(String token);
}
