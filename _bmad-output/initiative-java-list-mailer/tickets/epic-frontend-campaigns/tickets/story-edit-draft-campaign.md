---
id: 7
type: story
title: "Edit a Draft campaign from Compose"
parent: epic-frontend-campaigns
covers: [R3]
after: [6]
hitl: false
risk: low
refined: true
status: done
---

# Edit a Draft campaign from Compose

## Description

A Draft campaign can be edited. Campaigns index and campaign detail show Edit only when the status is Draft. Edit opens the Compose page loaded with that campaign: subject, HTML, text, lists, and existing attachments. Save updates the same Draft. A new file replaces attachments; leaving the file empty keeps the ones already stored.

## Requirements

1. **Edit control**
   **Given** a campaign in Draft
   **When** the operator views the campaigns index or its detail
   **Then** an Edit button is shown
   **And** campaigns in any other status have no Edit button

2. **Reuse Compose**
   **Given** Edit on a Draft
   **When** the operator opens it
   **Then** the Compose page shows that campaign's subject, bodies, lists, and kept attachment names
   **And** Save updates that Draft rather than creating another
