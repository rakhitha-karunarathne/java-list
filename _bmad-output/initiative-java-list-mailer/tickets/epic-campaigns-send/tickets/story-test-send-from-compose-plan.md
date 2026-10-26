---
title: 'Test send from compose'
type: 'feature'
ticket: '3'
created: '2026-10-26'
status: 'built'
baseline_revision: '5087269870277a9729672f19abb71a04a41e5764'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['blind-hunter', 'edge-case-hunter', 'verification-gap', 'intent-alignment']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Compose has no Test send; operators cannot verify SMTP/content without queueing, and failures lack ops/Attention hooks.

**Approach:** Add test-send API + Compose UI that mails pasted addresses via first-list path without queue/stats; stub ops/Attention emission on SMTP failure.

</frozen-after-approval>

## Implementation Notes

- POST `/api/campaigns/{id}/test-send`; `OpsHooksPort` + `LoggingOpsHooks` (capped); Compose Test send with busy guard.
- Review patches: stronger tests (path/subject/invalid/502), hook cap, busy button.

## Review Triage Log

- medium partial multi-recipient send — deferred (all-or-nothing UX with 502 after partial)
- medium concurrent Test send — patched busy flag
- medium weak tests — patched path/subject/invalid/502 assertions
- medium unbounded LoggingOpsHooks — patched CAP 500
- low queue SMTP lacks ops hooks — deferred (epic 9 + this story only owns test-send emission)
- verification-gap controller HTTP test — deferred
- false disable-bypass test name — renamed to reflect first-path/subject/queue-skip

## Plan Change Log
