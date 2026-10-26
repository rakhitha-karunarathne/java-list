---
id: 3
type: story
title: "Consent clear-unsub via notice-mail"
parent: epic-unsubscribe-consent
covers: [R2, R3]
after: [1]
hitl: false
risk: high
refined: true
---

# Consent clear-unsub via notice-mail

## Description

Clearing an unsubscribed state requires Member consent: notice-mail sends the consent message on the Notice sending path (not Campaign queue), with an HTTPS `/approve/<opaque token>` link. Default approve-token expiry is 14 days. Operators cannot clear unsub unilaterally. Successful approve clears per-List unsub scope in the token and is audited without raw secrets.

## Requirements

1. **Given** Member M is unsubscribed from one or more Lists (R2), **When** the system initiates clear-unsub consent, **Then** notice-mail delivers the consent email via the Notice sending path only and never via Campaign send-queue (R3, R2).
2. **Given** consent mail containing `/approve/<token>` (R2, R4), **When** M GETs a valid approve token before default 14-day expiry, **Then** unsubscribed flags clear for the List scope bound in the token and Audit records the action without secrets (R2, C2).
3. **Given** M is globally disabled for bounce (R3), **When** consent notice is sent, **Then** the Notice sending path still delivers this system mail (bounce-disable bypass for notice-mail only) (R3).
4. **Given** an Admin or List Manager (R2), **When** they attempt to clear unsub without M completing the approve link, **Then** the operation is unavailable or rejected — no unilateral clear (R2 — failure path).
5. **Given** an expired, reused-after-success, or cryptographically invalid approve token (R2, R4), **When** M requests `/approve/<token>`, **Then** unsub state remains unchanged and the Member sees a safe, understandable outcome (R2 — failure path).

## Boundaries

- Must not change: HTTPS `/unsubscribe/<token>` removal semantics (stories 1–2).
- Must not change: Campaign queue for any outbound mail (R3).
- Must not flip global Member Enabled/Disabled when clearing unsub (C3).
- Out: preference center; resubscribe without consent.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-unsubscribe-consent/epic-unsubscribe-consent.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- high risk: Mis-routed consent mail or unilateral clear breaks R15; approve tokens gate re-subscription to Lists.
