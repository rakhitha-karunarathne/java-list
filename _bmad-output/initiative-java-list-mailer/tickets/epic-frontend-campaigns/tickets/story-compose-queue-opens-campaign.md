---
id: 8
type: story
title: "Compose Queue opens the campaign"
parent: epic-frontend-campaigns
covers: [R3]
after: [7]
hitl: false
risk: low
refined: true
status: done
---

# Compose Queue opens the campaign

## Description

A successful Queue from the Compose page leaves Compose and opens that campaign's detail page. The campaigns index Queue button is unchanged: it queues in place and does not navigate.

## Requirements

1. **Success leaves Compose**
   **Given** a saved Draft on Compose, including Edit of an existing Draft
   **When** Queue succeeds
   **Then** the operator is on that campaign's detail page
   **And** this is the same when the campaign is Queued, Sending, or already Finished

2. **Failure stays**
   **Given** Queue is rejected
   **When** the error is shown
   **Then** the operator remains on Compose
