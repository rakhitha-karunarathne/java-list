---
title: 'Bulk-paste add members'
type: 'feature'
ticket: '2'
created: '2026-10-25'
status: done
baseline_revision: '741c28b'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** List managers need bulk-paste member add with skip counts; senders must be blocked.

**Approach:** members + list_members tables; POST /api/lists/{id}/members/bulk; manage grant + block LIST_SENDER.

</frozen-after-approval>

## Implementation Notes

Returns counts: added, invalid, duplicateInPaste, alreadyOnList, unsubscribed.

## Review Triage Log
