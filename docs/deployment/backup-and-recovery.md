# Backup & Disaster Recovery

## 1. Backup Scope
A complete disaster recovery backup consists of:
1. **PostgreSQL Relational Dump:** All tables, applications, event journals, and encrypted token records.
2. **Local Storage Archive:** Directory `./storage/files` containing generated resume PDFs and attachments.

## 2. Automated Daily Backup Script
A lightweight shell script (`scripts/db/backup.sh`) executes `pg_dump`:
```bash
pg_dump -U postgres -d job_command_center -F c -b -v -f "./backups/db_$(date +%Y%m%d_%H%M%S).dump"
```
Backups older than 30 days are automatically rotated and pruned.
