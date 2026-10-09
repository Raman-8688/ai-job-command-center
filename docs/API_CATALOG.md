# AI Job Command Center — Complete REST API Catalog

> **Authoritative API Contract for Frontend Integration (Bolt.new & Angular)**  
> **Base URL:** `http://localhost:8080`  
> **Format:** `application/json`  
> **Error Standard:** RFC 7807 Problem Details  
> **Authentication Standard:** Bearer JWT in `Authorization: Bearer <token>` header

---

## 1. Authentication & Security Contract

### Standard Headers
All authenticated requests must include:
```http
Authorization: Bearer <JWT_TOKEN>
```
Cross-Origin Resource Sharing (CORS) is enabled for `http://localhost:4200` with credentials supported.

### Standard RFC 7807 Error Response
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Email not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "instance": "/api/emails/550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-10-08T17:30:00Z",
  "correlationId": "corr-1728408600000"
}
```

---

## 2. Authentication Endpoints

### 2.1 Register Candidate
* **Method:** `POST`
* **Endpoint:** `/api/auth/register`
* **Auth:** Public
* **Request Body:**
  ```json
  {
    "email": "candidate@example.com",
    "password": "SecurePassword123!",
    "firstName": "Alex",
    "lastName": "Rivera",
    "displayName": "Alex Rivera"
  }
  ```
* **Success Status:** `201 Created`
* **Response Body:**
  ```json
  {
    "id": "UUID",
    "email": "candidate@example.com",
    "firstName": "Alex",
    "lastName": "Rivera",
    "displayName": "Alex Rivera",
    "role": "USER",
    "accountStatus": "ACTIVE",
    "createdAt": "2026-10-08T10:00:00Z"
  }
  ```

### 2.2 Login Candidate
* **Method:** `POST`
* **Endpoint:** `/api/auth/login`
* **Auth:** Public
* **Request Body:**
  ```json
  {
    "email": "candidate@example.com",
    "password": "SecurePassword123!"
  }
  ```
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": "UUID",
      "email": "candidate@example.com",
      "displayName": "Alex Rivera",
      "role": "USER"
    }
  }
  ```

### 2.3 Current Authenticated User Profile
* **Method:** `GET`
* **Endpoint:** `/api/users/me`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`

---

## 3. Gmail & Email Integration Endpoints (Phase 7)

### 3.1 Get Gmail OAuth Connect URL
* **Method:** `GET`
* **Endpoint:** `/api/email/gmail/connect-url`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth?response_type=code&...",
    "state": "550e8400-e29b-41d4-a716-446655440000"
  }
  ```

### 3.2 Handle Gmail OAuth Callback
* **Method:** `POST`
* **Endpoint:** `/api/email/gmail/callback`
* **Auth:** Bearer JWT required
* **Request Body:**
  ```json
  {
    "code": "4/0AeanS0Y...",
    "state": "550e8400-e29b-41d4-a716-446655440000"
  }
  ```
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "id": "UUID",
    "provider": "GMAIL",
    "emailAddress": "candidate.alex@gmail.com",
    "connected": true,
    "lastSyncAt": null,
    "syncStatus": "IDLE",
    "syncErrorMessage": null,
    "emailsSyncedCount": 0,
    "createdAt": "2026-10-08T18:00:00Z",
    "updatedAt": "2026-10-08T18:00:00Z"
  }
  ```

### 3.3 Get Gmail Connection Status
* **Method:** `GET`
* **Endpoint:** `/api/email/gmail/status`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "id": "UUID",
    "provider": "GMAIL",
    "emailAddress": "candidate.alex@gmail.com",
    "connected": true,
    "lastSyncAt": "2026-10-08T18:05:00Z",
    "syncStatus": "SUCCESS",
    "syncErrorMessage": null,
    "emailsSyncedCount": 15,
    "createdAt": "2026-10-08T18:00:00Z",
    "updatedAt": "2026-10-08T18:05:00Z"
  }
  ```
* **Error Behavior:** 404 if user has never connected Gmail.

### 3.4 Disconnect Gmail
* **Method:** `POST`
* **Endpoint:** `/api/email/gmail/disconnect`
* **Auth:** Bearer JWT required
* **Success Status:** `204 No Content`
* **Notes:** Invalidates stored tokens and marks `connected = false`.

### 3.5 Trigger Email Synchronization
* **Method:** `POST`
* **Endpoint:** `/api/email/sync`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "messagesFetched": 25,
    "newMessagesSynced": 5,
    "syncStatus": "SUCCESS",
    "syncedAt": "2026-10-08T18:10:00Z",
    "message": "Successfully synchronized 5 new emails"
  }
  ```

### 3.6 Get Sync Status & Metrics
* **Method:** `GET`
* **Endpoint:** `/api/email/sync/status`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "connected": true,
    "provider": "GMAIL",
    "emailAddress": "candidate.alex@gmail.com",
    "syncStatus": "SUCCESS",
    "lastSyncAt": "2026-10-08T18:10:00Z",
    "errorMessage": null,
    "totalEmailsCount": 38
  }
  ```

---

## 4. Email Management Endpoints (Phase 7)

### 4.1 List and Search Emails
* **Method:** `GET`
* **Endpoint:** `/api/emails`
* **Auth:** Bearer JWT required
* **Query Parameters:**
  * `page` *(optional, integer, default 0)*
  * `size` *(optional, integer, default 20)*
  * `classification` *(optional, enum: `APPLICATION_CONFIRMATION`, `INTERVIEW_INVITATION`, `ASSESSMENT`, `REJECTION`, `OFFER`, `NETWORKING_OUTREACH`, `STATUS_UPDATE`, `SPAM_OR_IRRELEVANT`, `OTHER`, `UNCLASSIFIED`)*
  * `processingStatus` *(optional, enum: `UNPROCESSED`, `PROCESSED`, `IGNORED`)*
  * `associatedJobId` *(optional, UUID)*
  * `search` *(optional, string to match subject, sender, or company)*
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "content": [
      {
        "id": "UUID",
        "externalMessageId": "msg-mock-102",
        "sender": "Amazon Recruiting <recruiting@amazon.com>",
        "recipient": "candidate.alex@gmail.com",
        "subject": "Amazon Interview Invitation: Software Development Engineer II",
        "snippet": "Congratulations! We would like to schedule a 60-minute technical phone screen...",
        "receivedAt": "2026-10-07T14:30:00Z",
        "classification": "INTERVIEW_INVITATION",
        "classificationConfidence": 0.950,
        "processingStatus": "UNPROCESSED",
        "extractedCompanyName": "Amazon",
        "extractedJobTitle": "Software Development Engineer II",
        "associatedJobId": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
  ```

### 4.2 Get Email Details
* **Method:** `GET`
* **Endpoint:** `/api/emails/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "id": "UUID",
    "connectionId": "UUID",
    "externalMessageId": "msg-mock-102",
    "externalThreadId": "thread-mock-102",
    "sender": "Amazon Recruiting <recruiting@amazon.com>",
    "recipient": "candidate.alex@gmail.com",
    "subject": "Amazon Interview Invitation: Software Development Engineer II",
    "snippet": "Congratulations! We would like to schedule...",
    "bodyPlain": "Hello Alex,\n\nWe were impressed by your profile...",
    "bodyHtml": "<p>Hello Alex,</p>...",
    "receivedAt": "2026-10-07T14:30:00Z",
    "classification": "INTERVIEW_INVITATION",
    "classificationConfidence": 0.950,
    "classificationReason": "Matched direct interview scheduling invitation in communication.",
    "processingStatus": "UNPROCESSED",
    "extractedCompanyName": "Amazon",
    "extractedJobTitle": "Software Development Engineer II",
    "extractedExternalId": null,
    "extractedNotes": "Extracted from email: Amazon Interview Invitation...",
    "associatedJobId": null,
    "createdAt": "2026-10-08T18:10:00Z",
    "updatedAt": "2026-10-08T18:10:00Z"
  }
  ```
* **Ownership Rule:** If email belongs to another user, returns `404 Not Found`.

### 4.3 Trigger AI Reclassification
* **Method:** `POST`
* **Endpoint:** `/api/emails/{id}/classify`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Returns updated `EmailDetailResponse`.

### 4.4 Override Classification
* **Method:** `PATCH`
* **Endpoint:** `/api/emails/{id}/classification`
* **Auth:** Bearer JWT required
* **Request Body:**
  ```json
  {
    "classification": "INTERVIEW_INVITATION"
  }
  ```
* **Success Status:** `200 OK`
* **Response Body:** Returns updated `EmailDetailResponse`.

### 4.5 Update Workflow Processing Status
* **Method:** `PATCH`
* **Endpoint:** `/api/emails/{id}/processing-status`
* **Auth:** Bearer JWT required
* **Request Body:**
  ```json
  {
    "processingStatus": "PROCESSED"
  }
  ```
* **Success Status:** `200 OK`
* **Response Body:** Returns updated `EmailDetailResponse`.

### 4.6 Process Email (Run AI Extraction)
* **Method:** `POST`
* **Endpoint:** `/api/emails/{id}/process`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Returns updated `EmailDetailResponse` with extracted company, role title, external ID, and sets `processingStatus = "PROCESSED"`.

### 4.7 Associate Email with Job
* **Method:** `POST`
* **Endpoint:** `/api/emails/{id}/associate-job/{jobId}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Returns updated `EmailDetailResponse` with `associatedJobId = jobId`.

### 4.8 Disassociate Email from Job
* **Method:** `DELETE`
* **Endpoint:** `/api/emails/{id}/associate-job`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Returns updated `EmailDetailResponse` with `associatedJobId = null`.

### 4.9 Create Job Directly from Email
* **Method:** `POST`
* **Endpoint:** `/api/emails/{id}/create-job`
* **Auth:** Bearer JWT required
* **Success Status:** `201 Created`
* **Response Body:**
  ```json
  {
    "id": "UUID",
    "externalJobId": null,
    "title": "Software Development Engineer II",
    "companyName": "Amazon",
    "companyWebsite": null,
    "jobUrl": null,
    "description": "Hello Alex,\n\nWe were impressed by your profile...",
    "location": "Remote / Unspecified",
    "workMode": "UNKNOWN",
    "employmentType": "FULL_TIME",
    "source": "EMAIL",
    "status": "ACTIVE",
    "skills": []
  }
  ```
* **Side Effects:** Automatically creates canonical `Job`, creates `UserJob` tracking relation, and sets `email.associatedJobId = job.id`.

### 4.10 Delete Email
* **Method:** `DELETE`
* **Endpoint:** `/api/emails/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `204 No Content`

### 4.11 Get Emails for a Specific Job
* **Method:** `GET`
* **Endpoint:** `/api/jobs/{jobId}/emails`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `List<EmailSummaryResponse>`

---

## 5. Resume Tailoring Endpoints (Phase 6)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/resumes/{resumeId}/tailor/{jobId}` | Creates a versioned tailored resume draft (`v1`, `v2`, ...) with AI suggestions |
| `GET` | `/api/resumes/{resumeId}/tailored` | Lists all tailored versions generated from the master resume |
| `GET` | `/api/jobs/{jobId}/tailored-resumes` | Lists all tailored resume drafts targeting a specific job |
| `GET` | `/api/tailored-resumes/{id}` | Fetches full tailored resume details, keyword coverage, and section suggestions |
| `PUT` | `/api/tailored-resumes/{id}` | Updates draft content and notes; recalculates keyword coverage score |
| `PATCH` | `/api/tailored-resumes/{id}/status` | Updates review status (`DRAFT`, `REVIEW`, `APPROVED`, `REJECTED`) |
| `POST` | `/api/tailored-resumes/{id}/suggestions/{suggestionId}/apply` | Applies a suggestion diff directly into draft content |
| `DELETE` | `/api/tailored-resumes/{id}` | Deletes tailored resume without affecting the immutable master resume |

---

## 6. Job Discovery & Matching Endpoints (Phase 3 & 4)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/jobs` | Creates/ingests a new job posting |
| `GET` | `/api/jobs` | Lists/filters jobs with pagination |
| `GET` | `/api/jobs/{id}` | Gets canonical job details |
| `GET` | `/api/jobs/{id}/match` | Calculates deterministic match score against candidate's verified skills |
| `POST` | `/api/jobs/{id}/ai-analysis` | Triggers AI deep analysis (responsibilities, tech stack, red flags) |
| `GET` | `/api/jobs/{id}/ai-analysis` | Retrieves saved AI job analysis |
