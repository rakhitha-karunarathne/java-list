---
title: 'Parameters Public base URL seed and secrets placement'
type: 'feature'
ticket: '6'
created: '2026-10-25'
status: done
baseline_revision: '301d648a7d51c0d9c8417076a309d0f6d7312117'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self', 'bugbot']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Operators need a seeded Public base URL Parameter and clear docs that secrets stay in env and Flexible TLS is unsupported.

**Approach:** Seed Parameter keys via ApplicationRunner from `mailer.public-base-url`; document secrets/HTTPS/Compose shapes; keep JWT/mailbox secrets out of Parameters.

</frozen-after-approval>

## Implementation Notes

Probe `GET /api/parameters/public-base-url` (JWT) until catalogs Admin UI. Seed-if-absent; reject non-https base URL.

## Review Triage Log

- 2026-10-25 bugbot: unconditional overwrite → seed-if-absent; HTTPS validation added; deploy.md UTF-8 fixed.
