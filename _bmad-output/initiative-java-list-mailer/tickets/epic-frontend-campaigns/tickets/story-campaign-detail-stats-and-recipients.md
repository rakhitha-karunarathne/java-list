---
id: 6
type: story
title: "Campaign detail lists, send stats, and recipients"
parent: epic-frontend-campaigns
covers: [R2]
after: [2]
hitl: false
risk: medium
refined: true
status: done
---

# Campaign detail lists, send stats, and recipients

## Description

Campaign detail shows lists in a table. A separate section shows send start, send finish, messages sent, messages waiting, bounce count, unique views, and total views. Recipients are every address the campaign was sent to, with first open time and open count.

## Requirements

1. **Lists**
   **Given** a campaign with lists
   **When** detail opens
   **Then** the lists are a table of names

2. **Sending section**
   **Given** queue rows and opens
   **When** detail opens
   **Then** send start is the earliest sent time
   **And** send finish is the latest sent time only when status is Finished
   **And** sent, waiting, bounces, unique views, and total views are shown

3. **Recipients**
   **Given** sent queue rows
   **When** detail opens
   **Then** each sent address is a row with first open time and open count
   **And** addresses not yet sent are omitted

## Dev Agent Record

### Code Review

| Severity | Finding | Disposition |
|---|---|---|
| low | Send finish on a still-sending campaign would look complete | Fixed — finish is set only when status is Finished |
| info | Waiting rows are counted, not listed as recipients | Matches "addresses sent to" |
