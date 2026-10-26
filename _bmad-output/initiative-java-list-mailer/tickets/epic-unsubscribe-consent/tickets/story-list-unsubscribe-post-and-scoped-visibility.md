---
id: 2
type: story
title: "List-Unsubscribe-Post and scoped visibility"
parent: epic-unsubscribe-consent
covers: [R1]
after: [1]
hitl: false
risk: medium
refined: true
---

# List-Unsubscribe-Post and scoped visibility

## Description

RFC List-Unsubscribe-Post one-click unsubscribe uses the same `/unsubscribe/<token>` endpoint as the HTTPS link in the message body. Admin and List Manager surfaces show per-List unsubscribe state for Members, scoped so List Managers see only Lists they manage. Unsubscribe remains per-List membership state and must not toggle global Member Enabled/Disabled.

## Requirements

1. **Given** a sent Campaign message includes List-Unsubscribe and List-Unsubscribe-Post (R1), **When** a compliant mail client POSTs the one-click unsubscribe to `/unsubscribe/<token>`, **Then** the same List removals and Audit behavior apply as for GET on a valid token (R1, C2).
2. **Given** List-Unsubscribe-Post to `/unsubscribe/<token>` with an invalid token or non-compliant payload (R1), **When** the POST is processed, **Then** no membership changes and failure semantics align with GET invalid-token handling (R1 — failure path).
3. **Given** an Admin user (R1), **When** viewing Member or List membership detail, **Then** unsubscribed state is visible across all Lists for that Member.
4. **Given** a List Manager granted manage/send on List L only (R1), **When** they view Members or membership state, **Then** unsubscribed indicators appear only for Lists they manage and not for other Lists (R1 — scoping failure if leaked).
5. **Given** a successful unsubscribe via POST (R1), **When** per-List unsub flags are set, **Then** global Member Enabled/Disabled is unchanged (C3).

## Boundaries

- Must not change: opaque token format, URL shape, or GET handler semantics (story 1 / AD-17, AD-18).
- Must not change: consent `/approve/<token>` or Notice sending path mail (story 3).
- Must not change: bounce-driven global disable rules (epic-bounce-processing).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-unsubscribe-consent/epic-unsubscribe-consent.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
