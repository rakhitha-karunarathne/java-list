---
title: 'Refactor sweep'
type: 'chore'
ticket: '5'
created: '2026-10-26'
status: 'done'
baseline_revision: '2971a68'
route: 'oneshot'
route_source: 'pinned'
risk: 'low'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Correlation/process used broad findAll scans; mailbox-query contract must stay MTS-stable.

**Approach:** Narrow repository queries; document mailbox-query stability; no behavior change.

</frozen-after-approval>

## Implementation Notes

findByStatusAndMemberId; findByStatusAndSubjectIgnoreCase; docs note.
