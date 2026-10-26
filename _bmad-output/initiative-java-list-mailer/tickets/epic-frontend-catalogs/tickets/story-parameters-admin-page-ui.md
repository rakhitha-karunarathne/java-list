---
id: 3
type: story
title: "Parameters Admin page UI"
parent: epic-frontend-catalogs
covers: [R5]
after: [epic-frontend-shell]
hitl: false
risk: low
refined: true
---

# Parameters Admin page UI

## Description

Admin **Parameters** page: each key as stacked label → control → always-visible description (purpose, values, range). Replace thin tracer list with Clearwater layout.

## Requirements

1. **Stacked fields (R5)**
   **Given** Admin opens Parameters
   **When** the page loads from `/api/parameters`
   **Then** each Parameter shows description under the control
   **And** save uses existing PUT semantics

2. **Admin only (R5)**
   **Given** a non-Admin session
   **When** they navigate to Parameters
   **Then** the route is hidden and/or API 403 is handled without leaking values

3. **No credentials (R5, C2)**
   **Given** the Parameters page
   **When** reviewed
   **Then** no SMTP/IMAP passwords or signing secrets appear as Parameter fields

## Boundaries

- No new Parameter keys.
- Out: Users Security (story 4).

## References

- parent — epic-frontend-catalogs.md
- ux — EXPERIENCE Flow 1

## Notes

- Decision: 2026-10-26 — Frontend catalogs epic.
