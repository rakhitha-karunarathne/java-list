---
id: 8
type: story
title: "Queue Sending Log"
parent: epic-ops-attention-audit
covers: [R2]
after: [7]
hitl: false
risk: medium
refined: true
status: done
---

# Queue Sending Log

## Description

Admins get a Queue Sending Log of queue drain activity: process start/end, sending-path connect/disconnect, and each message sent. Retention is `send.queue.log.retention.days`, default 90 (3 months).

## Requirements

1. **Events**
   **Given** the send queue has pending rows
   **When** a batch runs
   **Then** the log records START, CONNECT and DISCONNECT per sending path used, SEND per message, and END

2. **Admin page**
   **Given** an Admin
   **When** they open Queue Sending Log
   **Then** they see newest SEND rows
   **And** other roles do not have the nav item

3. **Retention**
   **Given** the retention parameter
   **When** the nightly job runs
   **Then** SEND rows older than that many days are deleted
   **And** the default is 90

## Boundaries

- Out: test sends (not queue processing). Empty ticks are not logged. Connect is once per sending path in a batch, not once per message.

## Dev Agent Record

### Code Review

| Severity | Finding | Disposition |
|---|---|---|
| low | SMTP has no session API, so connect per message would duplicate SEND | Fixed — CONNECT/DISCONNECT once per path in the batch |
| info | A failed batch rolls the log back with the send transaction | Kept — the log matches committed sends |
