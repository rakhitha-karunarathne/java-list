---
id: 5
type: story
title: "Refactor sweep"
parent: epic-unsubscribe-consent
covers: []
after: [1, 2, 3, 4]
hitl: false
risk: low
refined: true
---

# Refactor sweep

## Description

Cleanup-only pass across unsubscribe, consent, and notice-mail integration after stories 1–4: remove duplication, align naming with AD-17/AD-18, and drop dead code. No new product scope. HTTPS unsubscribe tracer, List-Unsubscribe-Post, consent approve, and Notice-path mail smokes must still pass unchanged.

## Requirements

1. **Given** stories 1–4 are delivered (R1–R4), **When** refactor touches unsubscribe or consent code paths, **Then** behavior of `/unsubscribe/<token>`, List-Unsubscribe-Post, `/approve/<token>`, and Notice-path consent mail remains functionally identical (R1, R2, R3, R4).
2. **Given** refactor removes or consolidates helpers (R1, R2), **When** the story 1 GreenMail tracer and story 3 consent flow are re-run, **Then** they pass without adding requirements or endpoints (R1, R2).
3. **Given** Audit emission from this epic (C2), **When** refactor adjusts logging or event shapes, **Then** no raw signing keys, full opaque tokens, or mailbox secrets appear in Audit payloads (C2 — failure guard).
4. **Given** membership update handlers (C3), **When** refactor edits unsub or clear-unsub code, **Then** neither path toggles global Member Enabled/Disabled (C3).

## Boundaries

- Must not change: functional requirements, public URL shapes, or token contracts (AD-17, AD-18).
- Must not change: epic-campaigns-send per-send token issuance or list-set snapshot rules.
- Out: new features, preference center, Audit UI (epic-ops-attention-audit).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-unsubscribe-consent/epic-unsubscribe-consent.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
