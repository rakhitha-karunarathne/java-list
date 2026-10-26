---
type: epic
title: "Frontend Ops, MTS & Attention polish"
parent: initiative-java-list-mailer
covers: [R8, R25, R29, R30, R31, R32]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md]
after: [epic-frontend-shell, epic-frontend-catalogs]
refined: true
risk: medium
---

# Frontend Ops, MTS & Attention polish

## Description

Complete operator UIs for Mail-to-send (inbox detail, senders, mappings, Drop log, From-domain Issues, blocks), and polish Ops Logs / Attention / Audit to Clearwater with role rules and deep links. Bounce mailbox index already started in catalogs FE — MTS-focused here.

## Outcome

UJ-4 and Attention/ops paths are operable in SPA without API tools; Issues ≠ Drop visually.

## Requirements

R1: MTS index + inbox detail (senders, mappings, substitute From, footer override) — C8.

R2: From-domain Issues UI distinct from Drop log; clear/resolve actions.

R3: Drop log (per inbox filter + combined) + global blocks actions.

R4: Ops Logs / Attention / Audit Clearwater polish; Admin ops highlights; LM scoped; sender forbidden.

## Constraints

C1: No MTS/auth matrix logic in UI — display API outcomes only.

C2: Issues never labeled as Drop reasons.

C3: C8 detail-not-nav for inbox and ops-log detail.

## Done when

1. Admin can configure MTS inbox/mappings and view Drop/Issues/blocks in SPA.
2. Attention/Ops/Audit match role rules with Clearwater treatment.
3. Issues ≠ Drop surfaces are visually distinct.

## Boundaries

- Bounce process algorithms stay backend.
- Out: SIEM; realtime push.

## References

- tickets — epic-mail-to-send, epic-ops-attention-audit
- ux — EXPERIENCE Attention highlight tokens

## Notes

- Decision: 2026-10-26 — Frontend ops/MTS epic after APIs closed.
- Decision: 2026-10-26 — Clearwater clickable mock approved; FE e2e walkthrough uses mock as reference.
