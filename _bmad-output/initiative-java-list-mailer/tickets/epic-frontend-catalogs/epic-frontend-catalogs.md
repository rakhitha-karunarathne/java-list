---
type: epic
title: "Frontend catalogs, Parameters & Users"
parent: initiative-java-list-mailer
covers: [R3, R4, R5, R6, R7, R9]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md]
after: [epic-frontend-shell]
refined: true
risk: medium
---

# Frontend catalogs, Parameters & Users

## Description

Admin/operator UIs for Sending paths, Bounce mailboxes, Footers, Notice sending path, Parameters, and Users — index → detail, Used By, delete-blocked messaging. Consumes catalogs/users APIs from product epics.

## Outcome

Ava can stand up catalogs, Parameters, and Users entirely from the SPA without API tools.

## Requirements

R1: Sending paths index + detail; Used By; delete blocked while referenced.

R2: Bounce mailboxes index + detail (IMAP/poll); detail not in sidebar.

R3: Footers index + detail via row; Used By.

R4: Notice sending path designation UI (single Notice path).

R5: Parameters Admin page — stacked fields with purpose/values/range descriptions.

R6: Users index + detail (role, grants, Security MFA panel).

## Constraints

C1: C8 — details from index/deep link only.

C2: No raw secrets displayed beyond API-safe fields (password write-only).

C3: UI-only; no catalog algorithm changes.

## Done when

1. Admin can CRUD paths, bounce mailboxes, footers, notice path, parameters, users from SPA.
2. Used By blocks delete with navigable links.
3. User Security MFA enroll/unenroll reachable from User detail only.

## Boundaries

- List/Member/Campaign/MTS bodies are other FE epics.
- Out: SIEM export UI.

## References

- tickets — epic-catalogs-parameters, epic-users-access
- ux — EXPERIENCE Flows 1–2; DESIGN

## Notes

- Decision: 2026-10-26 — Frontend epic for catalog surfaces after APIs closed.
