---
id: 2
type: story
title: "Bounce mailboxes and Notice path UI"
parent: epic-frontend-catalogs
covers: [R2, R4]
after: [1]
hitl: false
risk: medium
refined: true
---

# Bounce mailboxes and Notice path UI

## Description

Bounce mailboxes index → detail (IMAP host/port/creds, poll interval, enabled). Separate Admin control to designate the single **Notice sending path** from catalogued Sending paths.

## Requirements

1. **Bounce index (R2)**
   **Given** Admin opens Bounce mailboxes
   **When** index loads
   **Then** mailboxes list with name/enabled/poll summary
   **And** create/edit uses bounce-mailbox API

2. **Bounce detail (R2, C8)**
   **Given** Admin opens a mailbox row
   **When** detail shows
   **Then** IMAP fields and poll interval (default 15) are editable
   **And** password is write-only
   **And** detail is not a sidebar primary destination

3. **Notice path (R4)**
   **Given** at least one Sending path exists
   **When** Admin designates Notice sending path
   **Then** the choice persists via notice-sending-path API
   **And** UI clarifies this path is for system/consent mail (not Campaign queue)

4. **Unset Notice path (R4)**
   **Given** Notice path is cleared/unset
   **When** Admin views the control
   **Then** empty state is clear
   **And** no notification send is expected until set (backend skip behavior)

## Boundaries

- No bounce correlation UI here.
- Out: Drop log / Issues (ops-mts FE epic).

## References

- parent — epic-frontend-catalogs.md

## Notes

- Decision: 2026-10-26 — Frontend catalogs epic.
