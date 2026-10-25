---
type: epic
title: "Ops, Attention & audit close"
parent: initiative-java-list-mailer
covers: [R8, R25, R33]
sources: []
companions: []
after: []
refined: false
risk: medium
---

# Ops, Attention & audit close

## Description

Completes cross-cutting operator observability: Ops Logs (send / bounce poll / MTS poll) with Parameters retention; Attention inbox by role with highlighted ops errors; append-only Audit with retention. Confirms C8 detail-page IA.

## Outcome

Admins and list managers see actionable Attention and durable audit without secret leakage.

## Done when

1. Ops logs cover send, bounce poll, and MTS poll; retention via Parameters (default 90d).
2. Attention: Admin all + highlighted ops errors; LM scoped; list senders have none.
3. Audit append-only; no raw secrets; Admin all / LM member-scoped; retention default 365d.
4. Sidebar IA: detail pages via index/deep link, not primary nav (C8).

## Boundaries

- Earlier epics may stub ops/Attention/audit hooks; this epic makes them complete and verified.
- Does not reopen campaign or bounce algorithms.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R8, R25, R33, C6, C8
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-15, AD-16 attention/ops-log/audit
- ux — `_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/`

## Notes

- Waits on epic-unsubscribe-consent, epic-bounce-processing, and epic-mail-to-send because: their ops/Attention/audit events must exist to close.
