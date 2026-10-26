---
id: 8
type: story
title: "User detail page sections (Account, List Grants, Security)"
parent: epic-frontend-catalogs
covers: [R6]
after: [4]
hitl: false
risk: low
refined: true
status: done
---

# User detail page sections (Account, List Grants, Security)

## Description

User detail is one job per white card: **Account** (Role, Enabled, Save), **List Grants**, **Security** (MFA). Establish the reusable **page sections** pattern — when a page has multiple jobs, break the white box and start a new card — documented for later pages.

## Requirements

1. **Three section cards**
   **Given** Admin opens a User detail
   **When** the page loads
   **Then** three separate white rounded cards appear (not one combined card)
   **And** card 1 has Role, Enabled, and Save user
   **And** card 2 has List Grants (or Admin hint when grants do not apply)
   **And** card 3 has Security / MFA

2. **Page chrome**
   **Given** User detail
   **When** rendered
   **Then** back control and user email heading sit above the section stack (on the canvas), not buried inside only the first card

3. **Pattern documented**
   **Given** DESIGN / EXPERIENCE
   **When** operators later section other pages
   **Then** the page-sections pattern is described: one job per `.card`; stack with gap; do not nest major jobs in one white box

## Boundaries

- In: User detail layout + CSS `.page-sections` + spine docs.
- Out: Sectioning other pages (follow-up when named).

## References

- EXPERIENCE / DESIGN — Page sections
- parent — epic-frontend-catalogs.md

## Notes

- Decision: 2026-10-26 — User detail is the first page-sections exemplar.

## Dev Agent Record

### Completion Notes

- `.page-sections` + `.page-sections-head` in `index.css`; User detail = Account / List Grants / Security cards.
- EXPERIENCE + DESIGN document the pattern for later pages.

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| low | MFA panel had extra top margin inside single card | Fixed — panel is content-only inside its section card |
| low | Admin had no List Grants “box” (hint inline) | Fixed — always show List Grants card (hint when N/A) |
| info | Other pages not sectioned yet | Out of scope — wait for user list |
