---
title: 'Re-enable rules with confirm'
type: 'feature'
ticket: '5'
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

**Problem:** Re-enable must require confirm and must not override list unsubscribe.

**Approach:** PUT enable needs confirm=true; POST re-add with confirm; unsubscribed memberships ignored.

</frozen-after-approval>

## Implementation Notes

Open-tracking re-enable deferred/out (never auto).

## Review Triage Log
