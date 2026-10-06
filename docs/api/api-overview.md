# REST API Overview & Endpoints

## 1. Core Endpoints Summary

### User & Skills
- `GET /api/v1/profile` — Fetch candidate profile and goals.
- `PUT /api/v1/profile` — Update candidate contact details and preferences.
- `GET /api/v1/skills` — List candidate skill inventory with verification flags.
- `POST /api/v1/skills` — Add new verified or learning skill.

### Jobs & Pipeline
- `GET /api/v1/jobs` — Query jobs with pagination, status filtering, and search.
- `POST /api/v1/jobs` — Ingest new job description or URL.
- `GET /api/v1/jobs/{id}` — Fetch job details and analysis report.
- `POST /api/v1/jobs/{id}/analyze` — Trigger asynchronous AI analysis.

### Resumes & Tailoring
- `GET /api/v1/resumes/master` — Retrieve master resume sections.
- `POST /api/v1/resumes/tailor` — Request tailored resume for specific job (Level 2 draft).
- `GET /api/v1/resumes/{id}/pdf` — Download compiled ATS-friendly PDF.

### Applications & Lifecycle
- `GET /api/v1/applications` — List applications with pipeline stage filter.
- `POST /api/v1/applications` — Transition a job to applied.
- `GET /api/v1/applications/{id}/events` — Audit trail of state transitions.

### Approval Queue
- `GET /api/v1/approval-queue` — List pending Level 2/3 action items.
- `POST /api/v1/approval-queue/{id}/approve` — Approve and execute pending action.
- `POST /api/v1/approval-queue/{id}/reject` — Reject draft action.

### Email & Sync
- `POST /api/v1/emails/sync` — Trigger immediate on-demand Gmail poll.
- `GET /api/v1/emails/threads/{id}` — View synchronized job email thread.
- `POST /api/v1/emails/drafts/{id}/send` — Explicit Level 3 action to send approved draft.

### Dashboard & Analytics
- `GET /api/v1/dashboard/summary` — Overview metrics, upcoming interviews, urgent tasks.
- `GET /api/v1/analytics/funnel` — Funnel conversion rates and time-in-stage metrics.
