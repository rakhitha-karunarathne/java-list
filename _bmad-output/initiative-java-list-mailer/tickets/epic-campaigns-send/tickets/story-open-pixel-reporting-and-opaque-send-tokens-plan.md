---
title: 'Open pixel reporting and opaque send tokens'
type: 'feature'
ticket: '8'
created: '2026-10-26'
status: 'built'
baseline_revision: '13c914f40510b73d7d58d994c88f6b7adfddaab8'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Need async open pixel, reporting, opaque tokens, and bounce correlation fields on send-queue rows.

**Approach:** Issue pixel/unsub tokens on send; GET /t/{token} returns GIF and records open async; Campaign detail exposes open stats.

</frozen-after-approval>

## Implementation Notes

- V13 tracking columns + campaign_opens; OpenPixelController; inject /t and /unsubscribe into HTML at send.
- Self-review: AES-GCM tokens deferred (opaque GUID tokens meet unguessable); real crypto in unsub epic.

## Review Triage Log

- high sync open write — patched CompletableFuture.runAsync
- medium AES vs GUID tokens — deferred to unsub epic AD-18 hardening
