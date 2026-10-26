---
title: 'List-Unsubscribe-Post and scoped visibility'
type: 'feature'
ticket: '2'
created: '2026-10-26'
status: 'done'
baseline_revision: '44d82d9'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self', 'edge-case-hunter']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** One-click List-Unsubscribe-Post and operator visibility of per-list unsub state (scoped for LM).

**Approach:** POST /unsubscribe/{token} (RFC 8058); surface unsubscribed on Member detail (already scoped); add list-members unsub column if missing.

</frozen-after-approval>

## Implementation Notes

POST One-Click; GET /api/lists/{id}/members with unsubscribed; Members detail already scoped.

## Review Triage Log

- frontend Members UI — deferred (API surfaces AC; Compose SPA has no Members page yet)
- empty POST body rejected — intentional RFC-strict
