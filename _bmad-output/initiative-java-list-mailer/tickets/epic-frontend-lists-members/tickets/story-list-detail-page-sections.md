---
id: 5
type: story
title: "List detail page sections (settings, members, bulk add)"
parent: epic-frontend-lists-members
covers: [R1]
after: [1]
hitl: false
risk: low
refined: true
status: done
---

# List detail page sections (settings, members, bulk add)

## Description

List detail uses page sections: one card for settings through **Save**, one card for the members **table**, one card for **Bulk Add**.

## Requirements

1. **Settings**
   **Given** a List detail
   **When** it renders
   **Then** back, title, fields, and Save (or view-only hint) share one card

2. **Members**
   **Given** the same page
   **Then** members are an HTML table in their own card

3. **Bulk add**
   **Given** the operator can manage the list
   **Then** bulk paste and Add members are their own card (hidden when view-only)

## Notes

- Decision: 2026-10-26 — List detail sections follow page-sections pattern.

## Dev Agent Record

### Code Review

| Severity | Finding | Disposition |
|---|---|---|
| info | Members already `RecordTable` | Kept; moved into own card |
| info | View-only has no Bulk Add card | Correct — no manage grant |
