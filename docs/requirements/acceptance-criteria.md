# Acceptance Criteria

### AC-001: Skill Verification & Hallucination Defense
- Given a candidate profile containing 5 verified skills (e.g., Java, Spring Boot, PostgreSQL, Docker, AWS).
- When a tailored resume is generated for a job requiring Kubernetes and Go (which the candidate lacks).
- Then the generated resume MUST NOT add Kubernetes or Go to the candidate's skills list or bullet points.
- And the Job Analysis view MUST explicitly list Kubernetes and Go under `missingSkills`.

### AC-002: Duplicate Detection
- Given an existing job posting for "Acme Corp - Senior Java Engineer" with URL `https://careers.acme.com/jobs/123`.
- When the user attempts to ingest a job posting with URL `https://careers.acme.com/jobs/123?source=linkedin`.
- Then the system must detect the normalized canonical match.
- And the system must prompt the user that this job already exists in the pipeline instead of creating a second row.

### AC-003: Human Approval Before External Action
- Given an incoming interview invitation email classified with 0.98 confidence.
- When the system creates a draft confirmation response.
- Then the email MUST NOT be sent automatically to the recruiter.
- And the draft must remain in the `PENDING_APPROVAL` state in the Approval Queue until explicit human confirmation.

### AC-004: Idempotent Email Ingestion
- Given an email message with Gmail ID `msg_987654321`.
- When the sync worker processes the email multiple times during repeated sync intervals.
- Then exactly one record exists in `emails` and `email_classifications`.
- And no duplicate `application_events` are created.
