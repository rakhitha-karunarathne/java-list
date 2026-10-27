---
id: 11
type: story
title: "Cap each processing log"
parent: epic-ops-attention-audit
covers: [R38]
after: [10]
hitl: false
risk: low
refined: true
status: done
---

# Cap each processing log

## Description

Each processing log has its own keep-count Parameter, default 5000, and its age default changes from 90 days to 30. The logs are Bounce Processing Log, Queue Sending Log, and Mail-To-Send Processing Log. The nightly retention job deletes a row that is older than that log’s days or outside that log’s newest N rows. Ops log retention stays 90 days. Audit retention stays 365 days. Bounce message TTLs stay 180 and 365 days.

## Requirements

1. **Three counts**
   **Given** Parameters
   **When** an Admin opens them
   **Then** each processing log has an integer keep-count, default 5000, minimum 1

2. **Age default**
   **Given** a new install
   **When** the age Parameters are seeded
   **Then** each processing log’s retention days is 30

3. **Both limits**
   **Given** a log with more than its keep-count, and a row older than its days
   **When** the nightly job runs
   **Then** rows outside the newest N are deleted
   **And** rows older than the days are deleted even when the log is under the count

4. **Old default**
   **Given** a stored processing-log age of 90
   **When** the update runs
   **Then** that value becomes 30
   **And** a stored age other than 90 is left unchanged
