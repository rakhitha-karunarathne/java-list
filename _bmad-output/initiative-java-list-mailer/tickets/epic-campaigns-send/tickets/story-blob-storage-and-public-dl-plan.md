---
title: 'Blob storage and public dl'
type: 'feature'
ticket: '5'
created: '2026-10-26'
status: 'built'
baseline_revision: '92ab98d36d402c0557149a33f01e6c5638299f75'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['blind-hunter', 'edge-case-hunter']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Campaign images need public `/dl/<campaignGuid>/…` serving from DB blobs with HTML rewrite and cascade delete.

**Approach:** Serve campaign_attachments as public blobs at `/dl/{id}/{filename}`; rewrite HTML img refs on send; add Campaign delete that cascades blobs.

</frozen-after-approval>

## Implementation Notes

- PublicDlController; CampaignHtmlRewriter on send/test-send; DELETE campaign cascades attachments/queue/lists.
- Review: /dl images-only; filename path reject; unique (campaign,filename); skip public base when no blobs.

## Review Triage Log

- high public non-image attachments — patched image/* only on /dl
- high path traversal filename — patched reject .. / \
- medium duplicate filename NonUniqueResult — patched V12 unique constraint
- medium rewrite misses href/srcset — deferred (img src forms covered)
- medium delete auth not list-scoped — deferred (same as other campaign endpoints)
- low Cache-Control after delete — deferred
