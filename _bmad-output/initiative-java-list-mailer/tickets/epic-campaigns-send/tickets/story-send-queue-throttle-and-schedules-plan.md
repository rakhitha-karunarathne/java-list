---
title: 'Send-queue throttle and schedules'
type: 'feature'
ticket: '6'
created: '2026-10-26'
status: 'built'
baseline_revision: '37c5764e56521d435a52152d06b88bcb4977a31b'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Campaign queue still sent SMTP inline; need system processor with throttle and non-overlapping schedule ticks.

**Approach:** Enqueue-only on Queue/Requeue; `@Scheduled` SendQueueProcessor drains PENDING under Parameter throttle with in-process non-overlap.

</frozen-after-approval>

## Implementation Notes

- `queueAndSend` leaves QUEUED; `processPendingBatch` + `SendQueueProcessor.tick` drain; Parameter `send.queue.throttle.per.minute`.
- Self-review: STOPPED skipped; empty tick quiet; Notice path unused (row path from enqueue). Full lens pass deferred to next iteration if CI flags.

## Review Triage Log

- high sync SMTP on queue — patched split enqueue/processor
- medium tests updated for processPendingBatch
- low Compose UX still shows immediate Finished expectation — patched message text
- maybe-false throttle sleep timing under load — verify with GreenMail soak in 5.10
