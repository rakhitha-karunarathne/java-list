---
title: 'Multi-list dedupe and content compose'
type: 'feature'
ticket: '2'
created: '2026-10-26'
status: 'built'
baseline_revision: '12c2f197f8f00e933dfefd33bd35bde54995e58d'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['blind-hunter', 'edge-case-hunter', 'verification-gap', 'intent-alignment']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Campaign compose lacks multi-list UI, address-level enqueue dedupe, ordered first-list Sending path, multipart HTML/text+attachments, preview, and queue validation for empty bodies/lists.

**Approach:** Extend campaigns API + send path for ordered lists, email dedupe, multipart SMTP and draft update; add Compose SPA with multi-select, editors, and HTML/Text preview.

</frozen-after-approval>

## Implementation Notes

Oneshot: story criteria are complete; tracer 5.1 already has create/queue/send skeleton. Extend that path rather than new packages. Images as referenced URLs in HTML for this story; blob `/dl` is 5.5. Attachments stored as DB rows on the campaign for MIME parts.

- Address dedupe via lowercased email key; ordered lists via `position` + V11 backfill.
- Multipart SMTP in JavaMailSmtpAdapter; Compose SPA view in App.jsx.
- Review patches: null-body 400, PUT null preserves lists/attachments, duplicate list reject, ordered query tie-break, attachment/path/empty-body tests.

## Review Triage Log

- high null create/update body NPE — patched restore body==null → 400
- high PUT null attachments/lists wipe — patched null = leave unchanged
- medium duplicate listIds PK fail — patched HashSet size check
- medium position ties after migration — patched ROW_NUMBER backfill + OrderBy position,listId
- medium missing path/attachment/empty-body tests — patched CampaignSendServiceTest
- medium Compose save/queue unhandled rejection — patched try/catch + body reconcile
- low no draft resume/list picker after refresh — deferred
- low attachment size caps / MIME filename encoding — deferred
- low HTML preview XSS for pasted content — deferred (operator-authored)
- false “member-id vs email” for unique-email schema — same member on two lists already covered; email key still required by R1
- verification-gap MIME adapter unit test — deferred (service now asserts attachments forwarded)
- intent-alignment draft empty bodies allowed until queue — accepted (R3 split)
