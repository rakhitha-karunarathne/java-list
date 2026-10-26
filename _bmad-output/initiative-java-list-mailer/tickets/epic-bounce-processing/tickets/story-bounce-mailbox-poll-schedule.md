---
id: 2
type: story
title: "Bounce mailbox poll schedule"
parent: epic-bounce-processing
covers: [R3]
after: [1]
hitl: false
risk: medium
refined: true
---

# Bounce mailbox poll schedule

## Description

Schedule polling of every catalogued **Bounce mailbox** through **mailbox-query**: stream each message into the bounce slice for ingest (persist raw bounce activity, prepare for correlation). Respect per-mailbox poll interval (catalog default 15 minutes). Record poll outcomes in **Ops Logs**; poll errors feed **Attention** per initiative R25 hooks.

## Requirements

1. **All catalogued bounce mailboxes polled** (R3)
   **Given** two or more Bounce mailboxes exist in the catalog with valid IMAP settings
   **When** the bounce poll schedule runs in the test profile
   **Then** each mailbox is visited in turn via mailbox-query
   **And** no mailbox is permanently skipped while enabled in catalog

2. **Pickup within configured interval** (R3)
   **Given** a Bounce mailbox with poll interval set to a short test value (e.g. 1 minute in test profile)
   **When** a new message is delivered to that mailbox’s IMAP inbox
   **Then** a subsequent poll pass ingests it within one interval of delivery
   **And** the bounce record (or equivalent) shows received_at from poll time

3. **Sequential inbox polling** (R3)
   **Given** multiple Bounce mailboxes due at the same tick
   **When** the scheduler fires
   **Then** inboxes are processed one at a time, not in parallel IMAP sessions on one JVM
   **And** total tick duration reflects sequential IO (no overlapping mailbox-query runs)

4. **Same schedule class non-overlap** (R3)
   **Given** a bounce poll tick is still running (slow IMAP)
   **When** the next tick for the same bounce poll schedule would start
   **Then** the second tick is skipped or deferred (in-process only; epic C2)
   **And** no duplicate ingest of the same message from concurrent polls

5. **Ops Logs on poll activity** (R3)
   **Given** a successful poll pass that ingests at least one message
   **When** an operator views Ops Logs (or test assertions read the ops-log port)
   **Then** an entry exists for bounce mailbox poll with mailbox identity and message count or outcome
   **And** failures (auth, timeout, parse) also produce Ops Log entries with error detail

6. **Poll separate from bounce-process** (R3)
   **Given** ingested bounces exist but daily bounce-process has not run
   **When** poll continues on schedule
   **Then** new bounces are ingested without disabling Members
   **And** disable logic remains owned by story 4 (daily hard/soft bounce process)

## Boundaries

- Must not implement: correlation ladder, streak counting, or Member Disabled (stories 3–4).
- Must not implement: bounce mailbox create/edit UI or credential storage (epic-catalogs-parameters).
- Must not implement: mail-to-send inbox mappings (epic-mail-to-send); only bounce catalog mailboxes here.
- Out: POP3; external spam scoring (epic Boundaries).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-bounce-processing/epic-bounce-processing.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-12, AD-13 schedules)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Disable one mailbox in catalog and confirm poll skips it without failing the whole tick.
