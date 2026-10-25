---
title: 'JWT sessionStorage login stub'
type: 'feature'
ticket: '5'
created: '2026-10-25'
status: done
baseline_revision: '6057cdbe510c620d17b2773bbc5ce9039a61a802'
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

**Problem:** Need a bootstrap email+password JWT path and SPA sessionStorage so later epics can protect `/api`.

**Approach:** Spring Security resource-server JWT with env bootstrap credentials; login API; SPA stores Bearer token in sessionStorage and calls protected `/api/ping`.

</frozen-after-approval>

## Implementation Notes

Defaults admin@example.com / changeme; MAILER_JWT_SECRET ≥32 bytes.

## Review Triage Log

- 2026-10-25: unauth /api/ping → 401; login + Bearer ping → ok.
