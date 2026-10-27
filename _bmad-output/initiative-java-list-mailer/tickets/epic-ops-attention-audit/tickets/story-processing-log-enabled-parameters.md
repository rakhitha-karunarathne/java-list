---
id: 10
type: story
title: "Enable or disable each processing log"
parent: epic-ops-attention-audit
covers: [R2]
after: [9]
hitl: false
risk: low
refined: true
status: done
---

# Enable or disable each processing log

## Description

Each processing log has a Parameter, default on. When off, new rows are not written. Admins still open the page and retention still deletes old rows.

## Requirements

1. **Parameters**
   **Given** Parameters
   **When** an Admin looks at the page
   **Then** Bounce Processing Log Enabled, Queue Sending Log Enabled, and Mail-To-Send Processing Log Enabled are toggles
   **And** each defaults to true

2. **Writes**
   **Given** a log's enabled parameter is false
   **When** that processor runs
   **Then** no new activity row is saved
   **And** a missing value still writes (default on)

## Dev Agent Record

### Code Review

| Severity | Finding | Disposition |
|---|---|---|
| low | Retention day parameters for the three logs were not validated as integers | Fixed in the same parameter put check |
| info | Turning a log off does not hide the nav item | Kept — admins can still read existing rows |
