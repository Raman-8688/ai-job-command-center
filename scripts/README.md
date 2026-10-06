# Scripts & Tooling - AI Job Command Center

## Scope
Utility and automation scripts for development, maintenance, and database operations.

## Directory Structure
- `scripts/dev/`: Developer workflow scripts (seed data generators, environment validator).
- `scripts/db/`: Database backup, restore, and migration validation scripts.
- `scripts/security/`: Pre-commit secret scanning, credential verification, and token sanitation checks.

## Guidelines
- All scripts must fail cleanly with non-zero exit codes upon errors.
- Never hardcode credentials, private tokens, or sensitive URLs in scripts.
