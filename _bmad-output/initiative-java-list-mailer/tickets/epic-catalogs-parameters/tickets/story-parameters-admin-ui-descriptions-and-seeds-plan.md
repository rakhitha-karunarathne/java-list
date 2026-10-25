---
title: 'Parameters Admin UI descriptions and seeds'
type: 'feature'
ticket: '5'
created: '2026-10-25'
status: done
baseline_revision: 'b175fba'
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

**Problem:** Parameters need durable storage, Admin UI with descriptions, and product seed defaults.

**Approach:** Flyway parameters table; JpaParametersPort; ParameterDefinitions; SPA Parameters page; GET/PUT /api/parameters.

</frozen-after-approval>

## Implementation Notes

Replaces in-memory ParametersPort stub.

## Review Triage Log

- 2026-10-25 bugbot: retention validation + clear save errors.
- 2026-10-25 smoke: list shows descriptions; defaults MFA/VERP off; Public base URL editable.
