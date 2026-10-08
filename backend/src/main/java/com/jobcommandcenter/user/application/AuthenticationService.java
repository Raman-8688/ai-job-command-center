package com.jobcommandcenter.user.application;

import com.jobcommandcenter.security.jwt.JwtProperties;
import com.jobcommandcenter.security.jwt.JwtTokenService;
import com.jobcommandcenter.user.api.LoginRequest;
import com.jobcommandcenter.user.api.LoginResponse;
import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenService jwtTokenService,
                                 JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = User.normalizeEmail(request.email());

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed authentication attempt for email: {}", normalizedEmail);
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.isLocked()) {
            log.warn("Authentication rejected: Account locked for user: {}", user.getId());
            throw new LockedException("User account is locked");
        }

        if (!user.isActive()) {
            log.warn("Authentication rejected: Account inactive for user: {}", user.getId());
            throw new DisabledException("User account is inactive");
        }

        user.recordLogin();
        userRepository.save(user);

        String token = jwtTokenService.generateToken(user);
        log.info("User logged in successfully: {}", user.getId());

        return new LoginResponse(
            token,
            "Bearer",
            jwtProperties.getExpirationMs(),
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getRole().name()
        );
    }
}
