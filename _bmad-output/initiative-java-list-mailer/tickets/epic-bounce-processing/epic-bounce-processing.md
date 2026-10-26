---
type: epic
title: "Bounce processing"
parent: initiative-java-list-mailer
covers: [R26, R27]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md]
after: []
refined: true
risk: high
---

# Bounce processing

## Description

IMAP bounce mailbox polling (one inbox at a time, low memory) via mailbox-query; correlation ladder; daily hard/soft disable walks; uncorrelated never auto-disable (C7). Ops/Attention hooks for noise and errors.

## Outcome

Bounce-driven global disable works without punishing uncorrelated noise — initiative Done when #3.

## Requirements

R1 (parent R26): Bounce correlation (VERP optional → token → Message-ID/headers → subject+email); uncorrelated never auto-disable / never streak; logged + Attention.

R2 (parent R27): Daily bounce-process; separate hard/soft walks + holdback; either limit disables; soft independent after init.

R3 (parent R4, architecture AD-12): Bounce mailboxes (catalogued earlier) poll on schedule via mailbox-query; stream processing; one inbox at a time; low memory.

R4 (architecture AD-25): Uncorrelated bounces must never drive auto-disable or streak reset — enforced in process logic, not only logging.

R5 (parent R28 ownership): When Parameters VERP toggle is on, correlation prefers VERP; toggle itself remains catalogs epic.

## Constraints

C1 (parent C7): Uncorrelated bounces must never drive auto-disable or streak reset.

C2 (architecture AD-10, AD-12): Sequential IO polls; one bounce inbox at a time; V1 single app instance.

C3: Bounce disable sets Member global Disabled with reason; must not mark list unsubscribe.

## Done when

1. Bounce mailboxes poll on schedule; stream processing; one inbox at a time.
2. Correlation uses send-queue contract fields; uncorrelated → log + Attention, no disable/streak.
3. Daily hard/soft process with holdback disables per limits.
4. VERP respected when Parameters toggle is on (owned by catalogs epic).
5. mailbox-query is reusable by mail-to-send (contract documented for epic 8).

## Boundaries

- Owns mailbox-query introduction for IMAP (mail-to-send waits on this).
- Does not implement MTS auth/spam rules.
- Bounce mailbox CRUD is epic-catalogs-parameters; this epic consumes poll config.
- Out: POP3; external spam scoring.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R26–R27, C7, R4, R28
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-12, AD-25, AD-16 bounce/mailbox-query

## Notes

- Waits on epic-campaigns-send because: send-queue correlation contract.
- Decision: 2026-10-25 — Refine from parent R26–R27 + AD-12/25; mailbox-query introduced here for MTS reuse.

- Decision: 2026-10-25 — Stories planned. Tracer: 7.1 mailbox-query IMAP utility. Refactor sweep included.

- Decision: 2026-10-26 — All stories pulled and refined with full acceptance criteria.
