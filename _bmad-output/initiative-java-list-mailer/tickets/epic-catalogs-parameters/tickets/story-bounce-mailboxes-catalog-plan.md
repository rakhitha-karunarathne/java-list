---
title: 'Bounce mailboxes catalog'
type: 'feature'
ticket: '2'
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

**Problem:** Admins need bounce mailbox catalog with default 15-minute poll.

**Approach:** CRUD under /api/bounce-mailboxes; pollIntervalMinutes defaults to 15 when omitted/zero.

</frozen-after-approval>

## Implementation Notes

Password omitted from GET detail.

## Review Triage Log

- 2026-10-25: unit test defaults poll to 15.
