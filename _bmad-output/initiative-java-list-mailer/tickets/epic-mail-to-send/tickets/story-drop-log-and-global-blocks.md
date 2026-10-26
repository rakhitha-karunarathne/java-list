---
id: 4
type: story
title: "Drop log and global blocks"
parent: epic-mail-to-send
covers: [R4]
after: [3]
hitl: false
risk: high
refined: true
---

# Drop log and global blocks

## Description

Operator views and actions for Mail-to-send **Drop log** (distinct from **From-domain Issues**), **global blocks** on senders and domains, and sender-only auto-block after N counted rejects per day. Stranger paths stay silent with no courtesy reject; blocked parties drop silently after auth.

## Requirements

1. **Drop log per mailbox and combined view (R4)**
   **Given** ingest writes Drop log rows (story 3)
   **When** an Admin opens Drop log from ops UI or from an MTS inbox config link
   **Then** rows show mailbox, reason, timestamp, and whether the row counted toward auto-block
   **And** inbox config links open Drop log pre-filtered to that mailbox
   **And** a combined filterable view spans all mailboxes

2. **Row reason and auto-block flag (R4)**
   **Given** a Drop log row exists
   **When** an Admin views the row
   **Then** reason text matches the ingest outcome (auth fail, spam header, stranger unapproved, cross-inbox reject, etc.)
   **And** the counted-for-auto-block flag matches story 3 matrix rules

3. **Admin actions on Drop rows (R4)**
   **Given** an Admin views a Drop log row
   **When** they resolve, block sender, block domain, or ignore
   **Then** the action persists and is reflected on subsequent ingests
   **And** block sender/domain adds to global block lists (Admin may unblock)

4. **Auto-block sender after N/day (R4)**
   **Given** Parameter N counted rejects per sender per day (default 10) and rows marked counted-for-auto-block
   **When** a sender reaches N within a rolling day window
   **Then** the sender is auto-added to global sender blocks
   **And** domains are never auto-blocked (sender-only)

5. **Blocked sender/domain after auth (R4)**
   **Given** a sender or From domain is on global block list
   **When** a message from that party reaches ingest and auth would run
   **Then** after auth evaluation the message is silently dropped
   **And** behavior matches stranger silent path (no courtesy reject)

6. **No stranger courtesy reject (R4)**
   **Given** an unapproved stranger sends to an MTS inbox
   **When** any reject/silent path applies except the cross-inbox authorized notify case (story 3)
   **Then** no SMTP courtesy rejection or bounce is sent that confirms the inbox address
   **And** outcome is silent delete and/or Drop log only

7. **From-domain Issues excluded (R4)**
   **Given** a From-domain Issue is opened (story 2)
   **When** operators search Drop log or auto-block metrics
   **Then** that path does not appear as a Drop log substitute and does not increment auto-block counters

8. **Stranger path integration test (R4)**
   **Given** fixtures for stranger silent drop and counted rejects
   **When** messages are ingested until N counted drops
   **Then** sender appears in global blocks and subsequent mail is silently dropped
   **And** Drop log lists reasons for each drop

## Boundaries

- Daily digest on Notice path only when new Drop log items that day — optional smoke; not a substitute for row-level tests.
- No hourly auto-block cap in V1.
- From-domain Issues, hold folders, and delete-and-notify remain story 2.
- Campaign Queued creation remains story 1 + gates; this story does not create Campaigns.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-mail-to-send/epic-mail-to-send.md
- prd — _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/prd-java-list-mailer.md (FR-31)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Prefer mail-server edge rejection where configured; app still validates inbox mail per parent initiative.
