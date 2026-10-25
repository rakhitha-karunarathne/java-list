---
title: 'Compose Draft queue GreenMail tracer'
type: 'feature'
ticket: '1'
created: '2026-10-25'
status: done
baseline_revision: '19e23a2'
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

**Problem:** Need Draft→Queue→Send→Finished tracer through smtp-adapter for one List.

**Approach:** Campaigns + send_queue tables; JavaMailSmtpAdapter; POST /queue builds queue from enabled members and sends synchronously using first-list path.

</frozen-after-approval>

## Implementation Notes

Point a Sending path at GreenMail (host greenmail / localhost port 3025) under Compose profile test.

## Review Triage Log
