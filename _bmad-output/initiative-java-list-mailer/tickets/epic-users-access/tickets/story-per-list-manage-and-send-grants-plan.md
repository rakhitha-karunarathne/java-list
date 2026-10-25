---
title: 'Per-list manage and send grants'
type: 'feature'
ticket: '3'
created: '2026-10-25'
status: done
baseline_revision: '20529d0'
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

**Problem:** Non-Admin operators need per-list manage/send grants that take effect immediately.

**Approach:** Persist grants; Admin replaces grants on user detail; ListAccessService gates list APIs; Admin bypasses grants.

</frozen-after-approval>

## Implementation Notes

`GET /api/lists/{id}` probe until lists epic. Grant removal is replace-all PUT.

## Review Triage Log

- 2026-10-25 bugbot: null grants body → 400; trim listId; IdClass getters.
- 2026-10-25 smoke: grant → 200; clear grants → 403; Admin bypass ok.
