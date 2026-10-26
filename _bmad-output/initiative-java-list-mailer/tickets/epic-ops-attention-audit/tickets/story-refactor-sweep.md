---
id: 5
type: story
title: "Refactor sweep"
parent: epic-ops-attention-audit
covers: []
after: [1, 2, 3, 4]
hitl: false
risk: low
refined: true
---

# Refactor sweep

## Description

Cleanup-only pass across **ops-log**, **attention**, and **audit** slices after stories 1–4: dedupe helpers, align naming with AD-16 package boundaries, remove dead code. No new operator-visible behavior, Parameters keys, or epic scope. Ops Logs tracer, Attention role scoping, Audit campaign-status events, and C8 nav smokes must pass unchanged.

## Requirements

1. **No new scope** (R1, R2, R3, R4)
   **Given** the diff for this story
   **When** reviewed against epic Requirements and Constraints
   **Then** there are no new features, API fields, sidebar destinations, or retention defaults beyond refactors
   **And** slice boundaries remain ops-log, attention, audit per AD-16

2. **Ops Logs tracer smoke** (R2)
   **Given** the test profile GreenMail failure tracer from story 1
   **When** re-run after refactor
   **Then** Ops Logs row and Admin Attention hook still occur with the same expectations

3. **Attention role smoke** (R1)
   **Given** fixtures for Admin, List manager (single List), and List sender
   **When** Attention nav and API are exercised
   **Then** Admin sees ops highlights, List manager is scoped, List sender has no Attention access

4. **Audit smoke** (R3, C1)
   **Given** Stop/Requeue and list-add flows from story 3
   **When** re-run after refactor
   **Then** Audit rows still record Queue/Stop/Requeue/Finished/list-add
   **And** no raw secrets appear in Audit payloads (C1)

5. **Retention jobs unchanged behavior** (R2, R3, C2)
   **Given** ops and audit retention Parameters
   **When** purge jobs run in test
   **Then** cutoff still derives from Parameters port, not new hard-coded windows (C2)

6. **C8 nav unchanged** (R4)
   **Given** story 4 IA checklist
   **When** re-run after refactor touching front-end shell
   **Then** detail pages remain off primary sidebar

## Boundaries

- Must not change: acceptance behavior in stories 1–4 except bug fixes tagged in the sweep PR description.
- Must not change: Flyway schema unless purely cosmetic rename with no behavior change (avoid in sweep).
- Out: performance optimization projects unrelated to observability slices.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-ops-attention-audit/epic-ops-attention-audit.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-16)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
