---
type: epic
title: "Bounce processing"
parent: initiative-java-list-mailer
covers: [R26, R27]
sources: []
companions: []
after: []
refined: false
risk: high
---

# Bounce processing

## Description

IMAP bounce mailbox polling (one inbox at a time, low memory) via mailbox-query; correlation ladder; daily hard/soft disable walks; uncorrelated never auto-disable (C7). Ops/Attention hooks for noise and errors.

## Outcome

Bounce-driven global disable works without punishing uncorrelated noise — initiative Done when #3.

## Done when

1. Bounce mailboxes poll on schedule; stream processing; one inbox at a time.
2. Correlation uses send-queue contract fields; uncorrelated → log + Attention, no disable/streak.
3. Daily hard/soft process with holdback disables per limits.
4. VERP respected when Parameters toggle is on (owned by catalogs epic).

## Boundaries

- Owns mailbox-query introduction for IMAP (mail-to-send waits on this).
- Does not implement MTS auth/spam rules.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R26–R27, C7, R4
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-12, AD-25, AD-16 bounce/mailbox-query

## Notes

- Waits on epic-campaigns-send because: send-queue correlation contract.
