---
id: 3
type: story
title: "Drop log and global blocks UI"
parent: epic-frontend-ops-mts
covers: [R3]
after: [1]
hitl: false
risk: medium
refined: true
---

# Drop log and global blocks UI

## Description

Drop log combined + per-mailbox filter (inbox config link pre-filters). Row shows reason and counted-for-auto-block. Actions: resolve, block sender, block domain; Blocks list with unblock.

## Requirements

1. **Combined Drop log**
   **Given** drop rows exist
   **When** Admin opens Drop log
   **Then** newest-first rows show mailbox, reason, counted flag, time

2. **Inbox pre-filter**
   **Given** MTS inbox detail
   **When** Admin follows Drop log link
   **Then** Drop log opens filtered to that inboxId

3. **Actions**
   **Given** a Drop row
   **When** Admin resolve / block sender / block domain
   **Then** corresponding `/api/mts/**` calls succeed and UI updates

4. **Blocks**
   **Given** global blocks exist
   **When** Admin opens Blocks
   **Then** sender/domain entries list with unblock

## Boundaries

- No courtesy-reject UI (strangers stay silent — display only).
- Out: Issues (story 2).

## References

- parent — epic-frontend-ops-mts.md

## Notes

- Decision: 2026-10-26 — Frontend ops/MTS epic.
