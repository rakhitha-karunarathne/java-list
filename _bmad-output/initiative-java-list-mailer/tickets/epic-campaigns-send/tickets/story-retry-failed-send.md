---
id: 13
type: story
title: "Retry a failed send"
parent: epic-campaigns-send
covers: [R21]
after: [12]
hitl: false
risk: medium
refined: true
status: done
---

# Retry a failed send

## Description

The first SMTP failure on a recipient sets that row to Failed-Retry Pending. Parameter `send.retry.count` (default 3) is how many further tries it gets. `send.retry.delay.minutes` (default 5) is how long it waits. While it waits, other recipients are sent. After the count is used and it still fails, the row is Send Failed and is skipped. Campaign detail shows Retry failed when any row is Send Failed. That button sets those rows to Pending, clears their failure count, and sets a Finished campaign back to Queued. Failed-Retry Pending rows are left to retry on their own. A pass is not Finished while any row is Failed-Retry Pending.

## Requirements

1. **First failure**
   **Given** a recipient SMTP failure
   **When** the processor continues the run
   **Then** that row is Failed-Retry Pending
   **And** a later recipient in the same run is still sent

2. **Give up**
   **Given** the retry count is used up
   **When** the row fails again
   **Then** it is Send Failed
   **And** later runs skip it

3. **Retry failed**
   **Given** a campaign with at least one Send Failed row
   **When** the operator presses Retry failed
   **Then** those rows are Pending with a cleared failure count
   **And** Failed-Retry Pending rows are unchanged

4. **Defaults**
   **Given** a new install
   **When** the retry Parameters are seeded
   **Then** the count is 3 further tries
   **And** the delay is 5 minutes
