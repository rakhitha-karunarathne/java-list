---
id: 6
type: story
title: "Frontend Clearwater end-to-end walkthrough suite"
parent: epic-frontend-ops-mts
covers: [R1, R4]
after: [5]
hitl: true
risk: medium
refined: true
---

# Frontend Clearwater end-to-end walkthrough suite

## Description

Closing gate for frontend epics: documented walkthrough (and light automated checks where cheap) covering shell role nav, one catalog path, Lists/Members smoke, Compose queue smoke against test profile, MTS index, Attention highlight. Confirms Clearwater SPA covers EXPERIENCE IA without API tools for the happy path.

## Requirements

1. **Shell roles** — Admin vs List sender nav differ (Attention/Users/Parameters).
2. **Catalog** — Create or open one Sending path from SPA.
3. **Lists/Members** — Open Lists index and Members search (seeded data OK).
4. **Compose** — Open Compose; Save Draft smoke (Queue optional if GreenMail in profile).
5. **MTS** — Open MTS index.
6. **Attention/Ops** — Admin sees Attention and Ops Logs indexes.
7. **C8** — No detail entities in primary sidebar during walkthrough.
8. **Artifact** — Checklist committed under `docs/` or tickets notes with pass/fail.

## Boundaries

- Does not re-test backend bounce/MTS matrices.
- Out: full Playwright suite unless already in repo — prefer checklist + few smoke tests.

## References

- parent — epic-frontend-ops-mts.md
- ux — EXPERIENCE IA table; mockups/clickable/

## Notes

- Decision: 2026-10-26 — Closing FE suite after polish sweep.
