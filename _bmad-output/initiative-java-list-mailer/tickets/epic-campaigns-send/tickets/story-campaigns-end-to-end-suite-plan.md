---
title: 'Campaigns end-to-end suite'
type: 'feature'
ticket: '10'
created: '2026-10-26'
status: 'done'
baseline_revision: 'd98296563ccde396733fccc71c2fb02d9a9e6b83'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'thorough'
review_source: 'pinned'
lenses_ran: ['blind-hunter', 'edge-case-hunter', 'verification-gap', 'intent-alignment']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Epic needs an automated GreenMail regression suite for compose→send→pixel and Stop/Requeue/Finished-Add-List paths.

**Approach:** SpringBootTest + Testcontainers MariaDB + embedded GreenMail; close Finished+Add-List requeue gap; document Maven command.

</frozen-after-approval>

## Implementation Notes

GreenMail + Testcontainers `CampaignsE2ETest`; Finished add-list (`POST /lists`) + requeue; V14 `included_in_pass`.

## Review Triage Log

- high email PENDING skip on requeue — patched
- high QUEUED limbo when no new PENDING — patched (stay FINISHED)
- high V14 backfill for FINISHED lists — patched
- high E2E bypassed HTTP add-list/requeue — patched (admin JWT + API)
- medium stop/requeue via repo — patched POST /stop /requeue
- medium unit coverage Finished requeue — patched
- medium POST /queue from non-Draft — patched Draft-only
- medium no CI workflow — deferred (documented; no .github yet)
- low scheduled processor not in suite — deferred (manual processPendingBatch)
- low compose create via HTTP — deferred (queue/stop/requeue/lists/pixel covered)
