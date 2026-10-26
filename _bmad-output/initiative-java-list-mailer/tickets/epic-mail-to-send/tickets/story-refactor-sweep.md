---
id: 6
type: story
title: "Refactor sweep"
parent: epic-mail-to-send
after: [1, 2, 3, 4, 5]
hitl: false
risk: low
refined: true
---

# Refactor sweep

## Description

Cleanup and consolidation across the Mail-to-send slice after stories 1–5 land. No new product scope: refactors only, with regression checks on the authorized MTS→Campaign Queued tracer and stranger silent/Drop log paths.

## Requirements

1. **Scope guard**
   **Given** this story’s diff
   **When** reviewed against epic boundaries
   **Then** there are no new user-facing gates, surfaces, or Parameters beyond what R1–R7 and parent FR-28–FR-31 already require
   **And** any behavior change is justified as deduplication or naming aligned with Mail-to-send vocabulary

2. **Authorized tracer still passes (R1, R7)**
   **Given** the GreenMail IMAP authorized-sender fixture from story 1
   **When** refactor completes
   **Then** ingest still creates a Campaign in **Queued** (skip Draft) for mapped Lists with substitute From and footer override applied

3. **Stranger silent path still passes (R3, R4)**
   **Given** stranger and counted-reject fixtures from stories 3–4
   **When** refactor completes
   **Then** silent delete, Drop log reasons, and sender auto-block after N/day still match the outcome matrix
   **And** no courtesy reject is emitted to strangers

4. **Issues ≠ Drop log preserved (R2, R4)**
   **Given** From-domain Issue and Drop log fixtures
   **When** refactor completes
   **Then** From-domain Issues still do not use Drop log or auto-block counters
   **And** Drop log rows still exclude From-domain Issue semantics

5. **Used By guards intact (R6)**
   **Given** a Sending path and Footer referenced by MTS mappings
   **When** refactor completes
   **Then** catalog delete remains blocked with Used By links to inbox/mapping

## Boundaries

- No new dependencies, epics, or cross-slice features.
- Attention highlighting and ops polish may remain epic-ops-attention-audit.
- If a refactor would change acceptance criteria, split a new story instead of doing it here.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-mail-to-send/epic-mail-to-send.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Run story 1 tracer plus one matrix fixture and one stranger auto-block fixture before merge.
