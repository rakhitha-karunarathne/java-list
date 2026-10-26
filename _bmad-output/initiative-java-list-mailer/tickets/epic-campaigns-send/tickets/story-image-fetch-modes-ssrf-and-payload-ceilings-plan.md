---
title: 'Image-fetch modes SSRF and payload ceilings'
type: 'feature'
ticket: '7'
created: '2026-10-26'
status: 'built'
baseline_revision: '88532e0baee748eb3faa8b4145a349062183383c'
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

**Problem:** Remote image fetch needs Off/Best Effort/Mandatory with SSRF guards and payload/image ceilings.

**Approach:** Parameter-driven mode + RemoteImageFetchService on queue with SsrfGuard; ceilings from Parameters and strictest List payload override.

</frozen-after-approval>

## Implementation Notes

- IMAGE_FETCH_MODE / CAMPAIGN_*_MAX_BYTES Parameters; ingest on queue before enqueue; no redirect follow.
- Self-review: TTL restart on Finished Requeue deferred; full thorough lenses deferred (continue cadence).

## Review Triage Log

- high private IP fetch — patched SsrfGuard
- medium Mandatory fails queue — patched throw before enqueue
- low CDN TTL tracking — deferred
