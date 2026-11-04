---
id: 13
type: story
title: "Admin notice Bounces Processed"
parent: epic-ops-attention-audit
covers: []
after: [7]
hitl: false
risk: medium
refined: true
---

# Admin notice Bounces Processed

## Description

When a bounce **process** run disables at least one member, email every enabled Admin with Receive Notification Emails on.

**Subject:** `Bounces Processed`

**Body:** list of newly disabled member emails from that run.

## Requirements

1. **Given** process disables ≥1 member  
   **When** the run ends  
   **Then** Admins receive the email list

2. **Given** process disables nobody  
   **Then** no notice

## Boundaries

- In: `BounceProcessService.runProcess`  
- Out: poll/correlate-only runs
