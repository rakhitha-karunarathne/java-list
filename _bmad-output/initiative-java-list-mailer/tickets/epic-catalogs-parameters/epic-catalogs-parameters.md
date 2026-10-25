---
type: epic
title: "Catalogs & Parameters"
parent: initiative-java-list-mailer
covers: [R3, R4, R5, R6, R7, R9, R28]
sources: []
companions: []
after: []
refined: false
risk: high
---

# Catalogs & Parameters

## Description

Admin catalogues sending paths, bounce mailboxes, and footers; designates the Notice sending path; manages Parameters (non-credential tunables including VERP default off and retention defaults). Shared configs expose Used By and block delete while referenced.

## Outcome

Ava can stand up system catalogs and Parameters before any list or campaign work.

## Done when

1. Sending paths, bounce mailboxes, and footers CRUD with Used By / delete guards.
2. One Notice sending path is configurable.
3. Parameters Admin UI/API covers product defaults (ops log 90d, VERP off, etc.).
4. Credentials stay on paths/mailboxes; secrets never in Parameters.

## Boundaries

- Lists, campaigns, and bounce *processing* are sibling epics.
- smtp-adapter / mailbox-query implementations come with send/bounce epics; this epic stores catalog credentials only.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R3–R7, R9, R28, C6
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-15, AD-16

## Notes

- Waits on epic-users-access because: Admin auth and roles.
