# ADR-006: Email Synchronization — Scheduled Polling Before Webhooks

## Status
Accepted

## Context
Gmail supports real-time push notifications using Google Cloud Pub/Sub webhooks. However, setting up Cloud Pub/Sub requires:
1. A publicly reachable HTTPS endpoint with a verified domain.
2. An active Google Cloud Pub/Sub topic and subscription.
3. Complex tunneling (e.g., ngrok) during local development.
For a personal command center running locally, this infrastructure is overly complex and fragile.

## Decision
We implement a **Scheduled Polling Synchronization Strategy** using Spring's `@Scheduled` executor and the Gmail API's `users.history.list` endpoint:
- The system polls every 15 minutes (configurable via `GMAIL_SYNC_INTERVAL_MINUTES`).
- The system stores the last processed `historyId` to fetch only delta updates.
- A manual "Sync Now" button will be provided in the frontend UI for immediate on-demand fetching.
- Push-based Google Cloud Pub/Sub will be deferred to a future phase if cloud hosting is adopted.

## Alternatives Considered
- **Google Cloud Pub/Sub Webhooks:** Deferred due to local networking complexity and unnecessary infrastructure overhead in early phases.
- **Continuous Tight Polling (<1 min):** Rejected to avoid exhausting Gmail API quotas.

## Consequences
- **Positive:** Zero external tunneling required; works reliably offline and behind local NAT; simple error recovery and resumption.
- **Negative:** New emails are discovered with a maximum delay equal to the polling interval (e.g., 15 minutes), which is fully acceptable for job search workflows.
