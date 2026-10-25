---
type: epic
title: "Lists & members"
parent: initiative-java-list-mailer
covers: [R10, R11, R12, R13, R16, R17]
sources: []
companions: []
after: []
refined: false
risk: high
---

# Lists & members

## Description

Authorized users CRUD Lists (path, optional footer/overrides); bulk-paste members; Members index and detail with global enable/disable distinct from per-list unsubscribe; re-enable rules with confirm.

## Outcome

List managers can build and maintain membership ready for campaigns.

## Done when

1. List CRUD with sending path and optional footer/overrides; no public/private flag.
2. Bulk-paste add with skip counts; list senders cannot bulk-add.
3. Members index searchable and scoped; detail shows lists/status/timeline hook.
4. Enable/disable and re-enable rules match R12–R13.

## Boundaries

- Unsubscribe/consent public flows are epic-unsubscribe-consent.
- Campaign compose is epic-campaigns-send.
- Detail pages are not primary sidebar destinations (C8).

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R10–R13, R16–R17, C8
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-16 lists/members

## Notes

- Waits on epic-catalogs-parameters because: sending paths and footers.
