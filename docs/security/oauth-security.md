# OAuth Security & Refresh Token Vault

## 1. Refresh Token Protection
Google OAuth refresh tokens provide long-lived access to the candidate's Gmail inbox. Storing refresh tokens in plaintext is a severe security vulnerability.

### Storage Scheme
- The `TokenVaultService` encrypts all refresh tokens using **AES-256-GCM** before writing to PostgreSQL.
- The encryption key is derived from `TOKEN_ENCRYPTION_KEY` using PBKDF2 with a random salt.
- Tokens are decrypted on-demand strictly in memory during OAuth token refresh operations.

## 2. Revocation & Token Lifecycle
If a user disconnects their Gmail integration, the backend initiates an immediate token revocation call to `https://oauth2.googleapis.com/revoke` and purges the encrypted record from the database.
