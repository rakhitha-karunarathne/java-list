---
title: 'Notice sending path'
type: 'feature'
ticket: '4'
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

**Problem:** System mail needs exactly one designated Notice sending path, separate from campaign queue paths.

**Approach:** Singleton notice_sending_path row; GET/PUT /api/notice-sending-path.

</frozen-after-approval>

## Implementation Notes

Campaign queue never reads this row (enforced later in send epic).

## Review Triage Log
