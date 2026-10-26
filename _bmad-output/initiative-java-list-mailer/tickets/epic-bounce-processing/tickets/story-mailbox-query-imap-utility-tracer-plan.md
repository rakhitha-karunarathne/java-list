---
title: 'mailbox-query IMAP utility tracer'
type: 'feature'
ticket: '1'
created: '2026-10-26'
status: 'done'
baseline_revision: 'acdc666'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['quick-self', 'edge-case-hunter']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** MailboxPort is a stub; bounce/MTS need shared IMAP stream poll with low memory.

**Approach:** Spec-based MailboxPort + Jakarta Mail IMAP implementation; GreenMail tracer; document contract for epic 8.

</frozen-after-approval>

## Implementation Notes

ImapMailboxQuery; docs/mailbox-query.md; GreenMail tracer.

## Review Triage Log

- getMessages() returns Message stubs (bodies on demand) — acceptable V1 streaming
- UID incremental watermark — deferred until scale needs
