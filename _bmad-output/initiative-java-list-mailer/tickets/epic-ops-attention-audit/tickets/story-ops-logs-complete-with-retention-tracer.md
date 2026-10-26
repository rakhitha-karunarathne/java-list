---
id: 1
type: story
title: "Ops Logs complete with retention tracer"
parent: epic-ops-attention-audit
covers: [R2]
after: [epic-unsubscribe-consent, epic-bounce-processing, epic-mail-to-send]
hitl: true
risk: medium
refined: true
---

# Ops Logs complete with retention tracer

## Description

Make **Ops Logs** the durable operator trail for send-queue passes, **bounce mailbox poll**, and **mail-to-send (MTS) poll** activity. Earlier epics may emit stub hooks; this story completes ingestion, the Admin **Ops Logs** index UI, and retention purge driven by **Parameters** (default **90 days**). Failures from send, bounce poll, and MTS poll also raise **Attention** items for **Admin** (highlighting completes in story 2).

## Requirements

1. **Send pass logged** (R2)
   **Given** a Campaign send pass runs (success or SMTP failure) in the test profile with GreenMail
   **When** the pass completes or throws
   **Then** an Ops Logs row exists with job kind send, Campaign identity, outcome, and error summary on failure
   **And** no signing secrets, SMTP passwords, or full credential strings appear in the row (C1)

2. **Bounce poll logged** (R2)
   **Given** a catalogued Bounce mailbox poll runs via mailbox-query (success or IMAP failure)
   **When** the poll tick finishes
   **Then** an Ops Logs row exists with job kind bounce poll, mailbox identity, and message count or error detail
   **And** the row is visible on the Ops Logs index without reopening bounce correlation logic (C3)

3. **MTS poll logged** (R2)
   **Given** an MTS inbox poll runs (success or failure) after epic-mail-to-send hooks exist
   **When** the poll tick finishes
   **Then** an Ops Logs row exists with job kind MTS poll, inbox identity, and outcome
   **And** auth/spam/drop decisions remain owned by send-to-mail; this story only records poll/ingest outcomes (C3)

4. **Ops Logs index** (R2)
   **Given** an **Admin** or **List manager** with access to Ops Logs per role rules
   **When** they open **Ops Logs** from the sidebar
   **Then** they see a filterable index of send, bounce poll, and MTS poll entries newest-first
   **And** row drill-down opens an ops log **detail** page reached from the index, not as a primary sidebar destination (R4 — verified in story 4)

5. **Send failure → Attention hook** (R2)
   **Given** a send or test-send SMTP failure recorded in Ops Logs
   **When** the failure is persisted
   **Then** an Attention item is created or updated for **Admin** with ops-error classification
   **And** List managers do not receive ops-error Attention items for mailboxes or paths outside their scope (R1 — inbox rules in story 2)

6. **Retention from Parameters** (R2, C2)
   **Given** the ops log retention **Parameter** is seeded at **90 days** (AD-15) and Ops Logs rows exist older than that window in test
   **When** the ops retention purge job runs (dry-run or test-shortened window)
   **Then** rows with `created_at` before the retention cutoff are deleted
   **And** rows inside the window remain; changing the Parameter changes the cutoff without code edits (C2)

7. **Tracer: one failure end-to-end** (R2)
   **Given** GreenMail stopped or SMTP misconfigured for a one-list send in test profile
   **When** the operator queues or test-sends
   **Then** Ops Logs shows the failure and Admin Attention receives the ops highlight hook
   **And** re-running send after fixing SMTP does not duplicate spurious Attention rows without a new failure

## Boundaries

- Must not change: bounce correlation ladder, Member disable streaks, Campaign lifecycle, or MTS auth/spam matrix (C3).
- Must not implement: full Attention inbox UX by role (story 2) or Audit rows (story 3).
- Out: external SIEM export; real-time push notifications (epic Boundaries).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-ops-attention-audit/epic-ops-attention-audit.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-15 Parameters, AD-16 ops-log slice)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Tracer story: human sign-off on first GreenMail failure → Ops Logs → Attention hook path.
