---
type: epic
title: "Ops, Attention & audit close"
parent: initiative-java-list-mailer
covers: [R8, R25, R33]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md, _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/]
after: []
refined: true
risk: medium
---

# Ops, Attention & audit close

## Description

Completes cross-cutting operator observability: Ops Logs (send / bounce poll / MTS poll) with Parameters retention; Attention inbox by role with highlighted ops errors; append-only Audit with retention. Confirms C8 detail-page IA.

## Outcome

Admins and list managers see actionable Attention and durable audit without secret leakage.

## Requirements

R1 (parent R8): Attention inbox for human action; Admin sees all including highlighted ops errors; List managers see only their Lists/Members; List senders have no Attention inbox.

R2 (parent R25): Ops Logs for send / bounce poll / mail-to-send poll; errors on Admin Attention; retention via Parameters (default 90 days).

R3 (parent R33): Single append-only Audit log; no raw secrets; Admin all / LM member-scoped; retention default 365 days; includes Campaign status changes (Queue, Stop, Requeue, Finished, list added for Finished Requeue).

R4 (parent C8): Detail pages for List, Member, Campaign, inbox, bounce mailbox, sending path, footer, user, ops log are reached from indexes/deep links — not as primary sidebar destinations (Compose and Ops Logs index as already specified).

## Constraints

C1 (parent C6): Signing secrets and mailbox/path credentials never written raw into Audit.

C2 (architecture AD-15): Ops log retention (and audit retention if parameterized) read from Parameters seeds/defaults — not hard-coded only in this slice.

C3: Earlier epics may stub hooks; this epic makes Ops / Attention / Audit complete and verified end-to-end — must not reopen campaign or bounce algorithms.

## Done when

1. Ops logs cover send, bounce poll, and MTS poll; retention via Parameters (default 90d).
2. Attention: Admin all + highlighted ops errors; LM scoped; list senders have none.
3. Audit append-only; no raw secrets; Admin all / LM member-scoped; retention default 365d; Campaign Stop/Requeue/Finished events visible.
4. Sidebar IA: detail pages via index/deep link, not primary nav (C8) — verified against Clearwater UX.

## Boundaries

- Earlier epics may stub ops/Attention/audit hooks; this epic makes them complete and verified.
- Does not reopen campaign or bounce algorithms.
- Parameter values (retention, MFA flag) owned by catalogs; this epic consumes retention.
- Out: external SIEM export; real-time push notifications.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R8, R25, R33, C6, C8
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-15, AD-16 attention/ops-log/audit
- ux — `_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/`

## Notes

- Waits on epic-unsubscribe-consent, epic-bounce-processing, and epic-mail-to-send because: their ops/Attention/audit events must exist to close.
- Decision: 2026-10-25 — Refine from parent R8/R25/R33 + C8; UX companion adopted for IA verification only (validation findings stay UX Update, not this epic’s Requirements).
- Decision: 2026-10-25 — Audit must cover R35 Campaign status changes (Stop/Requeue/Finished/list-add).
- Assumption: Retention defaults 90d ops / 365d audit match Parameters seeds from catalogs epic.

- Decision: 2026-10-25 — Stories planned. Tracer: 9.1 Ops Logs complete with retention. Refactor sweep included. Closing e2e suite after sweep.
