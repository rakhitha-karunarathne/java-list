---
id: 9
type: story
title: "Page sections: title cards, Used By, MTS inbox, Compose"
parent: epic-frontend-catalogs
covers: [R1, R6]
after: [8]
hitl: false
risk: medium
refined: true
---

# Page sections: title cards, Used By, MTS inbox, Compose

## Description

Tighten the page-sections pattern: **all** detail content lives in white cards (record title is its own card). Apply to User detail title, catalog **Used By**, MTS inbox detail, and Compose.

## Requirements

1. **Record title card**
   **Given** a sectioned detail page (User detail; MTS inbox detail)
   **When** it renders
   **Then** back + record title are in their own `.card` — nothing for that page sits on bare canvas outside `.page-sections` cards

2. **Used By**
   **Given** Sending Path or Footer detail
   **When** it renders
   **Then** Used By is its own `.card`, separate from the edit form card

3. **MTS inbox detail**
   **Given** Admin opens an inbox
   **Then** sections are: (1) title, (2) account through Delete Inbox, (3) Authorized Senders + add form, (4) List Grants table

4. **Compose**
   **Given** Compose
   **Then** sections are: (1) Subject + HTML Body + Text Body, (2) Preview, (3) Lists table (just before actions), (4) actions (attachment, test send, Save / Test / Queue)

## Boundaries

- In: SPA layout + EXPERIENCE/DESIGN pattern update.
- Out: Sectioning every other page not listed.

## Notes

- Decision: 2026-10-26 — title is a card; Used By always its own card on catalog detail.
