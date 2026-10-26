---
id: 3
type: story
title: "Compose Clearwater polish and test send UI"
parent: epic-frontend-campaigns
covers: [R3]
after: [1]
hitl: true
risk: medium
refined: true
---

# Compose Clearwater polish and test send UI

## Description

Bring Compose to Clearwater: editor + preview HTML/Text tabs in sync; multi-list select with order; Save Draft; Queue; Test send dialog with bounce-disable disclosure and result counts.

## Requirements

1. **Preview sync**
   **Given** Compose open
   **When** operator edits HTML or text
   **Then** preview tabs stay in sync with editors

2. **Multi-list**
   **Given** multiple Lists in scope
   **When** operator selects and reorders Lists
   **Then** save/queue payload matches campaigns API list order (first path wins)

3. **Test send**
   **Given** Draft with ≥1 List
   **When** operator pastes addresses and Test sends
   **Then** UI shows sent/skip counts from API
   **And** copy states test send ignores bounce-disable

4. **Queue**
   **Given** valid Draft
   **When** Queue
   **Then** status leaves Draft toward Queued/Sending per API
   **And** failures surface readable errors (Ops/Attention remain backend)

5. **Layout**
   **Given** wide desktop
   **When** Compose shows
   **Then** editor + preview can sit side-by-side when width allows

## Boundaries

- Replace/upgrade existing App.jsx Compose; do not fork a second compose.
- Out: visual HTML WYSIWYG beyond textarea + preview unless already trivial.

## References

- parent — epic-frontend-campaigns.md
- ux — EXPERIENCE Flow 3

## Notes

- Decision: 2026-10-26 — Frontend Campaigns epic; HITL for Compose UX.
