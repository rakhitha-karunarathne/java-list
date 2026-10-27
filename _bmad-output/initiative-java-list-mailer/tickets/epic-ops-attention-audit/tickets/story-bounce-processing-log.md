---
id: 7
type: story
title: "Bounce Processing Log"
parent: epic-ops-attention-audit
covers: [R2]
after: [1]
hitl: false
risk: medium
refined: true
status: done
---

# Bounce Processing Log

## Description

Admins get a Bounce Processing Log of bounce poll and process activity: process start/end, inbox connect/disconnect, each fetched message, each correlation, and member disables. Retention is parameter `bounce.process.log.retention.days`, default 90 (3 months).

## Requirements

1. **Events**
   **Given** a due bounce mailbox is polled
   **When** the poll runs
   **Then** the log records START, CONNECT, FETCH per message, DISCONNECT, and END
   **And** correlation records CORRELATE per ingested message
   **And** a member disable records DISABLE

2. **Admin page**
   **Given** an Admin
   **When** they open Bounce Processing Log
   **Then** they see the newest rows (time, event, message)
   **And** other roles do not have the nav item
   **And** `/api/activity-logs` is Admin-only

3. **Retention**
   **Given** the retention parameter
   **When** the nightly job runs
   **Then** bounce rows older than that many days are deleted
   **And** the default is 90

## Boundaries

- In: shared `activity_logs` table, bounce instrumentation, Admin page, retention parameter.
- Out: Queue Sending Log page and Mail-To-Send Processing Log page (later stories). Idle ticks with nothing due are not logged.

## Dev Agent Record

### Completion Notes

- `activity_logs` plus `ActivityLogService`. Bounce poll, correlate, and daily process write BOUNCE rows.
- Parameter default 90. Job purges BOUNCE, SEND, and MTS (send/MTS pages follow).
- Admin nav item. Index returns newest 500.

### Code Review

| Severity | Finding | Disposition |
|---|---|---|
| low | Empty poll and correlate ticks would flood the log | Fixed — skipped when nothing is due or ingested. Daily process still logs start/end |
| info | Index capped at 500 | Documented on the page. Older rows remain until retention |
