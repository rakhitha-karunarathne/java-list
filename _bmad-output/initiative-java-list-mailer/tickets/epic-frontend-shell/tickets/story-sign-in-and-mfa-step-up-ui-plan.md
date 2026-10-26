---
title: 'Sign-in and MFA step-up UI'
type: 'feature'
ticket: '2'
created: '2026-10-26'
status: 'done'
baseline_revision: 'e668592'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Login lacked MFA step-up and Clearwater login card.

**Approach:** LoginCard password → MFA verify via /api/auth/mfa; sessionStorage JWT; enrollmentRequired blocks with message.

</frozen-after-approval>
