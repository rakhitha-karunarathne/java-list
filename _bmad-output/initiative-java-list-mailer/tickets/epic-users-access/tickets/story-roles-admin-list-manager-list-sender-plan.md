---
title: 'Roles Admin List manager List sender'
type: 'feature'
ticket: '2'
created: '2026-10-25'
status: done
baseline_revision: '4cb3519'
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

**Problem:** All authenticated operators share the same /api access; roles must gate Admin surfaces.

**Approach:** Map JWT role claim to ROLE_* authorities; restrict /api/users and /api/parameters to ADMIN; Admin CRUD to create List manager/sender users.

</frozen-after-approval>

## Implementation Notes

List-scoped routes land with grants story; this story gates Admin-only APIs. EnabledUserFilter refreshes ROLE_* from DB each request.

## Review Triage Log

- 2026-10-25 bugbot: stale JWT role → refresh authorities from DB; last-admin guard; duplicate email → 409.
