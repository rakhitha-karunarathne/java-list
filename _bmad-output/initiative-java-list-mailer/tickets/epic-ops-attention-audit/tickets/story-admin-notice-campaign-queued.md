---
id: 15
type: story
title: "Admin notice Campaign Queued"
parent: epic-ops-attention-audit
covers: []
after: [12]
hitl: false
risk: medium
refined: true
---

# Admin notice Campaign Queued

## Description

When a campaign transitions to **Queued**, email every enabled Admin with Receive Notification Emails on a copy of the campaign message.

**Subject:** `Campaign Queued: {campaign subject}`

**Body:** campaign text and/or HTML as queued.

## Requirements

1. **Given** `queueAndSend` leaves status Queued  
   **When** save completes  
   **Then** Admins receive the copy (covers Compose queue and MTS factory)

2. **Given** Finished requeue with no new addresses (stays Finished)  
   **Then** no notice

## Boundaries

- In: `CampaignSendService.queueAndSend`  
- Out: test send; Draft save without queue
