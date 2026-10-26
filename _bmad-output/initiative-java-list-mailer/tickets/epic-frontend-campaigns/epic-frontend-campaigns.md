---
type: epic
title: "Frontend Campaigns & Compose"
parent: initiative-java-list-mailer
covers: [R17, R18, R19, R20, R35]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md]
after: [epic-frontend-shell, epic-frontend-lists-members]
refined: true
risk: medium
---

# Frontend Campaigns & Compose

## Description

Clearwater **Campaigns** index (status filter) and **Campaign detail** (Stop / Requeue / Add List, stats). Elevate existing Compose tracer to Clearwater patterns: preview tabs, test send dialog, queue, multi-list order.

## Outcome

Sam can compose, test, queue, stop, and requeue campaigns fully in the SPA.

## Requirements

R1: Campaigns index with Draft · Queued · Sending · Finished · Stopped filter/badges.

R2: Campaign detail — status actions, Add List for Finished requeue gate, opens/sent stats as API provides.

R3: Compose Clearwater — HTML/text preview sync, multi-list select/order, Save Draft, Queue, Test send.

R4: C8 — Campaign detail not a sidebar primary destination.

## Constraints

C1: Campaign lifecycle rules stay in backend; UI disables illegal actions with helpers.

C2: Test send ignores bounce-disable — copy must say so.

## Done when

1. Index + detail + Compose support Stop/Requeue/Finished list-add flows.
2. Test send and Queue work from Compose.
3. Status never color-only (badge text + color).

## Boundaries

- Image fetch SSRF remains backend; UI only surfaces errors.
- Out: click tracking UI; template library.

## References

- tickets — epic-campaigns-send
- ux — EXPERIENCE Flows 3–4

## Notes

- Decision: 2026-10-26 — Frontend Campaigns & Compose epic.
