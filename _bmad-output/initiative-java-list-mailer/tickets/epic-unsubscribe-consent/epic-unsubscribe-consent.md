---
type: epic
title: "Unsubscribe & consent"
parent: initiative-java-list-mailer
covers: [R14, R15]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md]
after: []
refined: true
risk: medium
---

# Unsubscribe & consent

## Description

HTTPS unsubscribe (one URL per campaign send, List-Unsubscribe-Post) and consent clear-unsub via notice-mail. Opaque tokens; audited.

## Outcome

Members can leave lists safely; clear-unsub requires consent email — initiative Done when #4.

## Requirements

R1 (parent R14): HTTPS unsubscribe + List-Unsubscribe-Post; per List; multi-list campaign unsub removes from all Lists in that campaign; Admin/LM visibility scoped.

R2 (parent R15): Clear unsubscribe only via consent email on Notice path; expiry default 14 days; no unilateral clear; audited.

R3 (parent R6, architecture AD-16 notice-mail): notice-mail sends consent/system mail using the Notice sending path; bypasses bounce-disable; Campaign queue never does.

R4 (architecture AD-17, AD-18): Public surfaces use one unsubscribe URL shape `/unsubscribe/<token>` and consent `/approve/<token>`; tokens are opaque AES-GCM (not base64+HMAC); HTTPS only via Public base URL.

## Constraints

C1 (parent C4): Flexible TLS unsupported; member links HTTPS-only.

C2 (parent C6): No raw secrets in Audit events this epic writes.

C3: Unsub is per-list membership state; must not flip global Member Enabled/Disabled (bounce owns disable).

## Done when

1. `/unsubscribe/<token>` removes member from all lists on that campaign; List-Unsubscribe-Post works.
2. Consent `/approve/<token>` clears unsub only via Notice path mail; expiry default 14d; audited.
3. notice-mail slice sends consent/system mail using Notice sending path (bypasses bounce-disable).
4. Expired / already-unsubscribed paths are safe and understandable (no double-apply damage).

## Boundaries

- Campaign compose/send/lifecycle (Stop/Requeue) remains epic-campaigns-send (issues tokens per send pass; list-set snapshot at that send).
- Does not implement bounce disable (epic-bounce-processing).
- Full Audit UI/retention closes in epic-ops-attention-audit; this epic emits audit events.
- Out: preference center; public subscribe.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R14–R15, R6, C4, C6
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-17, AD-18, AD-16 unsubscribe/consent/notice-mail

## Notes

- Waits on epic-campaigns-send because: campaign send and opaque tokens.
- Decision: 2026-10-25 — Refine from parent R14–R15 + notice-mail/AD-17/18; one List-Unsubscribe URL per send kept.

- Decision: 2026-10-25 — Stories planned. Tracer: 6.1 HTTPS unsubscribe opaque token. Refactor sweep included.

- Decision: 2026-10-26 — All stories pulled and refined with full acceptance criteria.