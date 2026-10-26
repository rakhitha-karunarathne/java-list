---
id: 9
type: story
title: "Mail-To-Send Processing Log"
parent: epic-ops-attention-audit
covers: [R2]
after: [8]
hitl: false
risk: medium
refined: true
status: done
---

# Mail-To-Send Processing Log

## Description

Admins get a Mail-To-Send Processing Log: process start/end, inbox connect/disconnect, each fetched message, and each campaign created with the lists added. Retention is `mts.process.log.retention.days`, default 90 (3 months).

## Requirements

1. **Events**
   **Given** a due MTS inbox is polled
   **When** messages are fetched
   **Then** the log records START, CONNECT, FETCH per message, CAMPAIGN with list ids when a campaign is created, DISCONNECT, and END

2. **Admin page**
   **Given** an Admin
   **When** they open Mail-To-Send Processing Log
   **Then** they see newest MTS rows
   **And** other roles do not have the nav item

3. **Retention**
   **Given** the retention parameter
   **When** the nightly job runs
   **Then** MTS rows older than that many days are deleted
   **And** the default is 90

## Boundaries

- Out: an on/off switch for the log (next story). Idle ticks with nothing due are not logged.

## Dev Agent Record

### Code Review

| Severity | Finding | Disposition |
|---|---|---|
| low | FETCH is written even when the message is dropped | Kept — "each email fetched and processed" includes the gate result |
| info | Index capped at 500 | Same as the other processing logs |
