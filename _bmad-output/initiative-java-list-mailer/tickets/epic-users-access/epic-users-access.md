---
type: epic
title: "Users & access"
parent: initiative-java-list-mailer
covers: [R1, R2]
sources: []
companions: []
after: []
refined: false
risk: high
---

# Users & access

## Description

Install creates the initial Admin. Admins create/disable users with local credentials; Admin / List manager / List sender roles and per-list manage/send grants enforce scoped access via real JWT.

## Outcome

Only authorized operators reach operator APIs; list grants take effect immediately when changed.

## Done when

1. Fresh install yields a usable Admin.
2. Admin can create/disable users and assign roles.
3. Per-list grants are editable on user detail and apply immediately.
4. JWT auth guards `/api` for real sessions (sessionStorage).

## Boundaries

- Catalogs, lists, and campaigns stay in later epics.
- Sibling: epic-platform-kernel owns the shell this epic authenticates against.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R1–R2
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-6, AD-16 users, AD-19

## Notes

- Waits on epic-platform-kernel because: app shell, MariaDB, JWT stub.
