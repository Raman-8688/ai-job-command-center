# REST API Design Guidelines

## 1. Core Principles
- **Resource-Oriented:** Endpoints represent domain nouns (`/api/jobs`, `/api/applications`, `/api/resumes`), not verbs.
- **Stateless Communication:** Every request contains necessary authentication tokens (Bearer JWT) and context.
- **DTO Isolation:** JPA entities are strictly internal to the persistence layer. Controllers accept and return strongly typed Java Records / DTOs.
- **Predictable HTTP Status Codes:**
  - `200 OK`: Request succeeded.
  - `201 Created`: Resource successfully created (includes `Location` header).
  - `204 No Content`: Successful mutation returning no body.
  - `400 Bad Request`: Input validation failed or malformed JSON.
  - `401 Unauthorized`: Missing or invalid credentials.
  - `403 Forbidden`: Authenticated user lacks permission.
  - `404 Not Found`: Resource does not exist.
  - `409 Conflict`: Resource state conflict (e.g., duplicate job hash).
  - `500 Internal Server Error`: Unexpected server exception.
