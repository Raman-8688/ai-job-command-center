# Security Architecture Specification

## 1. Security Invariants
Because this platform handles sensitive personal career records, confidential recruiter emails, and API access tokens, security and privacy are paramount.

```
+-------------------------------------------------------------------------+
|                           SECURITY DEFENSE LAYERS                       |
|                                                                         |
|  [ Layer 1: Transport Security ]       HTTPS / TLS 1.3 Strict           |
|  [ Layer 2: Client Origin ]           Strict CORS (Localhost UI Only)   |
|  [ Layer 3: Authentication ]          Spring Security 6 + JWT           |
|  [ Layer 4: Token Encryption ]        AES-256-GCM Token Vault           |
|  [ Layer 5: Data Sanitation ]         Zero PII / Credential Logs        |
|  [ Layer 6: Human Gates ]             Mandatory Approval for L2/L3      |
+-------------------------------------------------------------------------+
```

## 2. Authentication & Authorization
- Spring Security 6 provides authentication and request authorization filters.
- Personal session uses stateless Bearer JWTs signed with HMAC-SHA256 (256-bit key from `.env`).
- CSRF protection is enforced for state-changing browser sessions, or mitigated by stateless Bearer token authentication.
