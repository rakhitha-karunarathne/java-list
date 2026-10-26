---
id: 1
type: story
title: "mailbox-query IMAP utility tracer"
parent: epic-bounce-processing
covers: [R3]
after: [epic-campaigns-send]
hitl: true
risk: high
refined: true
---

# mailbox-query IMAP utility tracer

## Description

Introduce the shared **mailbox-query** utility for IMAP: connect, list, and stream/process messages incrementally with low memory. V1 polls **one inbox at a time** per poller instance. Tracer proves incremental fetch against GreenMail; document the port/contract so epic-mail-to-send can reuse the same utility without bounce-specific logic leaking in.

## Requirements

1. **Incremental IMAP fetch** (R3)
   **Given** a GreenMail IMAP inbox with several messages (test profile)
   **When** mailbox-query runs a poll pass against that inbox
   **Then** each message is fetched and handed to a consumer callback without loading the full mailbox into memory
   **And** the pass completes without OOM-style batching of entire message bodies in one collection

2. **One inbox at a time** (R3)
   **Given** two configured IMAP inboxes (e.g. two bounce mailboxes or two test inboxes)
   **When** a bounce or tracer poller runs on a single JVM
   **Then** only one inbox poll is active at a time for that poller type
   **And** the second inbox starts only after the first pass finishes or aborts cleanly

3. **Tracer consumer processes at least one message** (R3)
   **Given** a seeded bounce-style message in GreenMail
   **When** the tracer invokes mailbox-query with a test consumer
   **Then** the consumer receives parsed envelope/metadata sufficient for downstream correlation (headers, subject, recipient hints)
   **And** the message can be marked handled or left for idempotent re-read per utility contract

4. **Credentials and connection errors surface** (R3)
   **Given** invalid IMAP credentials or unreachable host
   **When** mailbox-query attempts connect
   **Then** the failure is returned to the caller with a clear error (no silent hang)
   **And** the caller can record Ops Logs / Attention without crashing the JVM

5. **Reusable contract for mail-to-send** (R3)
   **Given** mailbox-query is introduced in this epic
   **When** a developer reads the documented port (interface + expected consumer lifecycle)
   **Then** mail-to-send can poll its inboxes using the same utility with only slice-specific consumers
   **And** bounce slice does not embed MTS or campaign rules inside mailbox-query

## Boundaries

- Must not implement: bounce correlation, daily bounce-process, Member disable, or bounce mailbox Admin CRUD (catalogs epic).
- Must not implement: POP3 or parallel multi-inbox polls within one poller (epic C2, architecture AD-12).
- Must not change: send-queue correlation field writes (epic-campaigns-send); this story only consumes IMAP.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-bounce-processing/epic-bounce-processing.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-12, AD-16, AD-27 GreenMail)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Run tracer with a mailbox larger than default fetch batch size (if any) to confirm streaming behavior.
