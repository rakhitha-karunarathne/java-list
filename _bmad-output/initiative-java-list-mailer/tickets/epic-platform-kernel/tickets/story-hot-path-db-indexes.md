---
id: 8
type: story
title: "Hot-path DB indexes for bounce correlation and joins"
parent: epic-platform-kernel
covers: [R2, R4]
after: [1]
hitl: false
risk: medium
refined: true
---

# Hot-path DB indexes for bounce correlation and joins

## Description

Regular scheduled and admin queries (bounce correlate/process, send queue, list membership, campaign status, attention) must use indexes on filter/join columns. Audit existing Flyway indexes against repository methods; add a migration for gaps so large `send_queue` / `bounce_events` tables do not table-scan on every tick.

## Requirements

1. **Given** bounce correlation looks up `verp_rcpt`, `smtp_message_id`, `correlation_token`, and `(status, subject)`  
   **Then** each of those paths is backed by an index (token already unique)

2. **Given** bounce process / campaign delete uses `bounce_events.member_id` or `send_queue_id`  
   **Then** those columns are indexed

3. **Given** reverse FK lookups (`list_members.member_id`, `campaign_lists.list_id`, `user_list_grants.list_id`, …)  
   **Then** secondary indexes exist where the PK does not lead with that column

4. **Given** docs  
   **Then** deploy/docs state that hot-path indexes are maintained via Flyway when repository query patterns change

## Boundaries

- In: Flyway V45 (or next) indexes only; short docs  
- Out: Query rewrites, partitioning, covering indexes for every UI sort, EXPLAIN automation in CI
