# API Conventions & Error Standards

## 1. Unified Response Envelope
Standard success endpoints return the payload directly or wrapped within a consistent pagination object.

### Pagination Contract
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 142,
  "totalPages": 8,
  "last": false
}
```

## 2. Standardized Error Response Contract
All exceptions intercepted by Spring's `@ControllerAdvice` return the RFC 7807-compliant structure:
```json
{
  "timestamp": "2026-10-06T22:15:00Z",
  "status": 400,
  "code": "INVALID_JOB_DATA",
  "message": "Job description must not be empty",
  "path": "/api/jobs",
  "correlationId": "req-98f24b-321a",
  "validationErrors": [
    {
      "field": "description",
      "rejectedValue": "",
      "message": "Must be between 50 and 50000 characters"
    }
  ]
}
```
Stack traces are strictly suppressed from all client responses.
