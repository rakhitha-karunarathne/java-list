---
id: 1
type: story
title: "Campaigns index and status filter UI"
parent: epic-frontend-campaigns
covers: [R1, R4]
after: [epic-frontend-shell]
hitl: false
risk: low
refined: true
---

# Campaigns index and status filter UI

## Description

**Campaigns** sidebar index listing campaigns with exact status badge labels Draft · Queued · Sending · Finished · Stopped (text + color). Filter by status; row opens detail.

## Requirements

1. **Index load**
   **Given** an operator with campaign access
   **When** they open Campaigns
   **Then** campaigns from API appear with subject and status badge
   **And** status is never color-only

2. **Filter**
   **Given** mixed statuses
   **When** operator filters Finished
   **Then** only Finished rows show

3. **Row → detail (C8)**
   **Given** operator clicks a row
   **When** detail opens
   **Then** Campaigns remains the sidebar primary entry

4. **Stop on index (optional)**
   **Given** EXPERIENCE allows Stop on index row for Draft/Queued/Sending
   **When** implemented
   **Then** illegal statuses hide/disable Stop

## Boundaries

- Detail actions in story 2; Compose in story 3.
- Out: analytics dashboards.

## References

- parent — epic-frontend-campaigns.md

## Notes

- Decision: 2026-10-26 — Frontend Campaigns epic.
