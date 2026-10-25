---
title: 'MFA mandatory Parameter enforcement'
type: 'feature'
ticket: '5'
created: '2026-10-25'
status: done
baseline_revision: 'c646126'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self', 'bugbot']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** When MFA-mandatory Parameter is on, operators without TOTP must enroll before getting a session.

**Approach:** Login reads ParametersPort; unenrolled users get enrollmentToken limited to /api/me/mfa; unenroll blocked while mandatory.

</frozen-after-approval>

## Implementation Notes

Admin can PUT /api/parameters/operator.mfa.mandatory until catalogs UI. EnabledUserFilter also blocks full JWTs when mandatory and user not enrolled.

## Review Triage Log

- 2026-10-25 bugbot: existing JWT bypass → filter checks mandatory; enroll path matcher tightened.
- 2026-10-25 smoke: enrollmentRequired, enroll-ping 401, unenroll 409 while mandatory.
