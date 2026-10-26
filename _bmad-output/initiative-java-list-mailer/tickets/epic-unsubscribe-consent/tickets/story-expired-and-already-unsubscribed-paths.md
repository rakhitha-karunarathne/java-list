---
id: 4
type: story
title: "Expired and already-unsubscribed paths"
parent: epic-unsubscribe-consent
covers: [R1, R2]
after: [2, 3]
hitl: false
risk: medium
refined: true
---

# Expired and already-unsubscribed paths

## Description

Harden public unsubscribe and consent approve flows for repeat visits, stale tokens, and expiry. Members who are already unsubscribed or who follow expired approve links must see clear outcomes without double-apply damage, membership corruption, or spurious Audit noise. Global Member Enabled/Disabled stays owned by bounce processing, not these paths.

## Requirements

1. **Given** M is already unsubscribed from every List in a valid unsubscribe token’s scope (R1), **When** M GETs or POSTs `/unsubscribe/<token>` again (same or newly issued valid token), **Then** membership remains unsubscribed, global Enabled/Disabled is unchanged, and the UI/message reflects already-unsubscribed without harmful side effects (R1, C3).
2. **Given** an approve token past default 14-day expiry (R2), **When** M visits `/approve/<token>`, **Then** unsubscribed state is not cleared, the outcome explains expiry, and no destructive membership mutation occurs (R2 — failure path).
3. **Given** an invalid unsubscribe token (R4), **When** GET or List-Unsubscribe-Post hits `/unsubscribe/<token>`, **Then** behavior matches story 1 safe failure even if M was previously unsubscribed via a valid token (R1, R4 — failure path).
4. **Given** M already cleared unsub via a successful approve (R2), **When** M reuses the same `/approve/<token>`, **Then** the handler is idempotent (clear already-cleared state or stable success) without duplicate damaging writes or contradictory Audit (R2).
5. **Given** any outcome on these edge paths (R1, R2), **When** Audit events are emitted, **Then** payloads contain no raw signing material, full tokens, or other secrets (C2).

## Boundaries

- Must not change: first-success unsubscribe or first-success approve implementations except for idempotent guards (stories 1, 3).
- Must not change: token crypto or Public base URL rules (R4, C1).
- Must not introduce preference center, public subscribe, or operator bypass of consent (R2).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-unsubscribe-consent/epic-unsubscribe-consent.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
