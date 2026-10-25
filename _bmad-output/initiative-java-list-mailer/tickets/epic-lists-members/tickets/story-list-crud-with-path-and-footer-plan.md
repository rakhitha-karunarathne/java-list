---
title: 'List CRUD with path and footer'
type: 'feature'
ticket: '1'
created: '2026-10-25'
status: done
baseline_revision: '44e5b3b'
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

**Problem:** Operators need List CRUD bound to a sending path and optional footer before members/campaigns.

**Approach:** Persist lists; wire catalog_references; Admin/LM create; manage grant gates update/delete; creator LM auto-granted.

</frozen-after-approval>

## Implementation Notes

API only for detail (no sidebar destination). Overrides: spamThreshold, payloadOverride, imageTtlOverrideHours.

## Review Triage Log
