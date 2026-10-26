---
id: 2
type: story
title: "Members searchable index and detail UI"
parent: epic-frontend-lists-members
covers: [R2, R3, R4]
after: [1]
hitl: false
risk: high
refined: true
---

# Members searchable index and detail UI

## Description

Sidebar **Members** searchable index; row → Member detail with enabled status and list memberships. List managers see only members on managed Lists.

## Requirements

1. **Searchable index**
   **Given** Admin opens Members
   **When** they type a search string
   **Then** results filter via members API
   **And** empty state has one clear sentence

2. **LM scope**
   **Given** List manager for List A only
   **When** Members index loads
   **Then** members only on List B do not appear
   **And** no cross-tenant PII leaks in UI

3. **Member detail (C8)**
   **Given** operator opens a member row
   **When** detail loads
   **Then** email, enabled, and list memberships show
   **And** deep links from List detail can open the same detail
   **And** Members remains the sidebar entry

## Boundaries

- Enable/disable + consent actions in story 3.
- Out: click tracking timeline (V1 out).

## References

- parent — epic-frontend-lists-members.md

## Notes

- Decision: 2026-10-26 — Frontend Lists & Members; high risk scoping.
