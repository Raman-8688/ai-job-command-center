# Phase 7 — Gmail & Email Integration

**Status:** COMPLETE  
**Local Branch:** `feature/phase-6-resume-tailoring` (extending Phase 7 modular monolith architecture)  
**Database Migration:** `V7__gmail_email_integration.sql`  
**Frontend Collaborator:** Bolt.new  

---

## 1. Overview & Objective

Phase 7 implements **Gmail / Email Integration & Intelligent Processing** for the AI Job Command Center. It connects candidates' email communications directly to their job search workflows, enabling:

1. **Secure Google OAuth 2.0 Integration:** Connect, status check, and disconnect Gmail accounts without exposing credentials to client applications.
2. **Inbox Synchronization & Ingestion:** Polling and ingesting job-related emails with idempotency (deduplication on external message IDs).
3. **AI & Heuristic Classification:** Automatically categorizing emails into job lifecycle stages (`APPLICATION_CONFIRMATION`, `INTERVIEW_INVITATION`, `ASSESSMENT`, `OFFER`, `REJECTION`, `NETWORKING_OUTREACH`, `STATUS_UPDATE`, `SPAM_OR_IRRELEVANT`, `OTHER`) with confidence scores and reasoning.
4. **Job Opportunity Extraction & Auto-Creation:** Extracting company names, job titles, requisition references, and action items from email text, and enabling one-click Job and UserJob creation directly from incoming correspondence.
5. **Bidirectional Job Association:** Linking and disassociating emails from canonical jobs for chronological interview tracking.

---

## 2. Architecture & Design Principles

### 2.1 Modular Monolith Isolation
All Phase 7 code resides in `com.jobcommandcenter.email`:
```
com.jobcommandcenter.email
├── api
│   ├── dto/
│   ├── EmailConnectionController.java
│   ├── EmailSyncController.java
│   ├── EmailController.java
│   └── JobEmailController.java
├── application
│   ├── EmailConnectionService.java
│   ├── EmailSyncService.java
│   └── EmailService.java
├── domain
│   ├── Email.java (Aggregate Root)
│   ├── EmailConnection.java (Aggregate Root)
│   ├── EmailClassification.java
│   ├── EmailProcessingStatus.java
│   ├── EmailProviderType.java
│   ├── SyncStatus.java
│   ├── EmailSearchCriteria.java
│   ├── EmailRepository.java
│   └── EmailConnectionRepository.java
└── infrastructure
    ├── client
    │   ├── GmailClient.java (SPI)
    │   ├── MockGmailClient.java
    │   ├── OAuthTokens.java
    │   ├── RemoteEmailMessage.java
    │   ├── RemoteEmailDetails.java
    │   └── GmailClientConfig.java
    └── persistence
        ├── EmailConnectionJpaEntity.java
        ├── EmailJpaEntity.java
        ├── SpringDataEmailConnectionRepository.java
        ├── SpringDataEmailRepository.java
        ├── EmailConnectionPersistenceAdapter.java
        └── EmailPersistenceAdapter.java
```

### 2.2 Zero Secrets Leakage
- `access_token` and `refresh_token` are stored in `email_connections` table.
- They are **never** returned in any REST API response or DTO (`EmailConnectionResponse` strictly excludes token fields).

### 2.3 Multi-Tenant Isolation
- Every query enforces `userId == SecurityUser.getId()`.
- Unauthorized access attempts return RFC 7807 `404 Not Found` rather than `403 Forbidden` to prevent user ID enumeration.

---

## 3. Database Schema (`V7__gmail_email_integration.sql`)

### 3.1 `email_connections` Table
Stores OAuth 2.0 tokens and synchronization metadata per user.

| Column | Type | Constraints / Description |
| :--- | :--- | :--- |
| `id` | `UUID` | Primary Key |
| `user_id` | `UUID` | Foreign Key $\rightarrow$ `users(id) ON DELETE CASCADE` |
| `provider` | `VARCHAR(50)` | `GMAIL`, `OUTLOOK`, `IMAP` |
| `email_address` | `VARCHAR(255)` | Candidate's email address |
| `is_connected` | `BOOLEAN` | Active connection flag |
| `access_token` | `TEXT` | OAuth access token (encrypted/internal) |
| `refresh_token` | `TEXT` | OAuth refresh token |
| `token_expires_at` | `TIMESTAMPTZ` | Access token expiration timestamp |
| `scopes` | `VARCHAR(500)` | Authorized OAuth scopes |
| `last_sync_at` | `TIMESTAMPTZ` | Timestamp of last successful sync |
| `last_history_id` | `VARCHAR(100)` | Gmail history ID cursor |
| `sync_status` | `VARCHAR(50)` | `IDLE`, `SYNCING`, `SUCCESS`, `FAILED` |
| `sync_error_message` | `TEXT` | Last sync failure reason |
| `emails_synced_count`| `INT` | Total messages ingested |
| `created_at` | `TIMESTAMPTZ` | Record creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | Record update timestamp |

*Constraint:* `uq_email_connections_user_provider (user_id, provider)`

### 3.2 `emails` Table
Stores synchronized, sanitized email messages and extracted intelligence.

| Column | Type | Constraints / Description |
| :--- | :--- | :--- |
| `id` | `UUID` | Primary Key |
| `user_id` | `UUID` | Foreign Key $\rightarrow$ `users(id) ON DELETE CASCADE` |
| `connection_id` | `UUID` | Foreign Key $\rightarrow$ `email_connections(id) ON DELETE SET NULL` |
| `external_message_id` | `VARCHAR(255)` | Remote Gmail message ID |
| `external_thread_id` | `VARCHAR(255)` | Remote Gmail thread ID |
| `sender` | `VARCHAR(255)` | Sender header |
| `recipient` | `VARCHAR(255)` | Recipient header |
| `subject` | `VARCHAR(500)` | Email subject line |
| `snippet` | `TEXT` | Short preview text |
| `body_plain` | `TEXT` | Sanitized plain text body |
| `body_html` | `TEXT` | Sanitized HTML body |
| `received_at` | `TIMESTAMPTZ` | Date email was received |
| `classification` | `VARCHAR(50)` | Lifecycle classification category |
| `classification_confidence` | `NUMERIC(4, 3)` | AI confidence score (0.000 to 1.000) |
| `classification_reason` | `TEXT` | Explanation for classification |
| `processing_status` | `VARCHAR(50)` | `UNPROCESSED`, `PROCESSED`, `IGNORED` |
| `extracted_company_name` | `VARCHAR(255)` | Extracted hiring company |
| `extracted_job_title` | `VARCHAR(255)` | Extracted role title |
| `extracted_external_id` | `VARCHAR(150)` | Extracted job req ID |
| `extracted_notes` | `TEXT` | Extracted action items / next steps |
| `associated_job_id` | `UUID` | Foreign Key $\rightarrow$ `jobs(id) ON DELETE SET NULL` |
| `created_at` | `TIMESTAMPTZ` | Record creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | Record update timestamp |

*Constraint:* `uq_emails_user_external_message_id (user_id, external_message_id)`

---

## 4. REST API Endpoints Summary

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/email/gmail/connect-url` | Generates OAuth consent URL |
| `POST` | `/api/email/gmail/callback` | Exchanges code for tokens and registers connection |
| `GET` | `/api/email/gmail/status` | Gets current connection state |
| `POST` | `/api/email/gmail/disconnect` | Revokes tokens and disconnects Gmail |
| `POST` | `/api/email/sync` | Manually triggers inbox synchronization |
| `GET` | `/api/email/sync/status` | Retrieves synchronization health and email count |
| `GET` | `/api/emails` | Paginated search & filtering of emails |
| `GET` | `/api/emails/{id}` | Fetches full email details and extracted data |
| `POST` | `/api/emails/{id}/classify` | Triggers AI reclassification |
| `PATCH` | `/api/emails/{id}/classification` | User manual override of classification |
| `PATCH` | `/api/emails/{id}/processing-status`| Updates status (`PROCESSED`, `IGNORED`, `UNPROCESSED`) |
| `POST` | `/api/emails/{id}/process` | Runs full AI job extraction |
| `POST` | `/api/emails/{id}/associate-job/{jobId}` | Links email to an existing Job |
| `DELETE` | `/api/emails/{id}/associate-job` | Unlinks email from its associated Job |
| `POST` | `/api/emails/{id}/create-job` | Creates new Job + UserJob directly from email |
| `DELETE` | `/api/emails/{id}` | Deletes local email record |
| `GET` | `/api/jobs/{jobId}/emails` | Lists all emails associated with a job |

---

## 5. Frontend Integration Guide for Bolt.new

1. **Authentication:**
   All API requests require the standard Bearer token header:
   `Authorization: Bearer <token>`
2. **CORS:**
   `http://localhost:4200` is configured with credentials allowed.
3. **Connect Flow:**
   * User clicks "Connect Gmail" $\rightarrow$ frontend calls `GET /api/email/gmail/connect-url`.
   * Frontend redirects candidate to `authorizationUrl`.
   * Google redirects back to `http://localhost:4200/email/callback?code=...&state=...`.
   * Frontend captures `code` and `state`, sends to `POST /api/email/gmail/callback`.
   * Frontend navigates to Email Hub.
4. **Email Hub UI Recommendations:**
   * **Badge Filters:** Filter buttons for `ALL`, `APPLICATION_CONFIRMATION`, `INTERVIEW_INVITATION`, `ASSESSMENT`, `OFFER`, `REJECTION`.
   * **Sync Button:** Triggers `POST /api/email/sync`, shows toast with count of new emails ingested.
   * **Action Bar:** "Create Job from Email" button calls `POST /api/emails/{id}/create-job`.
