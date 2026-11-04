---
id: 7
type: story
title: "Delete list with optional orphan recipients"
parent: epic-lists-members
covers: [R1]
after: [1]
hitl: false
risk: medium
refined: true
---

# Delete list with optional orphan recipients

## Description

List **detail** (manage grant) can **Delete** the list. A confirmation dialog includes a checkbox to also delete recipients who are **not** on any other list (and their member-linked send/bounce/open data). Recipients still on another list stay. Delete is rejected while the list has an **ongoing** campaign (Queued, Sending, or Paused).

## Requirements

1. **Given** List detail with manage grant and no ongoing campaign for the list  
   **When** Delete is confirmed (checkbox off)  
   **Then** the list, its memberships, grants, and MTS list grants are removed; members remain

2. **Given** checkbox “delete recipients…” is on  
   **When** Delete is confirmed  
   **Then** members who were only on this list are removed with their member-scoped data; members on other lists are kept

3. **Given** a Queued, Sending, or Paused campaign includes the list  
   **When** Delete is attempted  
   **Then** the API rejects with conflict and the list is unchanged

4. **Given** Lists index  
   **Then** there is no Delete control (detail only)

## Boundaries

- In: `DELETE /api/lists/{id}` + `deleteOrphanMembers`; List detail confirm dialog  
- Out: Deleting campaigns themselves; public subscribe
