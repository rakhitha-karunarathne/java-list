---
type: epic
title: "Mail-to-send"
parent: initiative-java-list-mailer
covers: [R29, R30, R31, R32]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md]
after: []
refined: true
risk: high
---

# Mail-to-send

## Description

IMAP mail-to-send inboxes, authorized senders, mappings, From-domain issues, auth/spam/DMARC outcome matrix, drop log and global blocks. Authorized path creates campaigns; strangers stay silent/logged.

## Outcome

Ingested mail becomes campaigns for authorized senders without courtesy rejects to strangers — initiative Done when #2.

## Requirements

R1 (parent R29): IMAP-only mail-to-send; index + inbox detail; authorized senders; mappings; substitute From domain rule; footer override.

R2 (parent R30): From-domain Issues (not Drop log); all-or-nothing; hold or delete-and-notify.

R3 (parent R31): Auth then spam-header-if-present; DMARC fail = reject treatment; outcome matrix (silent / notify / auto-block rules).

R4 (parent R32): Drop log + global blocks; auto-block sender-only after N/day; no stranger courtesy reject.

R5 (architecture AD-12): Reuses mailbox-query from bounce epic for IMAP poll; does not re-implement stream/poll primitives.

R6 (parent R9): Catalog Used By includes MTS mappings so referenced configs cannot be deleted while mapped.

R7 (parent R35): Authorized accept creates a Campaign via campaigns ports in **Queued** (skip Draft); Stop/Requeue/Finished list-add remain campaigns epic.

## Constraints

C1 (parent C8): Inbox detail is reached from the MTS index — not a primary sidebar destination.

C2 (architecture AD-12): One inbox at a time / low-memory poll rules apply to MTS inboxes as to bounce.

C3: Strangers stay silent or logged — never a courtesy reject that confirms the address.

## Done when

1. MTS index + inbox detail; mappings; substitute From domain; footer override.
2. From-domain Issues hold or delete-and-notify (all-or-nothing).
3. Auth then spam-header; DMARC fail = reject treatment; outcome matrix honored.
4. Drop log + global blocks; auto-block sender-only after N/day.
5. Authorized path creates a Campaign in Queued via campaigns ports; stranger path leaves no courtesy bounce.

## Boundaries

- Reuses mailbox-query from bounce epic; does not re-own bounce correlation.
- Campaign entity and lifecycle (Stop/Requeue) remain campaigns slice; MTS only creates/enqueues via ports.
- Issues / Drop / Blocks operator UIs ship here; Attention highlighting closes in epic-ops-attention-audit.
- Out: POP3; external spam service / AI spam scoring.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R29–R32, R9, C8
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-12, AD-16 send-to-mail

## Notes

- Waits on epic-campaigns-send because: campaigns and smtp-adapter.
- Waits on epic-bounce-processing because: mailbox-query utility.
- Decision: 2026-10-25 — Refine from parent R29–R32; Issues≠Drop kept explicit so stories do not merge the surfaces.
- Decision: 2026-10-25 — MTS-created Campaigns enter Queued per R35 (skip Draft).

- Decision: 2026-10-25 — Stories planned. Tracer: 8.1 MTS inbox ? Queued Campaign. Refactor sweep included.
- Decision: 2026-10-26 — All stories pulled and refined with full acceptance criteria.
