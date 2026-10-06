# Docker & Container Strategy

## 1. Principles
- Lightweight container footprints: use Alpine or distroless base images.
- Isolated named volumes for PostgreSQL data (`postgres_data`) to prevent data loss across container restarts.
- Multi-stage Docker builds for the Spring Boot application (using layered JARs for instant restart performance).

## 2. Docker Compose Layout
- `postgres`: Primary database running PostgreSQL 16 Alpine on port 5432 with integrated health checks.
- (Optional Dev): `pgadmin` or lightweight DB admin GUI behind a local profile.
