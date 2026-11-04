---
id: 14
type: story
title: "Admin notice Bounce(s) Received"
parent: epic-ops-attention-audit
covers: []
after: [7]
hitl: false
risk: medium
refined: true
---

# Admin notice Bounce(s) Received

## Description

When a bounce **fetch** run ingests at least one message, correlate those events, then email Admins counts grouped by campaign (and an uncorrelated bucket).

**Subject:** `Bounce(s) Received`

## Requirements

1. **Given** ingest count > 0 for a poll run (due poll or single mailbox poll)  
   **When** fetch finishes  
   **Then** newly ingested events are correlated  
   **And** Admins get per-campaign counts (via send_queue → campaign)

2. **Given** ingest count = 0  
   **Then** no notice

## Boundaries

- In: `BouncePollService.pollDue` / `pollOne`  
- Out: process/disable notices (story 13)
