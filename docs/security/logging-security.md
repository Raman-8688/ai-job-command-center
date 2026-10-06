# Logging Security & Sanitization

## 1. Zero-PII & Zero-Secret Logging Invariant
Application logs must never become a source of credential leakage or privacy violations.

## 2. Redaction Filters
The Logback configuration enforces automated regex masks to scrub sensitive patterns before outputting log entries:
- `Bearer [A-Za-z0-9\-._~+/]+=*` -> `Bearer [REDACTED]`
- `client_secret=[^&]+` -> `client_secret=[REDACTED]`
- `password=[^&]+` -> `password=[REDACTED]`
- `email_body=[^&]+` -> `email_body=[SANITIZED_SNIPPET]`

Full raw emails and unparsed resumes are logged only at `DEBUG` or `TRACE` level in dedicated developer sandbox runs and never in standard `INFO` output.
