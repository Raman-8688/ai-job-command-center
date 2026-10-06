# API Versioning Strategy

## 1. Strategy: URI Path Versioning
- Standard operational endpoints carry an explicit major version prefix: `/api/v1/...`
- Non-versioned `/api/...` routes will redirect or map to `/api/v1/...` by default.

## 2. Breaking vs. Non-Breaking Policy
- **Non-Breaking Changes (In-Place Evolution):** Adding new optional fields to response DTOs, adding new endpoints, or adding optional query parameters.
- **Breaking Changes (Requires New Version):** Renaming or removing existing fields, altering HTTP status codes, changing required request parameters.
