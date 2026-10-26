---
title: 'Expired and already-unsubscribed paths'
type: 'feature'
ticket: '4'
created: '2026-10-26'
status: 'done'
baseline_revision: 'f79a8a3'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self', 'verification-gap']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Repeat unsub/approve and expiry must be idempotent without double-apply or audit noise.

**Approach:** Harden outcomes already sketched in 6.1/6.3; add edge tests; no new product surface.

</frozen-after-approval>

## Implementation Notes

ALREADY/EXPIRED/ALREADY_CLEAR already skip audit; edge tests added.

## Review Triage Log

- behavior already correct from 6.1/6.3 — story delivers verification + pages for EXPIRED
