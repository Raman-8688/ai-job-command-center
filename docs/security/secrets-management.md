# Secrets & Credential Management

## 1. Zero Credential Commit Invariant
- Real API keys, passwords, client secrets, and certificates must **NEVER** be committed to Git.
- `.gitignore` strictly rejects `.env`, `*.pem`, `*.key`, `*.jks`, and Google credential JSON files.
- Continuous Integration (`.github/workflows/ci.yml`) runs pre-merge pattern scanners to block credential leakage.

## 2. Environment Ingestion
Secrets are injected into the runtime exclusively through environment variables (`.env` during local development, environment containers in production).
- Database credentials: `DB_USERNAME`, `DB_PASSWORD`
- Google OAuth: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- AI Providers: `AI_API_KEY`
- Crypto Vault: `JWT_SECRET`, `TOKEN_ENCRYPTION_KEY`
