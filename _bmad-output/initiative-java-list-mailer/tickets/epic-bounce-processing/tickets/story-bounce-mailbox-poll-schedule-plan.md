---
title: 'Bounce mailbox poll schedule'
type: 'feature'
ticket: '2'
created: '2026-10-26'
status: 'done'
baseline_revision: '2d114f1'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Catalogued bounce mailboxes are never polled; need scheduled sequential IMAP ingest.

**Approach:** BouncePollProcessor @Scheduled with non-overlap; persist bounce_events; OpsHooks; no disable.

</frozen-after-approval>

## Implementation Notes

V16; BouncePollService sequential + non-overlap; enabled skip.

## Review Triage Log

- GreenMail poll IT — unit mock coverage; IMAP covered in 7.1
- enabled API on catalog CRUD — entity field only; Admin toggle deferred
