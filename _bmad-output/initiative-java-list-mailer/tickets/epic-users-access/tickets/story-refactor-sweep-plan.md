---
title: 'Refactor sweep'
type: 'chore'
ticket: '6'
created: '2026-10-25'
status: done
baseline_revision: '60f820d'
route: 'oneshot'
route_source: 'pinned'
risk: 'low'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Users epic left controller coupling on JWT minting.

**Approach:** Extract AccessTokenService; re-smoke Admin login.

</frozen-after-approval>

## Implementation Notes

No other deferred high findings.

## Review Triage Log

- 2026-10-25: AccessTokenService extraction; mvn test + login smoke.
