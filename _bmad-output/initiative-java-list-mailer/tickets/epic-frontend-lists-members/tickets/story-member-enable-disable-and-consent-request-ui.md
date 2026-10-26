---
id: 3
type: story
title: "Member enable disable and consent request UI"
parent: epic-frontend-lists-members
covers: [R3]
after: [2]
hitl: false
risk: medium
refined: true
---

# Member enable disable and consent request UI

## Description

Member detail actions: global enable/disable with confirm per product rules; request consent clear-unsub for selected lists (Notice mail). Surface API errors when notice path unset (mail skipped) without claiming delivery.

## Requirements

1. **Disable confirm**
   **Given** an enabled Member
   **When** operator chooses Disable
   **Then** a confirm dialog explains global disable impact
   **And** API disable succeeds and UI refreshes

2. **Re-enable rules**
   **Given** product rules require confirm for re-enable
   **When** operator enables
   **Then** confirm + API path match lists-members epic behavior

3. **Consent request**
   **Given** Member has unsubscribed list memberships in scope
   **When** operator requests clear-unsub
   **Then** API is called with selected lists
   **And** UI does not claim email delivered if notice path is unset (honest copy)

4. **List sender**
   **Given** List sender without manage
   **When** they open Member detail actions
   **Then** manage-only actions are hidden or forbidden

## Boundaries

- No HTTPS approve page redesign (public `/approve/**` already exists).
- Out: inventing email preview of notice body beyond link confirmation.

## References

- parent — epic-frontend-lists-members.md
- tickets — epic-unsubscribe-consent

## Notes

- Decision: 2026-10-26 — Frontend Lists & Members.
