---
id: 9
type: story
title: "Refactor sweep"
parent: epic-campaigns-send
after: [1, 2, 3, 4, 5, 6, 7, 8]
hitl: false
risk: low
refined: true
---

# Refactor sweep

## Description

Internal cleanup after feature stories land: naming, duplication, and dead code only. No new operator-visible behavior, parameters, or epic scope.

## Requirements

1. **No new scope** (R1–R10)
   **Given** the diff for this story
   **When** reviewed against epic Requirements and Constraints
   **Then** there are no new features, API fields, or UI flows beyond refactors
   **And** extract-marked package boundaries for tracking and send-queue remain as architecture AD-22 describes

2. **Tracer smoke still passes** (R8)
   **Given** the test profile with GreenMail
   **When** the compose Draft → queue → send tracer from story 1 runs
   **Then** it still passes without criterion changes

3. **Lifecycle and pixel smokes still pass** (R8, R7)
   **Given** existing automated or manual smokes for Stop/Requeue (story 4) and open pixel (story 8)
   **When** executed after the sweep
   **Then** they pass with the same expectations as before the refactor

## Boundaries

- Must not change: acceptance behavior defined in stories 1–8 except bug fixes explicitly tagged in the sweep PR description.
- Must not change: Flyway schema unless purely cosmetic rename with no behavior change (avoid in sweep).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
