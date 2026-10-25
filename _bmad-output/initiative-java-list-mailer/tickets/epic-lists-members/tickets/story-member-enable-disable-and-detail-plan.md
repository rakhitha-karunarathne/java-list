---
title: 'Member enable disable and detail'
type: 'feature'
ticket: '4'
created: '2026-10-25'
status: done
baseline_revision: '29d6755'
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

**Problem:** Member detail must show list memberships and global enable/disable with reason, distinct from unsubscribe.

**Approach:** MemberDetail includes lists[] with unsubscribed flag; PUT disable with reason; timeline stub for audit epic.

</frozen-after-approval>

## Implementation Notes

Campaign queue must skip enabled=false (contract documented for send epic).

## Review Triage Log
