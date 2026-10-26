---
id: 1
type: story
title: "Sending paths and Footers index-detail UI"
parent: epic-frontend-catalogs
covers: [R1, R3]
after: [epic-frontend-shell]
hitl: false
risk: medium
refined: true
---

# Sending paths and Footers index-detail UI

## Description

SPA indexes for **Sending paths** and **Footers** with row → detail; Used By list with navigable links; delete rejected with clear Used By messaging.

## Requirements

1. **Paths index (R1)**
   **Given** Admin opens Sending paths from sidebar
   **When** the index loads
   **Then** catalogued paths are listed newest or name-ordered
   **And** Create opens a form that POSTs to existing path API

2. **Path detail (R1, C8)**
   **Given** Admin clicks a path row
   **When** detail loads
   **Then** SMTP/From fields are editable (password write-only / blank = keep)
   **And** Used By lists referencers with links
   **And** sidebar still shows Sending paths index — not path detail as primary nav

3. **Delete blocked (R1)**
   **Given** a path referenced by a List or MTS mapping
   **When** Admin attempts delete
   **Then** UI shows conflict with Used By (does not silent-fail)
   **And** path remains

4. **Footers index + detail (R3)**
   **Given** Admin opens Footers
   **When** they create/edit a footer and open detail
   **Then** HTML/text bodies save via footers API
   **And** Used By shows Lists/mappings; delete blocked while referenced

## Boundaries

- APIs only: `/api/sending-paths/**`, `/api/footers/**`.
- Out: Notice path (story 2); Bounce mailboxes (story 2).

## References

- parent — epic-frontend-catalogs.md
- tickets — epic-catalogs-parameters

## Notes

- Decision: 2026-10-26 — Frontend catalogs epic.
