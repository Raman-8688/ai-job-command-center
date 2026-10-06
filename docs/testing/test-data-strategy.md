# Test Data Strategy & Fixtures

## 1. Zero Real Data Policy in Test Fixtures
- Real personal resumes, live employer emails, and real recruiter phone numbers must **NEVER** be committed to Git test fixtures.
- All test fixtures must use synthetic, anonymized identities (e.g., `"Alex Mercer"`, `"Acme Corp"`, `"recruiter@example.com"`).

## 2. Seed Data Profiles
Test environments utilize predictable seed profiles:
- `seed-junior-dev.json` (Entry-level profile with 3 verified skills).
- `seed-senior-lead.json` (Staff/Lead profile with 25 verified skills, diverse production roles).
- `seed-sample-jds.json` (10 categorized realistic job postings).
- `seed-sample-emails.json` (Synthetic OA invites, interview requests, and rejection emails).
