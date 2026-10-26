---
type: epic
title: "Frontend shell & auth"
parent: initiative-java-list-mailer
covers: [R1, C8]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md, _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md]
after: [epic-ops-attention-audit]
refined: true
risk: medium
---

# Frontend shell & auth

## Description

Replace the thin tracer SPA chrome with Clearwater shell: branded layout, role-aware sidebar (indexes/tools only), sign-in with email + MFA step-up, session in sessionStorage. Establishes C8 detail-not-nav for all later frontend epics.

## Outcome

Operators land in a Clearwater admin shell with correct nav by role; detail pages never appear as primary sidebar destinations.

## Requirements

R1: Clearwater tokens, typography, and chrome per DESIGN.md (List Mailer product name).

R2: Sidebar IA per EXPERIENCE table; hide categories when role hides all items; C8 detail-not-nav.

R3: Sign in with email + password; MFA TOTP/backup step-up when required; JWT in sessionStorage.

R4: Responsive: desktop sidebar; narrow → drawer.

## Constraints

C1: Must not invent new backend APIs — consume existing `/api/auth/**` and role claims.

C2: Must not put entity detail pages in primary sidebar.

C3: Light theme only; no dark mode toggle (UX ban).

## Done when

1. Clearwater shell renders with role-aware sidebar matching EXPERIENCE IA categories present for Admin.
2. Login + MFA step-up works against existing auth API.
3. Nav regression check: no detail destinations in sidebar.

## Boundaries

- Page bodies for catalogs/lists/campaigns/ops ship in later frontend epics; this epic may leave placeholder index routes.
- Out: plugin marketplace UI; pixel-perfect mobile polish beyond drawer + scroll-x.

## References

- ux — EXPERIENCE.md (IA, Flow 2, Flow 5), DESIGN.md
- architecture — AD-4 SPA, AD-19 sessionStorage JWT

## Notes

- Decision: 2026-10-26 — Frontend sliced into new epics after product epics 1–9 closed APIs; shell first.
- Decision: 2026-10-26 — Clearwater clickable mock approved; shell/nav must match mock page map (spines win on conflict).
