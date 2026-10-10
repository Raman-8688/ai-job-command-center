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

---

## 7. Application Tracking & Lifecycle Endpoints (Phase 8)

### 7.1 Create Job Application
* **Method:** `POST`
* **Endpoint:** `/api/applications`
* **Auth:** Bearer JWT required
* **Success Status:** `201 Created`
* **Request Body:**
  ```json
  {
    "jobId": "UUID",
    "status": "DRAFT",
    "submissionSource": "LINKEDIN",
    "appliedAt": "2026-10-09T08:00:00Z",
    "externalReference": "REQ-12345",
    "notes": "Submitted application directly through portal",
    "nextFollowUpDate": "2026-10-16T08:00:00Z",
    "resumeId": "UUID",
    "tailoredResumeId": "UUID"
  }
  ```
* **Validation & Security:**
  - `jobId` must exist in canonical repository (otherwise `404 Not Found`).
  - Unique constraint: A candidate cannot have duplicate applications for the same job (returns `409 Conflict`).
  - If `resumeId` or `tailoredResumeId` is passed, candidate must own them (otherwise `404 Not Found`).
  - Emits immutable `CREATED` lifecycle event.

### 7.2 List Candidate Applications (Search & Paginated)
* **Method:** `GET`
* **Endpoint:** `/api/applications?status={status}&search={search}&page={page}&size={size}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `ApplicationPageResponse` with content list of `JobApplicationSummaryResponse`.

### 7.3 Get Dashboard Metrics Summary
* **Method:** `GET`
* **Endpoint:** `/api/applications/dashboard-summary`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "countsByStatus": {
      "DRAFT": 2,
      "APPLIED": 5,
      "SCREENING": 1,
      "INTERVIEW": 2,
      "OFFER": 0,
      "ACCEPTED": 0,
      "REJECTED": 1,
      "WITHDRAWN": 0,
      "ARCHIVED": 0
    },
    "activeApplicationsCount": 8,
    "followUpsDueCount": 2,
    "totalApplicationsCount": 11
  }
  ```

### 7.4 Get Application Detail
* **Method:** `GET`
* **Endpoint:** `/api/applications/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `JobApplicationResponse` with job details, resume linkages, and chronological timeline events.
* **Ownership Rule:** If application belongs to another user, returns `404 Not Found` (multi-tenant protection).

### 7.5 Update Application Metadata
* **Method:** `PUT`
* **Endpoint:** `/api/applications/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:**
  ```json
  {
    "submissionSource": "COMPANY_WEBSITE",
    "externalReference": "APP-987",
    "appliedAt": "2026-10-09T08:00:00Z",
    "nextFollowUpDate": "2026-10-17T08:00:00Z",
    "notes": "Updated contact info for recruiter"
  }
  ```

### 7.6 Transition Application Lifecycle Status
* **Method:** `POST`
* **Endpoint:** `/api/applications/{id}/transition`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:**
  ```json
  {
    "targetStatus": "INTERVIEW",
    "notes": "Passed technical phone screen, scheduled onsite panel",
    "eventSource": "USER",
    "occurredAt": "2026-10-09T08:00:00Z"
  }
  ```
* **State Machine Rules:**
  - Valid transitions enforced according to aggregate root lifecycle rules.
  - Invalid transitions reject with `400 Bad Request` and descriptive error details.
  - Automatically records immutable `JobApplicationEvent`.

### 7.7 Link Resume / Tailored Resume
* **Method:** `POST`
* **Endpoint:** `/api/applications/{id}/link-resume`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:**
  ```json
  {
    "resumeId": "UUID",
    "tailoredResumeId": "UUID",
    "notes": "Linked tailored resume variant v2"
  }
  ```

### 7.8 Get Timeline Audit Log
* **Method:** `GET`
* **Endpoint:** `/api/applications/{id}/events`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Ordered list of `JobApplicationEventResponse`.

### 7.9 Get AI Next-Step & Follow-Up Guidance
* **Method:** `GET`
* **Endpoint:** `/api/applications/{id}/guidance`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "recommendedAction": "Send courteous status inquiry to talent acquisition team",
    "rationale": "Over 7 days have elapsed since submission without a formal response.",
    "draftedFollowUpMessage": "Dear Recruiting Team,\n\nI hope this email finds you well...",
    "modelUsed": "MockDeterministicAIProvider",
    "generatedAt": "2026-10-09T08:00:00Z"
  }
  ```

### 7.10 Delete Application
* **Method:** `DELETE`
* **Endpoint:** `/api/applications/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `204 No Content`
* **Safety Constraint:** Only `DRAFT` or `WITHDRAWN` applications may be deleted. Active stages (`APPLIED`, `SCREENING`, `INTERVIEW`) reject deletion with `400 Bad Request`.

---

## 8. Interview Management & Preparation Endpoints (Phase 9)

### 8.1 Schedule New Interview
* **Method:** `POST`
* **Endpoint:** `/api/interviews`
* **Auth:** Bearer JWT required
* **Success Status:** `201 Created`
* **Request Body:**
  ```json
  {
    "jobId": "UUID",
    "applicationId": "UUID (optional)",
    "round": "TECHNICAL_SCREEN",
    "roundNumber": 1,
    "format": "VIDEO_CALL",
    "scheduledStartTime": "2026-10-15T14:00:00Z",
    "scheduledEndTime": "2026-10-15T15:00:00Z",
    "timeZone": "America/New_York",
    "meetingLink": "https://meet.google.com/xyz-abcd-efg",
    "location": "Google Meet",
    "interviewerNames": "Sarah Connor",
    "interviewerRoles": "Engineering Director",
    "notes": "Focus on distributed systems and high-throughput pipelines"
  }
  ```
* **Validation & Security:**
  - `jobId` must exist (otherwise `404 Not Found`).
  - If `applicationId` provided, candidate must own it (`404 Not Found`).
  - `scheduledEndTime` must be after `scheduledStartTime` (`400 Bad Request`).
  - Emits initial `SCHEDULED` timeline event.

### 8.2 List / Filter User Interviews
* **Method:** `GET`
* **Endpoint:** `/api/interviews?status={status}&round={round}&applicationId={appId}&jobId={jobId}&scheduledAfter={after}&scheduledBefore={before}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `List<InterviewSummaryResponse>`

### 8.3 Get Interview Dashboard Summary
* **Method:** `GET`
* **Endpoint:** `/api/interviews/dashboard-summary`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "totalInterviews": 4,
    "upcomingInterviews": 2,
    "completedInterviews": 2,
    "countByRound": {
      "TECHNICAL_SCREEN": 2,
      "SYSTEM_DESIGN": 1,
      "BEHAVIORAL_CULTURE": 1
    },
    "countByStatus": {
      "SCHEDULED": 2,
      "COMPLETED": 2
    },
    "nextUpcomingInterview": {
      "id": "UUID",
      "jobTitle": "Senior Backend Engineer",
      "companyName": "Datadog",
      "round": "TECHNICAL_SCREEN",
      "scheduledStartTime": "2026-10-15T14:00:00Z"
    },
    "overallReadinessScore": 88
  }
  ```
* **Readiness Calculation:** `overallReadinessScore` is computed strictly from actual persisted interview-preparation questions across all interviews owned by the candidate: `(reviewed questions / total questions) * 100`. If there are no interviews or no preparation questions exist yet, returns `0` (a transparent 0% indicating no preparation data, avoiding synthetic or invented scores).

### 8.4 Get Interview Details
* **Method:** `GET`
* **Endpoint:** `/api/interviews/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `InterviewResponse` with full details, events timeline, and practice questions.
* **Multi-Tenant Protection:** Returns `404 Not Found` if accessed by another candidate.

### 8.5 Update Interview Details
* **Method:** `PUT`
* **Endpoint:** `/api/interviews/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `UpdateInterviewDetailsRequest`

### 8.6 Reschedule Interview
* **Method:** `POST`
* **Endpoint:** `/api/interviews/{id}/reschedule`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:**
  ```json
  {
    "scheduledStartTime": "2026-10-17T15:00:00Z",
    "scheduledEndTime": "2026-10-17T16:00:00Z",
    "timeZone": "America/New_York",
    "reason": "Recruiter scheduling conflict"
  }
  ```
* **Constraint:** Cannot reschedule `COMPLETED` or `CANCELLED` interviews (`400 Bad Request`).

### 8.7 Update Interview Lifecycle Status
* **Method:** `POST`
* **Endpoint:** `/api/interviews/{id}/status`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:**
  ```json
  {
    "status": "COMPLETED",
    "outcome": "PASSED",
    "feedback": "Strong algorithmic performance, clear communication",
    "notes": "Advancing to next stage"
  }
  ```

### 8.8 Update Interview Outcome
* **Method:** `POST`
* **Endpoint:** `/api/interviews/{id}/outcome`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `UpdateInterviewOutcomeRequest` (`outcome`, `notes`).

### 8.9 Delete Interview
* **Method:** `DELETE`
* **Endpoint:** `/api/interviews/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `204 No Content`
* **Constraint:** `COMPLETED` interviews cannot be deleted (`400 Bad Request`).

### 8.10 Generate Grounded AI Interview Prep & Questions
* **Method:** `POST`
* **Endpoint:** `/api/interviews/{id}/ai-prep`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `InterviewPrepBundleResponse` containing grounded practice questions categorized into `TECH`, `SYSTEM_DESIGN`, `BEHAVIORAL`, `LEADERSHIP`, with STAR model answers and readiness scoring.

### 8.11 Get Interview Prep Bundle
* **Method:** `GET`
* **Endpoint:** `/api/interviews/{id}/prep`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `InterviewPrepBundleResponse`

### 8.12 Update Question Practice Notes & Reviewed Status
* **Method:** `PUT`
* **Endpoint:** `/api/interviews/{id}/prep/{prepId}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:**
  ```json
  {
    "userAnswerNotes": "Situation: System crashed under peak load. Action: Scaled Redis cluster. Result: Latency reduced 50%.",
    "isReviewed": true
  }
  ```

### 8.13 Add Custom Practice Question
* **Method:** `POST`
* **Endpoint:** `/api/interviews/{id}/prep`
* **Auth:** Bearer JWT required
* **Success Status:** `201 Created`
* **Request Body:** `SavePrepQuestionRequest`


---

## 9. Online Assessments & Company Technical Dossiers (Phase 10)

### 9.1 Create Online Assessment
* **Method:** `POST`
* **Endpoint:** `/api/assessments`
* **Auth:** Bearer JWT required
* **Success Status:** `201 Created`
* **Request Body:**
  ```json
  {
    "jobId": "3f3984a2-1731-4df8-9186-353f64998965",
    "applicationId": "Optional UUID",
    "interviewId": "Optional UUID",
    "platform": "HACKERRANK",
    "customPlatform": null,
    "title": "HackerRank SWE Assessment",
    "assessmentUrl": "https://hackerrank.com/tests/12345",
    "status": "RECEIVED",
    "result": "PENDING",
    "invitedAt": "2026-10-10T12:00:00Z",
    "deadlineAt": "2026-10-15T12:00:00Z",
    "durationMinutes": 90,
    "proctored": true,
    "notes": "Covers data structures and concurrency"
  }
  ```
* **Response Body:** `OnlineAssessmentResponse`

### 9.2 List Online Assessments with Filters
* **Method:** `GET`
* **Endpoint:** `/api/assessments`
* **Query Parameters:**
  * `status` (optional): Filter by `OnlineAssessmentStatus` (`RECEIVED`, `IN_PROGRESS`, `SUBMITTED`, `EXPIRED`, `ABANDONED`)
  * `platform` (optional): Filter by `AssessmentPlatform` (`HACKERRANK`, `CODE_SIGNAL`, `LEETCODE`, `CUSTOM`, etc.)
  * `jobId` (optional): Filter by associated `UUID` job
  * `applicationId` (optional): Filter by associated `UUID` application
  * `expiringWithinHours` (optional): Filter assessments expiring within next N hours
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Array of `OnlineAssessmentSummaryResponse`

### 9.3 Get Assessment Dashboard Summary
* **Method:** `GET`
* **Endpoint:** `/api/assessments/dashboard-summary`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:**
  ```json
  {
    "totalAssessments": 12,
    "pendingCount": 4,
    "inProgressCount": 2,
    "submittedCount": 5,
    "passedCount": 4,
    "failedCount": 1,
    "averageScore": 86.5,
    "upcomingDeadlines": [ ...OnlineAssessmentSummaryResponse... ]
  }
  ```

### 9.4 Get Online Assessment by ID
* **Method:** `GET`
* **Endpoint:** `/api/assessments/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `OnlineAssessmentResponse` (includes active study checklist items)

### 9.5 Update Online Assessment Metadata
* **Method:** `PUT`
* **Endpoint:** `/api/assessments/{id}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `UpdateAssessmentRequest` (`title`, `assessmentUrl`, `deadlineAt`, `durationMinutes`, `proctored`, `notes`, `expectedVersion`)

### 9.6 Start Online Assessment
* **Method:** `POST`
* **Endpoint:** `/api/assessments/{id}/start`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `StartAssessmentRequest` (`startedAt`, `notes`, `expectedVersion`)

### 9.7 Submit Online Assessment
* **Method:** `POST`
* **Endpoint:** `/api/assessments/{id}/submit`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `SubmitAssessmentRequest` (`submittedAt`, `notes`, `expectedVersion`)

### 9.8 Record Online Assessment Result
* **Method:** `POST`
* **Endpoint:** `/api/assessments/{id}/result`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:**
  ```json
  {
    "result": "PASSED",
    "score": 92.5,
    "maxScore": 100.0,
    "notes": "Passed all algorithmic test cases",
    "expectedVersion": 2
  }
  ```

### 9.9 Extend Online Assessment Deadline
* **Method:** `POST`
* **Endpoint:** `/api/assessments/{id}/extend`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `ExtendAssessmentDeadlineRequest` (`newDeadlineAt`, `reason`, `expectedVersion`)

### 9.10 Abandon Online Assessment
* **Method:** `POST`
* **Endpoint:** `/api/assessments/{id}/abandon`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `AbandonAssessmentRequest` (`reason`, `expectedVersion`)

### 9.11 Expire Online Assessment
* **Method:** `POST`
* **Endpoint:** `/api/assessments/{id}/expire`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `ExpireAssessmentRequest` (`notes`, `expectedVersion`)

### 9.12 Get Assessment Event History
* **Method:** `GET`
* **Endpoint:** `/api/assessments/{id}/events`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Array of `OnlineAssessmentEventResponse` ordered chronologically by `occurredAt`.

### 9.13 Get Assessment Study Checklist
* **Method:** `GET`
* **Endpoint:** `/api/assessments/{id}/checklist`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `AssessmentChecklistBundleResponse` (total, completed, completionPercentage, items)

### 9.14 Toggle Assessment Study Checklist Item Completion
* **Method:** `PATCH`
* **Endpoint:** `/api/assessments/{id}/checklist/{itemId}`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `ToggleChecklistItemRequest` (`completed`, `expectedVersion`)

### 9.15 Generate Grounded AI Assessment Briefing
* **Method:** `POST`
* **Endpoint:** `/api/assessments/{id}/briefing`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `GenerateBriefingRequest` (`forceRefresh`, `expectedVersion`)
* **Response Body:** `AssessmentBriefingResponse`

### 9.16 Get Company Technical Dossier
* **Method:** `GET`
* **Endpoint:** `/api/intel/jobs/{jobId}/company-dossier`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `CompanyDossierResponse`

### 9.17 Generate Grounded AI Company Technical Dossier
* **Method:** `POST`
* **Endpoint:** `/api/intel/jobs/{jobId}/company-dossier`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Request Body:** `GenerateCompanyDossierRequest` (`forceRefresh`)
* **Response Body:** `CompanyDossierResponse`

---

## 10. Analytics & Career Strategy Insights

### 10.1 Get Analytics Overview
* **Method:** `GET`
* **Endpoint:** `/api/analytics/overview`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `AnalyticsOverview`
  ```json
  {
    "totalApplications": 42,
    "activeApplications": 12,
    "interviewsScheduled": 5,
    "offersReceived": 2,
    "rejections": 18,
    "overallConversionRate": 4.76,
    "averageDaysToFirstResponse": 6.5,
    "activeAssessmentCount": 3
  }
  ```

### 10.2 Get Funnel Metrics
* **Method:** `GET`
* **Endpoint:** `/api/analytics/funnel`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `FunnelMetrics`
  ```json
  {
    "stageConversions": [
      {
        "fromStage": "SAVED",
        "toStage": "APPLIED",
        "enteredCount": 50,
        "convertedCount": 42,
        "conversionRate": 84.0,
        "averageDaysInStage": 2.1
      },
      {
        "fromStage": "APPLIED",
        "toStage": "SCREENING",
        "enteredCount": 42,
        "convertedCount": 15,
        "conversionRate": 35.71,
        "averageDaysInStage": 5.4
      }
    ],
    "totalInFunnel": 50,
    "reachedInterviewCount": 8,
    "reachedOfferCount": 2
  }
  ```

### 10.3 Get Source Effectiveness
* **Method:** `GET`
* **Endpoint:** `/api/analytics/sources`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Array of `SourceEffectiveness`
  ```json
  [
    {
      "source": "LINKEDIN",
      "applicationCount": 25,
      "interviewCount": 4,
      "offerCount": 1,
      "interviewConversionRate": 16.0,
      "offerConversionRate": 4.0
    },
    {
      "source": "REFERRAL",
      "applicationCount": 5,
      "interviewCount": 3,
      "offerCount": 1,
      "interviewConversionRate": 60.0,
      "offerConversionRate": 20.0
    }
  ]
  ```

### 10.4 Get Skill Gap Metrics
* **Method:** `GET`
* **Endpoint:** `/api/analytics/skills`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** Array of `SkillGapMetric`
  ```json
  [
    {
      "skillName": "Kubernetes",
      "frequencyInTargetJobs": 12,
      "frequencyInCandidateProfile": 0,
      "candidateHasSkill": false,
      "gapSeverity": "HIGH"
    },
    {
      "skillName": "Java",
      "frequencyInTargetJobs": 20,
      "frequencyInCandidateProfile": 1,
      "candidateHasSkill": true,
      "gapSeverity": "NONE"
    }
  ]
  ```

### 10.5 Generate Grounded AI Career Strategy Insights
* **Method:** `POST`
* **Endpoint:** `/api/analytics/insights`
* **Auth:** Bearer JWT required
* **Success Status:** `200 OK`
* **Response Body:** `AIAnalyticsAdvisorResponse`
  ```json
  {
    "summary": "Your job search shows strong top-of-funnel activity across 42 applications with 2 offers...",
    "bottlenecks": [
      {
        "category": "CONVERSION",
        "metricName": "APPLIED_TO_SCREENING",
        "observedValue": 22.5,
        "targetBenchmark": 30.0,
        "impactLevel": "HIGH",
        "description": "Screening conversion rate is below target.",
        "recommendedRemedy": "Tailor resumes more aggressively to match job keywords."
      }
    ],
    "recommendations": [
      {
        "priority": "HIGH",
        "title": "Focus on high-performing referral channels",
        "actionItem": "Allocate more effort to referrals which yield a 60.0% interview rate.",
        "expectedImpact": "Increase overall interview volume by 2x."
      }
    ],
    "strengths": [
      "Outstanding referral interview conversion rate of 60.0%"
    ],
    "dataLimitations": [
      "No interview outcome history recorded yet; interview metrics may be incomplete."
    ],
    "generatedAt": "2026-10-10T10:00:00Z"
  }
  ```
