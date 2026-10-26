---
title: 'Daily hard soft bounce process'
type: 'feature'
ticket: '4'
created: '2026-10-26'
status: 'done'
baseline_revision: 'df68dc6'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self', 'edge-case-hunter']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Correlated hard/soft bounces need daily consecutive walks with holdback and global disable.

**Approach:** BounceProcessService + cron 02:00 UTC; Parameters for limits; never touch list unsub; ignore UNCORRELATED.

</frozen-after-approval>

## Implementation Notes

V18 sent_at; hard/soft Parameters; disable_reason bounce class.

## Review Triage Log

- holdback mark path unit-light — covered by disable path tests
- soft independent of hard Parameter after seed — ParameterDefinitions documents seed 6
