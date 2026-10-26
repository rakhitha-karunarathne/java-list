---
id: 1
type: story
title: "Lists index detail and bulk add UI"
parent: epic-frontend-lists-members
covers: [R1, R4]
after: [epic-frontend-shell]
hitl: false
risk: medium
refined: true
---

# Lists index detail and bulk add UI

## Description

**Lists** sidebar index; row → List detail with settings (sending path, footer) and bulk paste add members showing skip counts (invalid, duplicate, already on list, unsubscribed).

## Requirements

1. **Lists index**
   **Given** Admin or List manager with grants
   **When** they open Lists
   **Then** visible Lists match role scope
   **And** Create List uses lists API

2. **List detail (C8)**
   **Given** operator opens a List row
   **When** detail loads
   **Then** name/path/footer bindings are editable per grants
   **And** sidebar remains on Lists (not List detail)

3. **Bulk add**
   **Given** List detail bulk-add box
   **When** operator pastes newline/comma/semicolon addresses and submits
   **Then** UI shows counts: added, skipped-invalid, skipped-duplicate-in-paste, skipped-already-on-list, skipped-unsubscribed
   **And** results match lists/members API

## Boundaries

- No Member index here (story 2).
- Out: public subscribe form.

## References

- parent — epic-frontend-lists-members.md
- prd — FR bulk add

## Notes

- Decision: 2026-10-26 — Frontend Lists & Members.
