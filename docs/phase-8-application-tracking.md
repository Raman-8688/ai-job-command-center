# Phase 8 — Application Tracking & Lifecycle Management

**Status:** COMPLETE  
**Local Branch:** `feature/phase-8-application-tracking`  
**Database Migration:** `V8__application_tracking.sql`  
**Automated Tests:** 109 passing tests (0 failures, 0 errors)  
**Frontend Build:** `ng build` clean production bundle  

---

## 1. Overview & Objective

Phase 8 implements **Job Application Tracking & Lifecycle Management** for the AI Job Command Center modular monolith. It transitions the application from passive job discovery and email viewing into an active operational cockpit where candidates track and manage their applications from creation to offer acceptance or archival.

Key Capabilities Delivered:
1. **Dedicated Application Lifecycle State Machine:** Full aggregate root `JobApplication` enforcing valid transition paths across 10 distinct statuses (`DRAFT`, `APPLIED`, `SCREENING`, `ASSESSMENT`, `INTERVIEW`, `OFFER`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`, `ARCHIVED`).
2. **Immutable Chronological Audit Trail:** Every status change, note entry, resume linkage, and stage shift is recorded as an immutable `JobApplicationEvent` with timestamp and source attribution.
3. **Master & Tailored Resume Linkages:** Direct reference linkage to canonical `Resume` (Phase 5) and job-specific `TailoredResume` (Phase 6) with strict candidate ownership verification.
4. **Unique Job-Application Constraint:** Enforces one active application record per candidate per job (`uq_job_applications_user_job`), preventing duplicate or conflicting entries.
5. **Dashboard Funnel Metrics:** Real-time analytics summarizing active applications in the funnel, late-stage offers/interviews, and follow-ups requiring candidate attention.
6. **Advisory AI Next-Step Guidance:** Extends `AIProvider` SPI with `generateApplicationGuidance` to provide deterministic recommended next steps, status inquiry rationales, and customized follow-up draft emails.
7. **Angular 18 Management Interface:** Responsive management UI (`ApplicationTrackerComponent`) featuring funnel cards, search/filter toolbar, candidate-action drawers, and one-click draft copying.

---

## 2. Architecture & Domain Model

### 2.1 Package Organization
All Phase 8 code is organized in the modular monolith package `com.jobcommandcenter.application`:
```
com.jobcommandcenter.application
├── api
│   ├── dto/
│   │   ├── CreateApplicationRequest.java
│   │   ├── UpdateApplicationRequest.java
│   │   ├── TransitionStatusRequest.java
│   │   ├── LinkResumeRequest.java
│   │   ├── JobApplicationResponse.java
│   │   ├── JobApplicationSummaryResponse.java
│   │   ├── JobApplicationEventResponse.java
│   │   ├── ApplicationPageResponse.java
│   │   ├── ApplicationDashboardSummaryResponse.java
│   │   └── ApplicationGuidanceResponse.java
│   └── JobApplicationController.java
├── application
│   └── JobApplicationService.java
├── domain
│   ├── ApplicationEventType.java
│   ├── ApplicationSource.java
│   ├── ApplicationStatus.java
│   ├── EventSource.java
│   ├── InvalidStateTransitionException.java
│   ├── JobApplication.java (Aggregate Root)
│   ├── JobApplicationEvent.java (Immutable Entity)
│   ├── JobApplicationRepository.java (Domain Port)
│   └── JobApplicationSearchCriteria.java
└── infrastructure
    ├── JobApplicationJpaEntity.java
    ├── JobApplicationEventJpaEntity.java
    ├── SpringDataJobApplicationRepository.java
    ├── SpringDataJobApplicationEventRepository.java
    └── JobApplicationPersistenceAdapter.java
```

### 2.2 Lifecycle State Machine Transitions
Status transitions follow strict domain invariant rules:

| Origin Status | Allowed Target Statuses | Disallowed / Terminal Paths |
|---|---|---|
| `DRAFT` | `APPLIED`, `WITHDRAWN`, `ARCHIVED` | Cannot jump directly to `SCREENING`, `INTERVIEW`, `OFFER`, `ACCEPTED` |
| `APPLIED` | `SCREENING`, `ASSESSMENT`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`, `ARCHIVED` | Cannot regress to `DRAFT` |
| `SCREENING` | `ASSESSMENT`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`, `ARCHIVED` | Cannot regress to `DRAFT` or `APPLIED` |
| `ASSESSMENT` | `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`, `ARCHIVED` | Cannot regress to `DRAFT` or `SCREENING` |
| `INTERVIEW` | `ASSESSMENT`, `OFFER`, `REJECTED`, `WITHDRAWN`, `ARCHIVED` | Cannot regress to `DRAFT` or `APPLIED` |
| `OFFER` | `ACCEPTED`, `REJECTED`, `WITHDRAWN`, `ARCHIVED` | Cannot jump back to `INTERVIEW` |
| `ACCEPTED` | `ARCHIVED`, `WITHDRAWN` | Terminal conversion state |
| `REJECTED` | `DRAFT`, `APPLIED`, `SCREENING` | Allowed re-application (records `REOPENED` event) |
| `WITHDRAWN` | `DRAFT`, `APPLIED`, `SCREENING` | Allowed re-application (records `REOPENED` event) |
| `ARCHIVED` | `DRAFT`, `APPLIED`, `SCREENING` | Allowed re-activation |

Attempting an illegal transition triggers `InvalidStateTransitionException` mapped by `GlobalExceptionHandler` to HTTP `400 Bad Request`.

---

## 3. Database Schema Migration (V8)

Flyway migration `V8__application_tracking.sql` creates:
- `job_applications`:
  - `id UUID PRIMARY KEY`
  - `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
  - `job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE`
  - `resume_id UUID REFERENCES resumes(id) ON DELETE SET NULL`
  - `tailored_resume_id UUID REFERENCES tailored_resumes(id) ON DELETE SET NULL`
  - `status VARCHAR(50) NOT NULL DEFAULT 'DRAFT'`
  - `applied_at TIMESTAMPTZ`
  - `submission_source VARCHAR(50) NOT NULL DEFAULT 'MANUAL'`
  - `external_reference VARCHAR(150)`
  - `next_follow_up_date TIMESTAMPTZ`
  - `notes TEXT`
  - `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `version BIGINT NOT NULL DEFAULT 0`
  - `CONSTRAINT uq_job_applications_user_job UNIQUE (user_id, job_id)`
- `job_application_events`:
  - `id UUID PRIMARY KEY`
  - `application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE`
  - `previous_status VARCHAR(50)`
  - `new_status VARCHAR(50) NOT NULL`
  - `event_type VARCHAR(50) NOT NULL`
  - `notes TEXT`
  - `source VARCHAR(50) NOT NULL DEFAULT 'USER'`
  - `occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

Indexes:
- `idx_job_applications_user`, `idx_job_applications_job`, `idx_job_applications_status`, `idx_job_applications_applied_at`, `idx_job_applications_follow_up`
- `idx_job_app_events_app_id`, `idx_job_app_events_occurred_at`

---

## 4. Multi-Tenant Security & Deletion Rules

1. **Multi-Tenant Isolation:**
   Every API endpoint requires a valid JWT Bearer token. Application lookup verifies `application.getUserId().equals(securityUser.getId())`. Accessing another user's application returns `404 Not Found` (rather than `403`) to eliminate ID-probing enumeration vectors.
2. **Related Resource Ownership:**
   When linking a master resume (`resumeId`) or tailored variant (`tailoredResumeId`), the service verifies ownership against `ResumeRepository` and `TailoredResumeRepository`. Foreign or nonexistent IDs trigger `404 Not Found`.
3. **Safe Deletion Policy:**
   Only applications in `DRAFT` or `WITHDRAWN` states may be deleted via `DELETE /api/applications/{id}` (`204 No Content`). Attempting to delete an active application in `APPLIED`, `SCREENING`, or `INTERVIEW` is rejected with `400 Bad Request`.

---

## 5. AI Guidance SPI Extension

The `AIProvider` SPI and `MockDeterministicAIProvider` were extended with:
```java
AIApplicationGuidanceResponse generateApplicationGuidance(AIApplicationGuidanceRequest request);
```
- In offline/test environments, `MockDeterministicAIProvider` analyzes status, elapsed days since submission, and notes to produce:
  - Contextual advice (e.g. allowing standard review window vs. sending polite follow-up).
  - Pre-drafted follow-up email messages tailored with company name and job title.
- Purely advisory (Level 2 Human-in-the-Loop): no messages are automatically dispatched.

---

## 6. Frontend Angular Implementation

Located in `frontend/src/app/features/application-tracking`:
- `models/job-application.model.ts`: Strongly-typed TypeScript interfaces and union types.
- `services/application-tracking.service.ts`: Angular HTTP service for all REST operations.
- `components/application-tracker/application-tracker.component.ts`: Interactive stateful controller.
- `components/application-tracker/application-tracker.component.html`: Metrics header, filter bar, application table, and slide-over drawer with AI guidance & timeline audit.
- `components/application-tracker/application-tracker.component.css`: Responsive design matching dark navbar and modern slate styling.
- `app.component.html`: Tabbed navigation switching between "Application Tracker" and "Resume Tailoring".

---

## 7. Verification & Test Metrics

- **Maven Test Suite:**
  ```
  [INFO] Results:
  [INFO] Tests run: 109, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```
- **Angular Production Build:**
  ```
  Application bundle generation complete. [4.782 seconds]
  Output location: D:\ai-job-command-center\frontend\dist\ai-job-command-center-frontend
  ```
