---
title: 'Members index searchable scoped'
type: 'feature'
ticket: '3'
created: '2026-10-25'
status: done
baseline_revision: '29d6755'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Operators need a searchable Members index scoped by role/grants.

**Approach:** GET /api/members?q=; Admin all; LM/sender only members on granted lists.

</frozen-after-approval>

## Implementation Notes

Detail is GET /api/members/{id} (not a sidebar destination).

## Review Triage Log
