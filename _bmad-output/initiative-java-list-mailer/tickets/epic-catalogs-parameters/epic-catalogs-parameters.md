---
type: epic
title: "Catalogs & Parameters"
parent: initiative-java-list-mailer
covers: [R3, R4, R5, R6, R7, R9, R28]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md]
after: []
refined: true
risk: high
---

# Catalogs & Parameters

## Description

Admin catalogues sending paths, bounce mailboxes, and footers; designates the Notice sending path; manages Parameters (non-credential tunables including VERP default off, retention defaults, and MFA-mandatory). Shared configs expose Used By and block delete while referenced.

## Outcome

Ava can stand up system catalogs and Parameters before any list or campaign work.

## Requirements

R1 (parent R3): Sending paths (SMTP + From) are catalogued; detail shows navigable Used By; delete blocked while referenced.

R2 (parent R4): Bounce mailboxes are catalogued with per-mailbox poll (default 15 min); edit on mailbox detail from the index.

R3 (parent R5): Footers are catalogued; detail shows Used By (Lists, mappings); delete blocked while referenced; UI campaigns use List footer only.

R4 (parent R6): One Notice sending path for system/consent/admin mail; bypasses bounce-disable; Campaign queue never does.

R5 (parent R7): Admin-only Parameters hold non-credential tunables (including ops log retention default 90 days, and whether operator MFA is mandatory); each Parameter on the Admin page shows a description covering purpose, supported values, and value range; credentials on paths/mailboxes; signing secrets and MFA secrets/backup codes not in Parameters.

R6 (parent R9): Shared referenced configs cannot be deleted while in use; Used By links to referencing records.

R7 (parent R28): VERP toggle on Parameters; default off.

R8 (architecture AD-15): Public base URL (seeded in platform) is editable here as a Parameter; credentials and signing secrets remain env/secret-file only.

## Constraints

C1 (parent C6): Signing secrets and mailbox/path credentials never stored as Parameters plaintext or written raw into Audit.

C2 (parent C8): Detail pages for bounce mailbox, sending path, footer are reached from indexes/deep links — not as primary sidebar destinations.

C3 (architecture AD-15): Parameters are Admin-only; product defaults ship as seeds, not hard-coded magic in feature slices.

## Done when

1. Sending paths, bounce mailboxes, and footers CRUD with Used By / delete guards.
2. One Notice sending path is configurable.
3. Parameters Admin UI/API covers product defaults (ops log 90d, VERP off, MFA mandatory flag default off, Public base URL, etc.); each parameter shows description (purpose, supported values, value range).
4. Credentials stay on paths/mailboxes; secrets never in Parameters (incl. TOTP secrets/backup codes — those live with users).

## Boundaries

- Lists, campaigns, and bounce *processing* are sibling epics.
- smtp-adapter / mailbox-query implementations come with send/bounce epics; this epic stores catalog credentials only.
- MFA enroll/login challenge is epic-users-access; this epic only owns the mandatory flag Parameter.
- Out: plugin pack UIs; credential vault products beyond env/secret file.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R3–R7, R9, R28, C6, C8
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-15, AD-16

## Notes

- Waits on epic-users-access because: Admin auth and roles.
- Decision: 2026-10-25 — Refine from parent covers + AD-15; Parameter descriptions and MFA-mandatory flag land here.

- Decision: 2026-10-25 — Stories planned. Tracer: 3.1 Sending paths catalog. Refactor sweep included.
