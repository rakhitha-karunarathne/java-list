---
type: epic
title: "Lists & members"
parent: initiative-java-list-mailer
covers: [R10, R11, R12, R13, R16, R17]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md]
after: []
refined: true
risk: high
---

# Lists & members

## Description

Authorized users CRUD Lists (path, optional footer/overrides); bulk-paste members; Members index and detail with global enable/disable distinct from per-list unsubscribe; re-enable rules with confirm.

## Outcome

List managers can build and maintain membership ready for campaigns.

## Requirements

R1 (parent R10): Authorized Users CRUD Lists they manage; each List maps to one Sending path; optional footer, spam threshold, payload/image/TTL overrides; no public/private flag in V1.

R2 (parent R11): List page bulk-paste add Members; skips invalid, in-paste dupes, already-on-list, unsubscribed; shows counts; List senders cannot.

R3 (parent R12): Member global Enabled/Disabled; Disabled skipped by Campaign queue; `disable_reason` shown; bounce disable ≠ unsubscribe.

R4 (parent R13): Re-enable manual or re-add-to-list with confirm (clears streaks) unless unsubscribed on that List; opens never auto re-enable.

R5 (parent R16): Searchable Members index in sidebar; Admin all / LM scoped; open → Member page; detail not a sidebar destination.

R6 (parent R17): Member page shows lists on/unsubscribed, status, timeline = audit filtered by Member.

## Constraints

C1 (parent C8): Detail pages for List and Member are reached from indexes/deep links — not as primary sidebar destinations (Members index and Compose remain as specified).

C2 (architecture AD-24): List and Member identities use GUID ids consistent with the kernel convention.

C3: Bounce-driven disable is applied by epic-bounce-processing; this epic owns the Member enabled flag surface and re-enable UX, and must not treat bounce disable as unsubscribe.

## Done when

1. List CRUD with sending path and optional footer/overrides; no public/private flag.
2. Bulk-paste add with skip counts; list senders cannot bulk-add.
3. Members index searchable and scoped; detail shows lists/status/timeline hook.
4. Enable/disable and re-enable rules match parent R12–R13.

## Boundaries

- Unsubscribe/consent public flows are epic-unsubscribe-consent.
- Campaign compose, status, Stop/Requeue, and attaching extra Lists to a Finished Campaign for Requeue are epic-campaigns-send (lists only supply membership).
- Audit timeline content completeness closes in epic-ops-attention-audit; this epic wires the Member-filtered view.
- Out: public subscribe / preference center; List public/private visibility flag.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R10–R13, R16–R17, C8
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-16 lists/members, AD-24

## Notes

- Waits on epic-catalogs-parameters because: sending paths and footers.
- Decision: 2026-10-25 — Refine from parent covers; bounce≠unsub kept as Constraint so stories do not conflate.

- Decision: 2026-10-25 — Stories planned. Tracer: 4.1 List CRUD with path and footer. Refactor sweep included.
