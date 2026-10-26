---
id: 1
type: story
title: "MTS inbox detail senders and mappings UI"
parent: epic-frontend-ops-mts
covers: [R1]
after: [epic-frontend-shell]
hitl: false
risk: medium
refined: true
---

# MTS inbox detail senders and mappings UI

## Description

Upgrade Mail-to-send index; inbox detail for IMAP settings, authorized senders, and mappings (Lists, substitute From, footer override). Detail only from index/mapping row (C8).

## Requirements

1. **Index**
   **Given** Admin opens Mail-to-send / Inboxes & mappings
   **When** index loads
   **Then** inboxes and all-mappings overview show
   **And** create inbox uses `/api/mts/inboxes`

2. **Inbox detail (C8)**
   **Given** Admin opens an inbox
   **When** detail loads
   **Then** IMAP fields, poll interval, enabled, senders, and mappings are manageable
   **And** sidebar keeps MTS index as primary (not inbox detail)

3. **Mappings**
   **Given** Lists exist
   **When** Admin creates a mapping with listIds, optional substitute From and footer
   **Then** API persists and overview lists the mapping
   **And** Used By consequences remain on catalog side

## Boundaries

- Auth/spam matrix not editable beyond existing Parameters/thresholds if any API exists — no invented UI.
- Out: Drop/Issues (stories 2–3).

## References

- parent — epic-frontend-ops-mts.md
- tickets — epic-mail-to-send story 1

## Notes

- Decision: 2026-10-26 — Frontend ops/MTS epic.
