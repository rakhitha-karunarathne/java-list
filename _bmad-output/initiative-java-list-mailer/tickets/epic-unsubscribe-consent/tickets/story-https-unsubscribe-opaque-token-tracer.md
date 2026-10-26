---
id: 1
type: story
title: "HTTPS unsubscribe opaque token tracer"
parent: epic-unsubscribe-consent
covers: [R1, R4]
after: [epic-campaigns-send]
hitl: true
risk: high
refined: true
---

# HTTPS unsubscribe opaque token tracer

## Description

End-to-end tracer: a Campaign send embeds one HTTPS `/unsubscribe/<opaque token>` link per send (body and List-Unsubscribe share it), using AD-18 AES-GCM opaque tokens and AD-17 Public base URL. When a Member follows the link, they are unsubscribed from every List in that Campaign’s list-set snapshot at send time. Unsubscribe updates per-List membership only and must not change global Member Enabled/Disabled.

## Requirements

1. **Given** a Campaign send pass has minted an opaque unsubscribe token (R4), **When** the message is rendered for delivery, **Then** exactly one HTTPS URL shaped `/unsubscribe/<token>` appears in the body and List-Unsubscribe header, absolutized via Public base URL (R4, C1).
2. **Given** a valid token binding Member M to Campaign C’s list-set snapshot at send (R1, R4), **When** M GETs `/unsubscribe/<token>`, **Then** M is marked unsubscribed on every List in that snapshot and an Audit event is recorded with no raw secrets (R1, C2).
3. **Given** a successful unsubscribe via GET (R1), **When** membership is updated, **Then** global Member Enabled/Disabled for M is unchanged (C3).
4. **Given** a multi-list Campaign (R1), **When** M unsubscribes with one token, **Then** M is removed from all Lists in C’s snapshot and not from Lists outside that send scope (R1).
5. **Given** a missing, malformed, tampered, or undecryptable token (R4), **When** any client requests `/unsubscribe/<token>`, **Then** no List membership changes, the response is safe and non-enumerating, and no Audit row leaks token material (R4, C2 — failure path).

## Boundaries

- Must not change: Campaign compose/send lifecycle or per-send token issuance (epic-campaigns-send).
- Must not change: global Member Enabled/Disabled (epic-bounce-processing owns disable).
- Must not change: consent clear-unsub or notice-mail flows (later stories in this epic).
- Out: preference center; public subscribe.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-unsubscribe-consent/epic-unsubscribe-consent.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- high risk: Public-edge token crypto and membership writes; defects allow forged unsubs or unintended list removals.
