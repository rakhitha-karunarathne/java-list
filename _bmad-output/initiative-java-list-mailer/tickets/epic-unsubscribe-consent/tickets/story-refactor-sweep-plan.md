---
title: 'Refactor sweep'
type: 'chore'
ticket: '5'
created: '2026-10-26'
status: 'done'
baseline_revision: '9d850bc'
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

**Problem:** Duplicated public HTML shells and dead C3 guard after stories 1–4.

**Approach:** Shared PublicHtmlPage; drop no-op enabled restore; refresh OpaqueTokenHelper docs.

</frozen-after-approval>

## Implementation Notes

Cleanup only; re-run unsub/consent unit tests.
