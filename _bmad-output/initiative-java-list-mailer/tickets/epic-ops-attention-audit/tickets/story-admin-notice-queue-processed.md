---
id: 12
type: story
title: "Admin notice Queue Processed"
parent: epic-ops-attention-audit
covers: []
after: [8]
hitl: false
risk: medium
refined: true
---

# Admin notice Queue Processed

## Description

When a send-queue drain run finds pending work, email every **enabled Admin** on the Notice Sending Path.

**Subject:** `Queue Processed`

**Body:** start date-time, finish date-time, per-campaign emails sent, per-campaign emails errored (SMTP/runtime failures), messages/hour = total sent ÷ run duration in hours.

## Requirements

1. **Given** pending queue rows at the start of a batch  
   **When** the batch finishes  
   **Then** each enabled Admin receives the notice (Notice path)  
   **And** empty ticks (no pending) send nothing

2. **Given** Notice Sending Path is unset  
   **When** a batch would notify  
   **Then** skip without failing the send run

## Boundaries

- In: scheduled/manual queue drain via `processPendingBatch`  
- Out: test send; changing Attention UI
