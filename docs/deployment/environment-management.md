# Environment Management

## 1. Environment Profiles
The Spring Boot application recognizes three primary execution profiles:
- `local`: Developer workstation running local PostgreSQL and mock or live AI.
- `test`: Automated integration testing profile using Testcontainers.
- `prod`: Private production deployment on a dedicated server or VPS.

## 2. Configuration Precedence
1. Command-line JVM arguments (`-D...`)
2. Operating System environment variables (`APP_ENV`, `DB_PASSWORD`, etc.)
3. Local `.env` file (loaded via Spring dotenv integration)
4. `application.yml` defaults
