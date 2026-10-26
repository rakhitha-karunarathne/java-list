---
id: 2
type: story
title: "From-domain Issues UI distinct from Drop"
parent: epic-frontend-ops-mts
covers: [R2]
after: [1]
hitl: false
risk: medium
refined: true
---

# From-domain Issues UI distinct from Drop

## Description

Operator **From-domain Issues** surface (list open issues, clear) visually and navigationally distinct from **Drop log**. Copy must never call an Issue a “drop”.

## Requirements

1. **Issues list**
   **Given** open From-domain Issues exist
   **When** Admin opens Issues UI
   **Then** rows show sender, detail, resolution (held vs deleted wording), timestamps
   **And** surface title is Issues — not Drop log

2. **Clear**
   **Given** an open Issue
   **When** Admin clears it
   **Then** `/api/mts/from-domain-issues/{id}/clear` succeeds and row leaves open list

3. **Not Drop**
   **Given** an Issue
   **When** Admin searches Drop log
   **Then** that Issue is not presented as a Drop reason row
   **And** UI chrome does not reuse Drop styling as the only differentiator without label difference

## Boundaries

- Hold folder IMAP CREATE remains backend; UI only shows resolution state from API.
- Out: auto-block counters for Issues.

## References

- parent — epic-frontend-ops-mts.md

## Notes

- Decision: 2026-10-26 — Frontend ops/MTS epic.
