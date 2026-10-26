---
id: 1
type: story
title: "Clearwater shell and role-aware sidebar tracer"
parent: epic-frontend-shell
covers: [R1, R2, R4]
after: [epic-ops-attention-audit]
hitl: true
risk: medium
refined: true
---

# Clearwater shell and role-aware sidebar tracer

## Description

Introduce Clearwater layout chrome and a role-aware sidebar that lists **indexes and tools** only (C8). Placeholder empty states allowed for routes owned by later frontend epics.

## Requirements

1. **Clearwater chrome (R1)**
   **Given** an authenticated operator
   **When** the app shell loads
   **Then** canvas/surface/ink/accent match DESIGN.md tokens and product name **List Mailer** appears in chrome
   **And** body/heading fonts follow DESIGN typography (not system Inter/Arial defaults)

2. **Sidebar indexes only (R2, C8)**
   **Given** an Admin session
   **When** the sidebar is enumerated
   **Then** primary items include Attention, Campaigns, Compose, Lists, Members, Mail-to-send, Bounce mailboxes, Sending paths, Footers, Parameters, Users, Ops Logs, and Audit (or Admin-group placement per mock)
   **And** no primary item is Campaign/List/Member/User/inbox/path/mailbox/ops-log **detail**

3. **Role-hide categories (R2)**
   **Given** a List sender session (send grant, no manage)
   **When** the shell renders
   **Then** Attention is absent
   **And** Admin-only items (Users, Parameters) are absent
   **And** Compose/Campaigns remain when grants allow

4. **Active nav treatment (R1)**
   **Given** the operator opens Compose
   **When** sidebar renders
   **Then** Compose shows accent-subtle active treatment
   **And** switching to Campaigns updates active state without adding detail nav

5. **Narrow drawer (R4)**
   **Given** a narrow viewport
   **When** the operator opens nav
   **Then** sidebar is a drawer (not permanently stealing width)
   **And** Esc or overlay dismisses the drawer

6. **Placeholder indexes (R2)**
   **Given** a sidebar destination whose body epic is not yet built
   **When** the operator opens it
   **Then** they see a clear empty/placeholder page — not a broken route
   **And** detail-not-nav discipline still holds

## Boundaries

- Must not implement catalog/list/campaign/ops page bodies beyond placeholders (later FE epics).
- Must not change backend auth or role claims.
- Out: dark mode; plugin marketplace UI.

## References

- parent — epic-frontend-shell.md
- ux — EXPERIENCE.md Information Architecture; DESIGN.md; mockups/clickable/

## Notes

- Decision: 2026-10-26 — Created with frontend epic slice; tracer for shell before feature pages.
