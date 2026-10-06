# OAuth 2.0 Strategy & Token Security

## 1. Flow Specification
The application employs standard **Authorization Code Flow with PKCE (Proof Key for Code Exchange)**:
1. User clicks "Connect Gmail" in the Settings UI.
2. Frontend redirects to Google's OAuth 2.0 authorization server with code challenge.
3. Google returns authorization code to backend callback `/api/auth/oauth2/callback/google`.
4. Backend exchanges authorization code for access token and refresh token directly over secure backchannel.

## 2. Least-Privilege Scope Matrix
- `https://www.googleapis.com/auth/gmail.readonly` (Read incoming emails)
- `https://www.googleapis.com/auth/gmail.compose` (Create drafts in user inbox)
- `https://www.googleapis.com/auth/gmail.send` (Send emails after human approval)

## 3. Token Vault & Storage Security
- Access tokens live in memory with short TTLs (~60 minutes).
- Refresh tokens are stored in the database encrypted using AES-256-GCM.
- Encryption keys are loaded via environment variables (`JWT_SECRET` / `TOKEN_ENCRYPTION_KEY`) and are never written to disk or logs.
