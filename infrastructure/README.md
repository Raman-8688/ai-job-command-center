# Infrastructure - AI Job Command Center

## Scope
This directory contains deployment, virtualization, and local runtime infrastructure definitions for the AI Job Command Center.

## Components
- `docker-compose.yml`: Root compose definition for local services (PostgreSQL 16).
- `postgres/`: Database initialization scripts and custom health check extensions.
- `local-storage/`: Local mounted directories for encrypted file storage.

## Principles
- Keep infrastructure lightweight and local-first.
- Do not introduce distributed orchestrators (Kubernetes, Nomad) or messaging brokers (Kafka, RabbitMQ) unless concrete scale demands require them.
- Ensure automated recovery and zero-loss volume persistence for PostgreSQL.
