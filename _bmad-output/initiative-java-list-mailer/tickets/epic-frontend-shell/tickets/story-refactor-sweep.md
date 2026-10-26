---
id: 3
type: story
title: "Refactor sweep"
parent: epic-frontend-shell
covers: []
after: [1, 2]
hitl: false
risk: low
refined: true
---

# Refactor sweep

## Description

Cleanup-only: extract nav config, shared layout components, token CSS variables. No new routes or auth behavior.

## Requirements

1. **No new scope** — Diff has no new sidebar destinations or auth features.
2. **Shell smoke** — Clearwater + role-hide still pass.
3. **Login smoke** — Email/password and MFA step-up still pass.

## Boundaries

- No DESIGN token changes except bug fixes.
- Out: feature pages for other FE epics.

## References

- parent — epic-frontend-shell.md

## Notes

- Decision: 2026-10-26 — Created with frontend epic slice.
