---
id: 2
type: story
title: "Attention inbox by role"
parent: epic-ops-attention-audit
covers: [R1]
after: [1]
hitl: false
risk: high
refined: true
---

# Attention inbox by role

## Description

Deliver the **Attention** inbox for human-action items: **Admin** sees all actionable rows including **highlighted ops errors** from Ops Logs; **List managers** see only items tied to Lists and Members they manage; **List senders** have no Attention surface. Sidebar shows Attention only when the signed-in role has items or access per UX IA.

## Requirements

1. **Admin sees all Attention** (R1)
   **Given** an **Admin** and Attention items from ops errors, uncorrelated bounces, MTS issues, and other epic hooks
   **When** Admin opens **Attention** from the sidebar
   **Then** all open items appear in one inbox ordered by recency or severity
   **And** ops-error rows use visible highlight treatment (`{colors.warning}` / Attention error rows per UX)

2. **Ops errors highlighted for Admin** (R1, R2)
   **Given** a send, bounce poll, or MTS poll failure recorded in Ops Logs with an Attention hook from story 1
   **When** Admin views Attention
   **Then** those items appear with ops-error highlight distinct from generic Attention rows
   **And** resolving or acknowledging (if implemented) does not delete Ops Logs history

3. **List manager scoped inbox** (R1)
   **Given** a **List manager** granted manage on List A only and Attention items exist for List A and List B
   **When** the List manager opens Attention
   **Then** only items referencing List A or Members on List A are visible
   **And** no row exposes List B identifiers or other tenants’ Member PII

4. **List manager cannot see global ops errors** (R1, R2)
   **Given** an ops-error Attention item for a global bounce mailbox or unrelated Sending path
   **When** a **List manager** (non-Admin) opens Attention
   **Then** that ops-error item is absent
   **And** Admin still sees it

5. **List sender has no Attention** (R1)
   **Given** a user with only **List sender** role (send grant, no manage)
   **When** they sign in
   **Then** **Attention** is not in the sidebar and `/attention` (or equivalent) is forbidden
   **And** no Attention API leaks items to that session

6. **Sidebar category visibility** (R1)
   **Given** a role that has no Attention access (List sender)
   **When** the shell renders navigation
   **Then** the Attention category is hidden entirely per EXPERIENCE IA
   **And** Admin and scoped List managers see Attention when their role includes it

7. **Deep link from Attention** (R1)
   **Given** an Attention row references a Campaign, List, or Member
   **When** the operator follows the row action or link
   **Then** they land on the appropriate **detail** page via deep link
   **And** that detail page is not added as a primary sidebar destination (R4)

## Boundaries

- Must not change: what upstream epics emit (bounce uncorrelated, MTS drop, etc.); only consume and scope display.
- Must not implement: Audit log UI or Ops Logs retention (stories 1 and 3).
- Out: email/push notifications for new Attention items (epic Boundaries).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-ops-attention-audit/epic-ops-attention-audit.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (Attention highlight tokens)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (Attention row, sidebar IA)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- high risk: wrong scoping leaks cross-tenant Member or ops data to List managers.
