# Functional Requirements Specification

Each requirement is uniquely identified by `FR-XXX`.

---

## Module 1: User Profile & Verified Skill Matrix
- **FR-001:** The system shall record and maintain the candidate's core profile: Full Name, Email, Phone, Location, Portfolio Links (GitHub, LinkedIn, Website), Target Job Titles, Notice Period, and Compensation Expectations.
- **FR-002:** The system shall maintain a structured inventory of candidate skills, requiring for each skill: Name, Category (Language, Framework, Database, DevOps, etc.), Proficiency Level (Beginner, Intermediate, Advanced, Expert), Years of Experience, Experience Type (`PRODUCTION`, `PERSONAL_PROJECT`, `LEARNING`, `ACADEMIC`, `INTERVIEW_PREPARATION`), and Verification Status (`VERIFIED`, `UNVERIFIED`).
- **FR-003:** The system shall strictly prohibit the AI engine from citing skills or experiences marked as unverified or absent from the candidate profile during resume or communication generation.

---

## Module 2: Job Ingestion & Management
- **FR-004:** The system shall support manual creation and direct pasting of raw Job Descriptions (JDs) as well as job posting URLs.
- **FR-005:** The system shall compute a deduplication signature (based on normalized company name, normalized title, location, and canonical URL) to prevent redundant job entries.
- **FR-006:** The system shall track job status across the pipeline: `DISCOVERED`, `ANALYZED`, `SHORTLISTED`, `READY_FOR_APPLICATION`, `APPLIED`, `OA`, `INTERVIEW`, `OFFER`, `REJECTED`, `WITHDRAWN`, `EXPIRED`, `NO_RESPONSE`.

---

## Module 3: AI Job Analysis & Fit Scoring
- **FR-007:** The system shall parse unstructured JDs using versioned AI prompts to extract: Company, Title, Required Years of Experience, Required Skills, Preferred Skills, Responsibilities, Salary range (if available), Location, and Remote classification.
- **FR-008:** The system shall compute an objective Fit Score (0-100) alongside broken-down sub-scores: Technical Match, Experience Match, Role Match, and Location Match.
- **FR-009:** The system shall generate a structured Skill Gap list highlighting missing technologies and provide a recommendation category: `APPLY`, `CONSIDER`, `LOW_PRIORITY`, or `DO_NOT_APPLY`.

---

## Module 4: Resume Studio & Tailoring
- **FR-010:** The system shall maintain a comprehensive Master Resume containing verified work experience, achievements, education, and project bullet points.
- **FR-011:** The system shall generate tailored, job-specific resume variants by selecting relevant verified achievements and re-ordering technical bullet points to match the target JD.
- **FR-012:** The system shall perform a strict pre-compilation factual audit to verify that no fabricated facts or unverified technologies were introduced by the LLM.
- **FR-013:** The system shall compile and render tailored resumes to industry-standard ATS-friendly PDF format.

---

## Module 5: Email Synchronization & Intelligence
- **FR-014:** The system shall securely authenticate with Google Gmail using OAuth 2.0 and synchronize job-related email threads via scheduled polling.
- **FR-015:** The system shall classify incoming messages into: `JOB_ALERT`, `APPLICATION_CONFIRMATION`, `RECRUITER`, `OA`, `INTERVIEW`, `REJECTION`, `OFFER`, `FOLLOW_UP`, `HR`, or `OTHER`.
- **FR-016:** The system shall extract critical stage metadata from classified emails, including interview dates, timezones, assessment deadlines, recruiter contact details, and confidence scores. Messages with confidence below 0.80 shall be flagged for `MANUAL_REVIEW`.

---

## Module 6: Automation & Human Approval Queue
- **FR-017:** The system shall enforce a Human Approval Queue for all Level 2 and Level 3 actions (resume variants, cover letters, recruiter replies, application submissions).
- **FR-018:** The system shall maintain an immutable event log (`application_events`) for every state transition in an application's lifecycle.
- **FR-019:** The system shall evaluate configurable follow-up rules (e.g., 5 days post-application without confirmation, 10 days post-interview) and generate draft follow-up messages for human approval.

---

## Module 7: Interview & Assessment Intelligence
- **FR-020:** Upon detecting an Online Assessment (OA) invitation, the system shall record assessment platform, duration, deadline, and generate a targeted technical study checklist.
- **FR-021:** Upon detecting an interview invitation, the system shall generate a comprehensive interview dossier containing: company overview, expected technical questions, relevant user projects to highlight, behavioral question outlines (STAR format), and questions to ask the interviewer.

---

## Module 8: Analytics & Dashboard
- **FR-022:** The system shall provide an executive dashboard displaying pipeline counts, pending approvals, upcoming interview countdowns, and active OA deadlines.
- **FR-023:** The system shall calculate funnel conversion analytics: Application Response Rate, Interview Conversion Rate, OA Pass Rate, and Offer Rate.
