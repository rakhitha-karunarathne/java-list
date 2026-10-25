---
title: 'Thin kernel ports GUID filter chains'
type: 'feature'
ticket: '4'
created: '2026-10-25'
status: done
baseline_revision: '2458e8de8b98ae87549b7020b34fb6c5d4b37a21'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Feature slices need kernel ports, GUID helper, opaque-token stub, and empty plugin filter-chain registration before auth and catalogs land.

**Approach:** Add kernel package stubs for Parameters/SMTP/Mailbox/Scheduler/OpaqueToken, Guids helper, and FilterChainRegistry with HTTP interceptor (empty chains).

</frozen-after-approval>

## Implementation Notes

Stubs in KernelStubConfig; FilterChainRegistry + HttpFilterChainInterceptor on /api and public path prefixes.

## Plan Change Log

## Review Triage Log

- 2026-10-25 quick-self: GuidsTest + FilterChainRegistryTest green; no high findings.
