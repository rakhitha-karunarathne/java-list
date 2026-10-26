---
id: 5
type: story
title: "Refactor sweep"
parent: epic-frontend-catalogs
covers: []
after: [1, 2, 3, 4]
hitl: false
risk: low
refined: true
---

# Refactor sweep

## Description

Share index/detail table patterns and Used By component across catalog pages. No new catalog features.

## Requirements

1. No new scope.
2. Paths/Footers/Bounce/Notice/Parameters/Users smokes still pass.
3. C8 detail-not-nav unchanged.

## Boundaries

- Out: Lists/Members/Campaigns UIs.

## References

- parent — epic-frontend-catalogs.md

## Notes

- Decision: 2026-10-26 — Frontend catalogs epic.
