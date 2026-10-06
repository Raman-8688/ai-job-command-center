# Gmail Integration Architecture

## 1. Overview
The Gmail integration synchronizes and analyzes job-related communications, detects recruiter outreach, updates application state machines, and drafts replies.

## 2. Interaction Lifecycle
1. **Authorize:** User initiates Google OAuth 2.0 consent via browser.
2. **Synchronize (Level 1):** Scheduled background worker fetches incremental message updates using `historyId`.
3. **Classify (Level 1):** Unprocessed messages are evaluated by `EmailClassificationService`.
4. **Link Application (Level 1):** Message is correlated with active application records via company name, recruiter email, or subject thread references.
5. **Draft Reply (Level 2):** For messages requiring action, AI generates a draft reply saved to Gmail drafts or local approval queue.
6. **Send (Level 3):** User explicitly clicks "Send" to trigger outgoing message dispatch via Gmail REST API.

## 3. Data Invariants
- Passwords are never requested or stored.
- Email contents are sanitized to remove tracker pixels before local caching.
- Synchronized emails are strictly retained in the local database for personal search and auditing.
