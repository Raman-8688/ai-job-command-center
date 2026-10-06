# Data Dictionary

## 1. Key Data Enumerations

### Experience Type (`experience_type`)
- `PRODUCTION`: Commercial, production-grade enterprise software experience.
- `PERSONAL_PROJECT`: Hands-on development in personal/open-source projects.
- `LEARNING`: Actively studying or completing course tutorials.
- `ACADEMIC`: University or academic research coursework.
- `INTERVIEW_PREPARATION`: Practiced specifically for technical interview problems.

### Job Status (`job_status`)
- `DISCOVERED`: Ingested into database, pending detailed analysis.
- `ANALYZED`: Analyzed by AI engine; score and gaps calculated.
- `SHORTLISTED`: User marked opportunity as a high-priority target.
- `READY_FOR_APPLICATION`: Tailored resume and artifacts approved by user.
- `APPLIED`: Application submitted externally.
- `OA`: Online Assessment invitation received.
- `INTERVIEW`: Technical/behavioral interview scheduled.
- `OFFER`: Formal offer extended.
- `REJECTED`: Application rejected.
- `WITHDRAWN`: Application withdrawn by candidate.
- `EXPIRED`: Posting closed or deadline passed.
- `NO_RESPONSE`: Inactive for >30 days post-application.

### Email Category (`email_category`)
- `JOB_ALERT`: Generic automated alerts from job boards.
- `APPLICATION_CONFIRMATION`: Official acknowledgment of receipt.
- `RECRUITER`: Direct personal message from a recruiter/talent partner.
- `OA`: Online assessment invitation or HackerRank/Codility link.
- `INTERVIEW`: Interview request or calendar invitation.
- `REJECTION`: Formal rejection email.
- `OFFER`: Offer letter or compensation discussion.
- `FOLLOW_UP`: Follow-up inquiry regarding an existing application.
- `HR`: Logistics or background check communication.
- `OTHER`: Unclassified email.
