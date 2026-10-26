---
id: 5
type: story
title: "Compose attachments and testing sections"
parent: epic-frontend-campaigns
covers: [R3]
after: [3]
hitl: false
risk: low
refined: true
status: done
---

# Compose attachments and testing sections

## Description

Compose page sections: **Attachments** is its own card immediately under the content card. **Testing** is its own card immediately before the Actions (buttons) card.

## Requirements

1. **Attachments**
   **Given** Compose
   **When** it renders
   **Then** the file attachment control is a card directly under Subject / HTML / Text

2. **Testing**
   **Given** Compose
   **Then** test-send addresses, the bounce-disable hint, and Test send are a card immediately above Save Draft / Queue

## Notes

- Decision: 2026-10-26 — order is Content · Attachments · Preview · Lists · Testing · Actions.

## Dev Agent Record

### Code Review

| Severity | Finding | Disposition |
|---|---|---|
| info | Test send button lives in Testing, not Actions | Matches “testing section before buttons” |
| info | Result message stays on the content card | Unchanged; still visible at top |
