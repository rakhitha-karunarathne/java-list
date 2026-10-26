---
title: 'Bounce correlation ladder and safety'
type: 'feature'
ticket: '3'
created: '2026-10-26'
status: 'done'
baseline_revision: 'dfbc5c8'
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

**Problem:** Ingested bounces need VERP→token→Message-ID→subject+email correlation; uncorrelated must never disable.

**Approach:** V17 correlation fields on send_queue/bounce_events; BounceCorrelationService ladder; Attention on miss.

</frozen-after-approval>

## Implementation Notes

X-JavaList-Token at send; correlatePending; no Member disable here.

## Review Triage Log

- subject+email uses findAll scan — shortcut: index later when volume grows
- VERP match reuses token id in bounce+ path
