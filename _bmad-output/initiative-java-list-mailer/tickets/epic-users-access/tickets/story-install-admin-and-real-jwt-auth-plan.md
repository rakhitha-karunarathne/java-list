---
title: 'Install Admin and real JWT auth'
type: 'feature'
ticket: '1'
created: '2026-10-25'
status: done
baseline_revision: '1673f7a'
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

**Problem:** Operator API still authenticates against env bootstrap credentials; need a real user row and install Admin.

**Approach:** Flyway `users` table; seed Admin once from bootstrap env with BCrypt; login verifies DB hash and issues JWT with user id + role.

</frozen-after-approval>

## Implementation Notes

Env bootstrap is install-only. Roles enum includes LIST_MANAGER/LIST_SENDER for later stories; seed uses ADMIN. EnabledUserFilter rejects JWT for disabled/missing users.

## Review Triage Log

- 2026-10-25 bugbot: disabled JWT still valid → EnabledUserFilter; blank bootstrap → fail-fast; seed race → catch unique violation.
