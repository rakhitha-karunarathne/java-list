---
id: 5
type: story
title: "Refactor sweep"
parent: epic-bounce-processing
after: [1, 2, 3, 4]
hitl: false
risk: low
refined: true
---

# Refactor sweep

## Description

Internal cleanup after bounce feature stories land: naming, duplication, and dead code only. No new operator-visible behavior, Parameters, or epic scope. **mailbox-query** public contract must remain stable for epic-mail-to-send.

## Requirements

1. **No new scope** (R1–R5)
   **Given** the diff for this story
   **When** reviewed against epic Requirements and Constraints (incl. C7 uncorrelated safety)
   **Then** there are no new features, API fields, or UI flows beyond refactors
   **And** bounce disable still does not mark list unsubscribe

2. **Tracer and poll smoke still pass** (R3)
   **Given** the test profile with GreenMail
   **When** mailbox-query tracer (story 1) and bounce mailbox poll ingest (story 2) smokes run
   **Then** they pass with the same expectations as before the refactor
   **And** one-inbox-at-a-time behavior is unchanged

3. **Correlation safety smoke still passes** (R1, R4)
   **Given** correlated and uncorrelated bounce fixtures
   **When** correlation smoke from story 3 runs after the sweep
   **Then** matched bounces link to queue rows; uncorrelated produce Attention without disable or streak change
   **And** AD-25 safety paths remain covered

4. **Daily disable smoke still passes** (R2)
   **Given** fixtures that hit hard or soft limit
   **When** daily bounce-process smoke from story 4 runs
   **Then** Member Disabled + disable_reason still occur at limits; holdback behavior unchanged

5. **mailbox-query API stable for MTS** (R3)
   **Given** the documented mailbox-query port after refactor
   **When** compared to pre-sweep contract notes for epic-mail-to-send
   **Then** no breaking signature or lifecycle change without explicit epic coordination
   **And** bounce-specific types are not forced into the shared utility package

## Boundaries

- Must not change: acceptance behavior defined in stories 1–4 except bug fixes explicitly tagged in the sweep PR description.
- Must not change: Flyway schema unless purely cosmetic rename with no behavior change (avoid in sweep).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-bounce-processing/epic-bounce-processing.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-12, AD-16)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
