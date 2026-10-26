---
id: 3
type: story
title: "Append-only Audit with Campaign status events"
parent: epic-ops-attention-audit
covers: [R3]
after: [1]
hitl: false
risk: high
refined: true
---

# Append-only Audit with Campaign status events

## Description

Complete the single **append-only Audit** log: durable, immutable rows for operator and system actions with **no raw secrets**. **Admin** sees all events; **List managers** see events scoped to Members they manage. Retention purge reads **Parameters** (default **365 days**). Campaign **status** transitions—**Queue**, **Stop**, **Requeue**, **Finished**, and **list added** for Finished Requeue—must appear alongside consent/unsub/bounce events emitted by earlier epics.

## Requirements

1. **Append-only store** (R3)
   **Given** any audited action occurs
   **When** the Audit row is written
   **Then** existing rows are never updated or deleted except by the retention purge job
   **And** API/UI exposes read-only history

2. **No raw secrets in Audit** (R3, C1)
   **Given** an action involves signing keys, SMTP/IMAP passwords, opaque unsubscribe tokens, or mailbox credentials
   **When** Audit is written
   **Then** payloads contain actor, action type, entity references, and redacted summaries only
   **And** grep of Audit text fields in test fixtures finds no plaintext secrets (C1)

3. **Admin sees all Audit** (R3)
   **Given** an **Admin** and Audit rows from Campaign, consent, unsub, and catalog actions
   **When** Admin opens **Audit** (sidebar or Admin group per mock IA)
   **Then** all rows within retention are visible with filter/search as implemented
   **And** scope is not limited by List grants

4. **List manager member-scoped Audit** (R3)
   **Given** a **List manager** for List A and Audit rows for Member M on List A and Member N only on List B
   **When** the List manager opens Audit
   **Then** rows referencing M (or List A operations on M) are visible
   **And** rows for N or List B-only actions are absent

5. **Campaign Queue audited** (R3)
   **Given** a Draft Campaign
   **When** the operator queues send (status → Queued)
   **Then** an Audit row records Queue with Campaign id and actor
   **And** send-queue execution details stay in Ops Logs, not duplicated as secrets in Audit

6. **Campaign Stop audited** (R3)
   **Given** a Campaign in Draft, Queued, or Sending
   **When** Stop succeeds (status → Stopped)
   **Then** an Audit row records Stop with Campaign id and actor

7. **Campaign Requeue audited** (R3)
   **Given** a Stopped Campaign
   **When** Requeue succeeds
   **Then** an Audit row records Requeue with Campaign id and actor

8. **Campaign Finished audited** (R3)
   **Given** a send pass completes
   **When** status becomes Finished
   **Then** an Audit row records Finished with Campaign id (system or operator actor as appropriate)

9. **List added for Finished Requeue audited** (R3)
   **Given** a Finished Campaign
   **When** the operator adds a List before Requeue (parent R35 / initiative list-add)
   **Then** an Audit row records list added with Campaign id and List id
   **And** a subsequent Requeue row exists when Requeue runs

10. **Retention from Parameters** (R3, C2)
    **Given** audit retention **Parameter** seeded at **365 days** and rows older than the window in test
    **When** the audit retention purge job runs
    **Then** expired rows are removed and in-window rows remain
    **And** changing the Parameter changes purge behavior without hard-coded-only cutoff (C2)

11. **Cross-epic events present** (R3)
    **Given** HTTPS unsub and consent approve flows from epic-unsubscribe-consent and bounce disable from epic-bounce-processing already emit Audit hooks
    **When** those flows run in integration test
    **Then** corresponding Audit rows appear with no raw secrets
    **And** this story does not redefine those action semantics (C3)

## Boundaries

- Must not change: Campaign lifecycle rules, token crypto, or bounce disable algorithms (C3).
- Must not duplicate: Ops Logs job traces (story 1); Attention inbox (story 2).
- Out: SIEM export; Audit row editing or operator delete (epic Boundaries).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-ops-attention-audit/epic-ops-attention-audit.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-15 retention seeds, AD-16 audit slice)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- high risk: secret leakage in Audit payloads or overly broad List manager scope.
