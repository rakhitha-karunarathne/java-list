---
title: 'Stop and Requeue lifecycle'
type: 'feature'
ticket: '4'
created: '2026-10-25'
status: done
baseline_revision: '1828ae1'
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

**Problem:** Campaigns need Stop → Stopped and Requeue from Stopped.

**Approach:** POST /stop and /requeue; Finished+Add List requeue deferred to follow-on.

</frozen-after-approval>

## Implementation Notes

shortcut: Finished Requeue after Add List not in this commit, add with multi-list story.

## Review Triage Log
