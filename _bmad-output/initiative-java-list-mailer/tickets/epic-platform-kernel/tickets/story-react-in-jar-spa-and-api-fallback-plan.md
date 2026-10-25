---
title: 'React-in-jar SPA and API fallback'
type: 'feature'
ticket: '3'
created: '2026-10-25'
status: done
baseline_revision: '8fb9b1b33d995403464775287c42dff5d923add6'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Operators need a React SPA served from the Boot jar with JSON under `/api` and HTML fallback for client routes.

**Approach:** Add Vite React 19 frontend under `mailer/frontend`, package build output into Boot static resources, expose `/api/ping`, and forward non-API routes to `index.html`.

</frozen-after-approval>

## Implementation Notes

Vite outDir → `src/main/resources/static`; frontend-maven-plugin on generate-resources. SpaWebFilter forwards extensionless GET routes. Dockerfile copies `frontend/` for in-image builds.

## Plan Change Log

## Review Triage Log

- 2026-10-25 quick-self: /api/ping ok; / and /campaigns return index.html 200; SpaWebFilterTest green. Dockerfile fixed to COPY frontend (was BUILD FAILURE without it).
