# Use Cases

## Use Case UC-01: Ingest and Analyze Job Description
- **Primary Actor:** User
- **Preconditions:** User is logged in; Profile and verified skills are populated.
- **Main Success Scenario:**
  1. User navigates to Job Discovery and inputs job posting text or URL.
  2. System normalizes company and title, verifying no identical active job exists.
  3. System triggers asynchronous AI analysis (`JobAnalyzerService`).
  4. AI parses required competencies, experience level, and compensation.
  5. System evaluates requirements against user skills, computing match score and gaps.
  6. Job record is saved in status `ANALYZED`; user reviews score and recommendation.
- **Extensions:**
  - *2a. Duplicate Detected:* System alerts user with a link to the existing job record and halts redundant creation.

---

## Use Case UC-02: Generate & Approve Tailored Resume
- **Primary Actor:** User
- **Preconditions:** Job is in status `SHORTLISTED` or `ANALYZED`; Master Resume exists.
- **Main Success Scenario:**
  1. User selects "Tailor Resume" for the target job.
  2. AI engine aligns master resume bullets with JD keywords without fabricating unverified facts.
  3. Pre-render validator verifies all mentioned skills exist in `user_skills`.
  4. System enqueues draft resume into Human Approval Queue (Level 2).
  5. User inspects diff, performs manual inline adjustments, and clicks "Approve".
  6. System renders ATS-friendly PDF and transitions job to `READY_FOR_APPLICATION`.

---

## Use Case UC-03: Process Incoming Email & Handle OA/Interview
- **Primary Actor:** Scheduled Sync Worker
- **Preconditions:** Gmail OAuth authorization is active.
- **Main Success Scenario:**
  1. Sync worker queries Gmail API for new messages since last `historyId`.
  2. Worker stores raw message metadata and triggers AI Classifier.
  3. AI classifies message as `OA` with confidence 0.94 and extracts deadline date.
  4. System matches message to active Application record by company/sender domain.
  5. Application status updates to `OA`; study dossier task is generated.
  6. System drafts acknowledgment email and queues it in Human Approval Queue.
  7. User reviews draft in UI and clicks "Send via Gmail" (Level 3 execution).
