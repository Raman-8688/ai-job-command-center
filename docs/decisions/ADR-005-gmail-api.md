# ADR-005: Email Integration — Official Gmail REST API via OAuth 2.0

## Status
Accepted

## Context
Job applications generate critical email updates (application acknowledgments, coding assessments, recruiter inquiries, interview schedules, rejections). To organize these events, the system must read and organize relevant messages and generate drafts. Using legacy protocols like IMAP/SMTP requires storing raw Google account passwords or less-secure app passwords, violating modern security hygiene and lacking granular permission controls.

## Decision
We integrate with the official **Google Gmail REST API** using **OAuth 2.0 Authorization Code Flow** with PKCE:
- Scopes will follow least-privilege principles:
  - `https://www.googleapis.com/auth/gmail.readonly` (reading messages)
  - `https://www.googleapis.com/auth/gmail.compose` (creating drafts)
  - `https://www.googleapis.com/auth/gmail.send` (sending user-approved emails)
- No user passwords will ever be stored. OAuth refresh tokens will be stored encrypted at rest using AES-256-GCM.

## Alternatives Considered
- **IMAP/SMTP with App Passwords:** Rejected due to lack of granular scope restrictions, high risk of credential exposure, and potential Google deprecation.
- **Third-Party Email Aggregation APIs (e.g., Nylas):** Rejected to preserve personal privacy and avoid sending personal emails through third-party multi-tenant SaaS vendors.

## Consequences
- **Positive:** Complies with modern Google security standards, enables granular scopes, supports direct draft creation in the user's native Gmail client.
- **Negative:** Requires setting up a Google Cloud Console Project and managing OAuth consent screens and token refresh cycles.
