---
id: 4
type: story
title: "C8 detail-not-nav IA verification"
parent: epic-ops-attention-audit
covers: [R4]
after: [2, 3]
hitl: true
risk: low
refined: true
---

# C8 detail-not-nav IA verification

## Description

Verify **C8 detail-not-nav** against Clearwater UX: **List**, **Member**, **Campaign**, MTS inbox, **bounce mailbox**, **Sending path**, **footer**, **user**, and **ops log** details are reached only from indexes or deep links—not as primary sidebar destinations. **Compose** and **Ops Logs** index remain sidebar entries as specified. Human walkthrough of mock or running app; fix nav regressions only, no new product scope.

## Requirements

1. **Detail entities absent from sidebar** (R4)
   **Given** the Clearwater mock or test-profile app shell for **Admin**
   **When** the sidebar nav is enumerated
   **Then** there are no primary destinations for List detail, Member detail, Campaign detail, MTS inbox detail, bounce mailbox detail, Sending path detail, User detail, or ops log detail
   **And** catalog **Footers** may show index in sidebar with detail via row per EXPERIENCE table

2. **Indexes and tools present** (R4)
   **Given** the same shell
   **When** comparing sidebar to EXPERIENCE Information Architecture table
   **Then** **Compose**, **Campaigns**, **Lists**, **Members**, **Ops Logs**, **Attention** (Admin), **Mail-to-send**, **Bounce mailboxes**, **Sending paths**, **Parameters** (Admin), and **Users** (Admin) appear as specified
   **And** **Audit** placement matches mock/IA (sidebar or Admin group)

3. **Index → detail navigation** (R4)
   **Given** Admin on Campaigns index
   **When** they open a row
   **Then** Campaign detail loads without adding Campaign detail to the sidebar
   **And** the same pattern holds for List, Member, User, bounce mailbox, Sending path, MTS inbox, and Ops Logs row → detail

4. **Attention deep links** (R4)
   **Given** an Attention row linking to a Member or Campaign
   **When** the operator follows the link
   **Then** the target detail page opens
   **And** sidebar selection remains on **Attention** or the index context, not a new detail nav item

5. **List sender reduced nav** (R4, R1)
   **Given** a **List sender** session
   **When** sidebar renders
   **Then** detail entities are still absent and **Attention** is absent
   **And** allowed tools (e.g. **Compose**, **Campaigns** per grants) match role IA

6. **DESIGN.md Don'ts honored** (R4)
   **Given** DESIGN.md Do's and Don'ts
   **When** reviewing nav implementation
   **Then** no violation of “Put detail entities … as primary sidebar destinations”
   **And** Flow 5 (detail-not-nav discipline) in EXPERIENCE is demonstrable in walkthrough

7. **Regression checklist** (R4)
   **Given** stories 1–3 added Ops Logs, Attention, and Audit surfaces
   **When** IA verification runs
   **Then** new pages follow index/deep-link pattern (ops log detail from Ops Logs index only)
   **And** findings are filed as nav fixes in this story, not new epic requirements

## Boundaries

- Must not change: functional behavior of Ops Logs, Attention, or Audit—navigation placement only.
- Must not redesign: Clearwater theme tokens or unrelated catalog flows.
- Out: mobile drawer pixel-perfect polish unless it exposes detail-as-sidebar (epic Boundaries).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-ops-attention-audit/epic-ops-attention-audit.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (sidebar, detail-not-nav Don't)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (Information Architecture table, Flow 5)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- hitl: sign-off against `.working/clickable-mockup/index.html` or equivalent page map cited in EXPERIENCE.
