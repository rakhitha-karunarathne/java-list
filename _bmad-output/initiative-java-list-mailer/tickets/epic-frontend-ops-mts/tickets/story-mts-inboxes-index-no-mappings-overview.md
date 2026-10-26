---
id: 8
type: story
title: "MTS index is Inboxes only (no mappings overview)"
parent: epic-frontend-ops-mts
covers: [R1]
after: [7]
hitl: false
risk: low
refined: true
status: done
---

# MTS index is Inboxes only (no mappings overview)

## Description

The Mail-to-send index is an **Inboxes** page: list of mail boxes only. List grants live on the authorized-sender page (and as a summary on inbox detail). Drop the global “All Mappings” overview and rename chrome from “Inboxes & Mappings” to **Inboxes**.

## Requirements

1. **Index header and nav**
   **Given** Admin opens Mail-to-send
   **When** the index loads
   **Then** the page heading, sidebar label, and shell title are **Inboxes**
   **And** back links from create / inbox detail say **Inboxes**

2. **No mappings overview on index**
   **Given** the Inboxes index
   **When** it renders
   **Then** there is no “All Mappings” (or equivalent) table
   **And** operators open an inbox, then a sender, to manage list grants

## Boundaries

- In: SPA labels + remove index mappings table; EXPERIENCE / docs wording.
- Out: Removing `GET /api/mts/mappings` (still used by inbox detail List Grants); renaming inbox-detail grant UI.

## References

- EXPERIENCE — Mail-to-send / Inboxes
- related — story-mts-inbox-edit-and-sender-list-grants

## Notes

- Decision: 2026-10-26 — index = inboxes only; grants via sender page.

## Dev Agent Record

### Completion Notes

- Sidebar / shell / page H2 / back links → **Inboxes**.
- Removed All Mappings table from index; inbox detail List Grants unchanged.

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| low | Index fetched `/api/mts/mappings` only for removed overview | Fixed — dropped index fetch; inbox List Grants use `detail.mappings` |
| info | Legacy stories/mocks still say “Inboxes & Mappings” | EXPERIENCE wins; mocks not rewritten |
