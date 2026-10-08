package com.jobcommandcenter.user.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a user identity in the system.
 */
public class User {

    private final UUID id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String displayName;
    private AccountStatus accountStatus;
    private Role role;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    public User(UUID id,
                String email,
                String passwordHash,
                String firstName,
                String lastName,
                String displayName,
                AccountStatus accountStatus,
                Role role,
                Instant createdAt,
                Instant updatedAt,
                Instant lastLoginAt) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.email = normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.firstName = Objects.requireNonNull(firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(lastName, "Last name cannot be null");
        this.displayName = displayName != null ? displayName : firstName + " " + lastName;
        this.accountStatus = accountStatus != null ? accountStatus : AccountStatus.ACTIVE;
        this.role = role != null ? role : Role.USER;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static User createNew(String email,
                                 String passwordHash,
                                 String firstName,
                                 String lastName,
                                 String displayName,
                                 Role role) {
        Instant now = Instant.now();
        return new User(
            UUID.randomUUID(),
            email,
            passwordHash,
            firstName,
            lastName,
            displayName,
            AccountStatus.ACTIVE,
            role,
            now,
            now,
            null
        );
    }

    public static String normalizeEmail(String rawEmail) {
        if (rawEmail == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        return rawEmail.trim().toLowerCase();
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.accountStatus == AccountStatus.LOCKED;
    }

    public void changeStatus(AccountStatus status) {
        this.accountStatus = Objects.requireNonNull(status, "Account status cannot be null");
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDisplayName() { return displayName; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }
}
