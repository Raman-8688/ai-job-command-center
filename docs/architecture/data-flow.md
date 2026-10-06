# End-to-End Data Flow

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Web as Angular Client
    participant JobMod as Job Module
    participant AIMod as AI Engine
    participant ResMod as Resume Module
    participant ApprQueue as Approval Queue
    participant Gmail as Gmail Worker / API

    %% Phase 1: Ingestion & Analysis
    User->>Web: Paste Job Description / URL
    Web->>JobMod: POST /api/jobs
    JobMod->>JobMod: Check deduplication hash
    JobMod-->>Web: 201 Created (Status: DISCOVERED)
    JobMod->>AIMod: Request JD Extraction & Match Analysis
    AIMod->>AIMod: Ground analysis with UserSkills
    AIMod-->>JobMod: Parsed JD, Match Score (88%), Missing Skills
    JobMod->>JobMod: Update status to ANALYZED

    %% Phase 2: Resume Tailoring
    User->>Web: Request Tailored Resume
    Web->>ResMod: POST /api/resumes/tailor
    ResMod->>AIMod: Generate targeted bullet phrasing
    AIMod-->>ResMod: Draft tailored text
    ResMod->>ResMod: Run Factual Grounding Validator
    ResMod->>ApprQueue: Enqueue Level 2 Draft (PENDING_APPROVAL)
    ApprQueue-->>Web: Notification: Draft Resume Ready
    User->>Web: Review diff & Approve
    Web->>ApprQueue: POST /api/approval-queue/{id}/approve
    ApprQueue->>ResMod: Compile final ATS PDF

    %% Phase 3: Background Email Sync & Interview
    loop Every 15 Minutes
        Gmail->>Gmail: Poll unread messages
        Gmail->>AIMod: Classify email text
        AIMod-->>Gmail: Category: INTERVIEW, Date: 2026-10-15 14:00
        Gmail->>JobMod: Update Application to INTERVIEW
        Gmail->>ApprQueue: Enqueue Draft Confirmation Reply (Level 2)
    end
    User->>Web: Review recruiter draft and click Send (Level 3)
    Web->>Gmail: Send message via Gmail REST API
```
