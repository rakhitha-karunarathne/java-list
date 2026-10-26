---
type: epic
title: "Frontend Lists & Members"
parent: initiative-java-list-mailer
covers: [R10, R11, R12, R13, R14, R15, R16]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md]
after: [epic-frontend-shell]
refined: true
risk: medium
---

# Frontend Lists & Members

## Description

SPA for Lists index/detail (settings, bulk add) and searchable Members index/detail (status, lists, enable/disable, consent request). Role-scoped lists for List managers.

## Outcome

Operators manage membership entirely in-browser per phpList-like paths without API tools.

## Requirements

R1: Lists index + detail; sending path/footer bindings; bulk paste add members with skip counts.

R2: Members searchable index (Admin all / LM scoped).

R3: Member detail — status, list memberships, enable/disable with confirm rules, consent clear-unsub request.

R4: C8 — List/Member detail never primary sidebar destinations.

## Constraints

C1: Consume lists/members/consent APIs only.

C2: No inventing public subscribe UI (V1 out).

## Done when

1. List CRUD + bulk add works in SPA.
2. Members search + detail + disable/enable + consent request work for Admin/LM scopes.
3. Detail-not-nav holds.

## Boundaries

- Campaign compose is FE campaigns epic.
- Out: preference center.

## References

- tickets — epic-lists-members, epic-unsubscribe-consent
- ux — EXPERIENCE Flow 5

## Notes

- Decision: 2026-10-26 — Frontend Lists & Members epic.
