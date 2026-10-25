---
title: 'Footers catalog'
type: 'feature'
ticket: '3'
created: '2026-10-25'
status: done
baseline_revision: 'a915164'
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

**Problem:** Admins need footer catalog with Used By delete guards.

**Approach:** CRUD /api/footers using CatalogReferenceService FOOTER target type.

</frozen-after-approval>

## Implementation Notes

Same delete 409 shape as sending paths.

## Review Triage Log
