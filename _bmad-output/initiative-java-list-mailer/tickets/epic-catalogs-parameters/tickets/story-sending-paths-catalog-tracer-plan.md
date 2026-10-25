---
title: 'Sending paths catalog tracer'
type: 'feature'
ticket: '1'
created: '2026-10-25'
status: done
baseline_revision: 'e8d5110'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self', 'bugbot']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Admins need a sending-path catalog with Used By delete guards before lists/campaigns.

**Approach:** Persist SMTP+From paths; CatalogReferenceService tracks references; DELETE returns 409 + usedBy.

</frozen-after-approval>

## Implementation Notes

Password omitted from GET detail (username kept). Lists epic will call CatalogReferenceService.add/remove.

## Review Triage Log

- 2026-10-25 bugbot: transactional delete + idempotent add.
- 2026-10-25 smoke: create ok; DELETE while referenced → 409 usedBy; then 204.
