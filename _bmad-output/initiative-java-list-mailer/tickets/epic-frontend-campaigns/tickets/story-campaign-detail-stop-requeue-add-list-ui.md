---
id: 2
type: story
title: "Campaign detail Stop Requeue Add List UI"
parent: epic-frontend-campaigns
covers: [R2]
after: [1]
hitl: false
risk: medium
refined: true
---

# Campaign detail Stop Requeue Add List UI

## Description

Campaign detail: status badge, sent/open stats as API returns, **Stop**, **Requeue**, **Add List**. Finished Requeue disabled until a new List is attached; helper explains new-list-only behavior.

## Requirements

1. **Stop**
   **Given** Campaign in Draft, Queued, or Sending
   **When** operator Stops
   **Then** status becomes Stopped via API
   **And** Stop hidden/disabled on Finished/Stopped

2. **Requeue from Stopped**
   **Given** Stopped Campaign
   **When** Requeue
   **Then** API requeue runs and status updates

3. **Finished Add List gate**
   **Given** Finished Campaign
   **When** Requeue before Add List
   **Then** control is disabled with helper text
   **When** operator Adds a List then Requeues
   **Then** both API calls succeed and UI reflects Queued/Sending/Finished progression

4. **Stats**
   **Given** detail view
   **When** opens/sent counts exist on API
   **Then** they display without inventing metrics

## Boundaries

- No queue algorithm changes.
- Out: per-recipient queue browser (unless API already exposes and is trivial).

## References

- parent — epic-frontend-campaigns.md
- ux — EXPERIENCE Flow 4

## Notes

- Decision: 2026-10-26 — Frontend Campaigns epic.
