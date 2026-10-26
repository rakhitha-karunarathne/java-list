---
title: 'Refactor sweep'
type: 'chore'
ticket: '6'
created: '2026-10-26'
status: 'done'
baseline_revision: '723f541'
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

**Problem:** Duplicated email parse helpers across MTS gate/from-domain/drop.

**Approach:** Shared MtsEmails; docs/mts.md; no behavior change.

</frozen-after-approval>

## Implementation Notes

MtsEmails; extractEmail delegates; mts.md index.
