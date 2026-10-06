# ADR-008: Local-First Development & File Storage

## Status
Accepted

## Context
When generating resumes, storing raw job descriptions, and caching email attachment metadata, the system requires file storage. Deploying cloud object storage (e.g., AWS S3, Google Cloud Storage) during local development adds cloud credential dependencies, cost, and complexity when offline.

## Decision
We adopt a **Local-First Storage Strategy** with a clean storage interface abstraction:
```java
public interface StorageService {
    String storeFile(byte[] content, String filename, String mimeType);
    byte[] retrieveFile(String fileKey);
    void deleteFile(String fileKey);
}
```
The initial implementation (`LocalStorageService`) persists files directly to an encrypted or protected local directory (`./storage/files`) referenced in `.env`. The interface enables seamless migration to AWS S3 or MinIO in future deployment phases without impacting domain services.

## Alternatives Considered
- **Direct AWS S3 dependency from day one:** Rejected as unnecessary cloud complexity and recurring cost for local execution.
- **Storing binary PDF files directly in PostgreSQL as bytea/BLOBs:** Rejected because large binary payloads degrade database backup speed, cache efficiency, and query performance.

## Consequences
- **Positive:** Works 100% locally with zero cloud dependencies; rapid iteration; decoupled domain architecture.
- **Negative:** Local disk must be backed up alongside PostgreSQL volumes during system maintenance.
