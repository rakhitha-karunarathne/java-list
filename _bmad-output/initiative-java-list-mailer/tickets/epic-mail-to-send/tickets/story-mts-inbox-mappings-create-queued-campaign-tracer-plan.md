---
title: 'MTS inbox mappings create Queued campaign tracer'
type: 'feature'
ticket: '1'
created: '2026-10-26'
status: 'done'
baseline_revision: '308f9d1'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** No MTS inbox/mapping path to create Campaigns from authorized IMAP mail.

**Approach:** MTS catalog tables + poll via mailbox-query; authorized accept creates Campaign then queueAndSend (Queued); minimal Admin API + index UI; gates stubbed pass until stories 2–3.

</frozen-after-approval>

## Implementation Notes

V19; Admin API; Inboxes UI; GreenMail tracer Queued.

## Review Triage Log

- multi-mapping selection uses first mapping — shortcut: pick by To/subject later
- auth/spam/From gates deferred to 8.2–8.3 stubs
