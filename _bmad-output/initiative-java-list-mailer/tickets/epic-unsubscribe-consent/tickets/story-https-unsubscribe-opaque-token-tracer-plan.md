---
title: 'HTTPS unsubscribe opaque token tracer'
type: 'feature'
ticket: '1'
created: '2026-10-26'
status: 'done'
baseline_revision: '17407bf'
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

**Problem:** Campaign sends use GUID unsub links with no public handler; AD-18 AES-GCM and membership unsub are missing.

**Approach:** Kernel AesGcmOpaqueTokenHelper; seal list-set snapshot at send; GET /unsubscribe/{token}; List-Unsubscribe header; audit via OpsHooksPort; GreenMail tracer.

</frozen-after-approval>

## Implementation Notes

V15 widen unsub_token; UnsubscribeController; AES-GCM seal; List-Unsubscribe (+ Post header for 6.2).

## Review Triage Log

- List-Unsubscribe-Post header shipped early with GET — intentional for RFC clients; POST handler is story 6.2
- enabled flip guard is defensive no-op — left as C3 belt
- HITL flag on story — developed per user batch request
