---
title: 'Consent clear-unsub via notice-mail'
type: 'feature'
ticket: '3'
created: '2026-10-26'
status: 'done'
baseline_revision: 'f4da658'
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

**Problem:** Operators cannot clear unsub without Member consent on Notice path; /approve missing.

**Approach:** NoticeMailService via Notice sending path; consent AES-GCM tokens (14d); POST request-consent; GET /approve/{token}; no unilateral clear API.

</frozen-after-approval>

## Implementation Notes

NoticeMailService; ConsentService; /approve; POST /api/members/{id}/consent-clear-unsub.

## Review Triage Log

- consent.expiry Parameter not in ParameterDefinitions seed — optional override via raw key; default 14d hardcoded path OK for V1
- GreenMail consent IT — deferred to 6.4 edge suite / unit coverage sufficient
