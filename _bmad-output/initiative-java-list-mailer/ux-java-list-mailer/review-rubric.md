# Spine Pair Review — java-list

## Overall verdict

The Clearwater DESIGN spine is mechanically sound and well-shaped for downstream token consumption. EXPERIENCE commits the core IA discipline (detail-not-nav, Parameters, login/MFA placement, Compose/test send) but leaves large swaths of initiative Requirements R1–R34 without named Key Flows or IA rows—especially mail-to-send (Drop log, Issues, Global blocks), member HTTPS surfaces, Notice path, bounce log, and campaign/list operational journeys. Component and state contracts are pattern-level generics rather than per-surface coverage. **Adequate for early architecture/story slicing on catalogs + campaigns; thin as a full product UX contract until mock-aligned surfaces and flows are promoted into EXPERIENCE.**

## 1. Flow coverage — thin

Checked initiative `initiative-java-list-mailer.md` Requirements R1–R34 against EXPERIENCE.md IA, State Patterns, Component Patterns, and Key Flows (4 flows).

### Findings

- **[critical]** R14–R15 (HTTPS unsubscribe, consent clear-unsub) and R34 member/operator HTTPS surfaces have no IA rows or Key Flow; mock implements `#unsub` / `#consent` under “Member Surfaces” (`mockups/clickable/index.html`) but spines omit them (EXPERIENCE.md §Information Architecture). *Fix:* Add member-surface IA entries (out-of-sidebar, reached from email links) plus one Key Flow with failure paths (expired consent, already unsubscribed).
- **[high]** R6 (Notice sending path), R30 (From-domain Issues), R32 (Drop log + global blocks) appear in mock sidebar but are absent from EXPERIENCE IA (EXPERIENCE.md §Information Architecture vs `mockups/clickable/index.html` nav). *Fix:* Add IA rows with role scope and link mock; add at least one MTS/Issues flow with climax + hold/delete-and-notify failure path (R30).
- **[high]** R8 (Attention role scoping: Admin all + ops highlight, LM list/member only, List sender none) is only partially stated (IA row + generic ops failure state); no Key Flow demonstrating LM vs Admin vs sender (EXPERIENCE.md §Information Architecture, §State Patterns). *Fix:* Flow with named LM protagonist + explicit “no Attention for List sender” beat; Admin ops-error highlight as climax.
- **[high]** R1 install/first Admin bootstrap and Admin disable-user journeys have no Key Flow or IA beyond generic Users (initiative R1; EXPERIENCE.md Key Flows). *Fix:* Short Admin onboarding flow (first login → Parameters/Users) or explicit “install-time only, no UX beyond first Admin” decision in Foundation.
- **[medium]** R2 (list manage/send grants on User detail) and R11 (bulk-paste add with skip counts) lack dedicated flows; only mentioned in IA / Flow 4 parenthetical (EXPERIENCE.md §Information Architecture, Flow 4). *Fix:* Key Flow for grant edit + immediate effect; bulk-add flow with invalid/dedupe count climax.
- **[medium]** R18 (multi-list campaign, first list’s path), R22–R23 (image-fetch modes, payload/TTL ceilings), R24 (campaign reporting stats), R26–R27 (uncorrelated bounce → Attention) have no Key Flow or behavioral pattern (initiative R18–R24, R26–R27; EXPERIENCE.md). *Fix:* Compose/campaign flow extension or separate flows referencing requirement IDs in prose.
- **[medium]** R28 (VERP toggle on Parameters) not named in Flow 1 or Parameter examples beyond MFA/retention (EXPERIENCE.md Flow 1 step 3). *Fix:* Name VERP in Parameter flow example or Component Patterns.
- **[medium]** Flow 1 (Parameters) and Flow 2 (Login MFA) omit failure paths where applicable (save/validation failure; wrong/expired TOTP) despite rubric expectation (EXPERIENCE.md Flows 1–2). *Fix:* Numbered failure branch or pointer to §State Patterns with surface-specific copy.
- **[low]** R21 (queue processor), R31 (MTS auth matrix), R34 (TLS mode) are operator-backend or config-heavy with minimal UX need; acceptable omission if documented in Foundation as “no operator UI” (EXPERIENCE.md §Foundation). *Fix:* One-line Foundation boundary per requirement to prevent story rediscovery.
- **[low]** Flow 4 (detail-not-nav) has no failure path (EXPERIENCE.md Flow 4). *Fix:* Optional “404/deep-link to forbidden detail” branch for permission-denied.

## 2. Token completeness — adequate

Extracted YAML frontmatter and all `{path.to.token}` references in DESIGN.md body; cross-checked EXPERIENCE token refs.

### Findings

- **[medium]** Load-bearing contrast targets live only in EXPERIENCE §Accessibility Floor, not in DESIGN §Colors (EXPERIENCE.md lines 97–98; DESIGN.md §Colors). *Fix:* Add contrast note in DESIGN Colors for ink-on-surface, ink-on-canvas, white-on-accent (WCAG 2.2 AA target).
- **[low]** Badge danger/warning variants referenced in DESIGN §Components prose without `components.badge-danger` / `badge-warning` tokens (DESIGN.md lines 194–195). *Fix:* Add component token objects or state “use `{colors.danger}` background” explicitly in badge spec.
- **[low]** `{components.card.shadow}` uses raw CSS string while other tokens use refs—valid but inconsistent for codegen (DESIGN.md YAML line 94). *Fix:* Optional named shadow token under `spacing` or document as intentional exception.

No undefined `{path.to.token}` references found. All color YAML keys have hex in frontmatter and table.

## 3. Component coverage — thin

Union of named components/patterns across DESIGN §Components and EXPERIENCE §Component Patterns.

### Findings

- **[high]** DESIGN-only components lack EXPERIENCE behavioral rows: Primary button, Ghost/secondary, Card, Input, Badge, Table (DESIGN.md §Components; EXPERIENCE.md §Component Patterns). *Fix:* Add behavioral rules (e.g., Table row click, destructive confirm on primary danger actions).
- **[high]** EXPERIENCE-only patterns lack DESIGN visual specs: Compose preview (tabs/sync), Test send (dialog), Used By (link list), Attention error rows (highlight treatment), MFA Security panel (DESIGN.md §Components; EXPERIENCE.md §Component Patterns). *Fix:* Extend DESIGN Components for dialog, attention-row highlight using `{colors.warning}`/`{colors.danger}`, Security panel anatomy.
- **[medium]** “Sidebar nav” (EXPERIENCE) vs “Sidebar” + “nav-active” (DESIGN)—same intent, inconsistent naming (both spines). *Fix:* Align glossary: one canonical name with cross-ref.
- **[medium]** Dialogs referenced in Interaction Primitives without component entry in either spine (EXPERIENCE.md §Interaction Primitives line 92). *Fix:* DESIGN dialog + EXPERIENCE confirm/MFA/test-send behavior row.

## 4. State coverage — thin

Walked IA surfaces in EXPERIENCE §Information Architecture against §State Patterns (generic table only).

### Findings

- **[high]** Per-surface empty/cold-load/error/permission not mapped for: Attention (empty inbox), Campaigns index, Campaign detail, Mail-to-send index/detail, Bounce mailboxes, Ops Logs, Audit, Sign in (lockout/wrong password), catalog indexes (Sending paths, Footers) (EXPERIENCE.md §Information Architecture vs §State Patterns). *Fix:* Surface × state matrix (can follow Drift example shape) or extend State Patterns with surface column.
- **[medium]** List sender “no Attention inbox” (R8) is not a State Pattern row—only mock hint (EXPERIENCE.md; mock line 346). *Fix:* Permission-denied / feature-hidden state for Attention nav item.
- **[medium]** Delete blocked + Used By covered generically; catalog-specific delete confirm not tied to Card/Table surfaces (EXPERIENCE.md §State Patterns). *Fix:* Tie to Used By pattern on detail pages.
- **[low]** Loading uses “skeleton or quiet spinner” without choosing default (EXPERIENCE.md §State Patterns). *Fix:* Pick one for indexes vs details.

## 5. Visual reference coverage — adequate

Listed `mockups/` (2 files), `wireframes/` (none), `imports/` (none).

### Findings

- **[medium]** `.working/clickable-mockup/index.html` cited as composition reference alongside promoted mock (EXPERIENCE.md line 51) but lives outside `mockups/`; spines-win stated once ✓. Risk of stale duplicate chrome (`.memlog.md` line 39). *Fix:* Drop `.working/` link or mark deprecated; single canonical `mockups/clickable/index.html`.
- **[low]** Many mock pages (Drop log, Bounce log, Issues, Notice path, member surfaces) not named in spine prose—only “full page map” (EXPERIENCE.md line 51). *Fix:* Bullet list of mock sections IA must match.
- **[low]** `ux-java-list-mailer.md` links mockups correctly; no orphan files in `mockups/`.

## 6. Bloat & overspecification — strong

### Findings

- **[low]** EXPERIENCE §Foundation repeats stack context (JWT, sessionStorage) that architecture owns (EXPERIENCE.md lines 17–18). *Fix:* Trim to “see architecture” if consumers split cleanly.

No material overspecification; prose is compact relative to initiative scope.

## 7. Inheritance discipline — adequate

### Findings

- **[high]** Audit IA row deferring to mock (“As specified in mock/IA”) breaks ticket-as-source discipline (EXPERIENCE.md line 47; initiative R33). *Fix:* Commit sidebar group + Admin/LM scope verbatim from R33.
- **[medium]** Requirement/journey names (Notice sending path, Drop log, Global blocks, Issues, Bounce log) appear in initiative + mock but not EXPERIENCE glossary/IA (initiative R6, R30, R32; EXPERIENCE.md). *Fix:* Verbatim naming from initiative Requirements in IA table.
- **[medium]** EXPERIENCE `{../DESIGN.md}` path reference is non-standard vs `{DESIGN.md}` or relative link used elsewhere (EXPERIENCE.md line 15). *Fix:* Consistent cross-spine link pattern for extractors.
- **[low]** EXPERIENCE token refs `{colors.accent-subtle}`, `{colors.danger}`, `{colors.focus-ring}` resolve to DESIGN YAML ✓ (EXPERIENCE.md §Component Patterns, §State Patterns, §Accessibility Floor).
- **[low]** `sources` frontmatter lists initiative + PRD paths (EXPERIENCE.md lines 4–6) ✓.

## 8. Shape fit — adequate

### Findings

- **[medium]** Inspiration & anti-patterns section absent though `.memlog.md` records phpList job map, rejected dense phpList chrome, and explicit visual rejects (`.memlog.md` lines 8–31; validate.md shape rule). *Fix:* Add §Inspiration & Anti-patterns (phpList IA lift, SaaS chrome reject list).
- **[low]** DESIGN.md section order matches canonical spec (Brand → … → Do's and Don'ts) ✓ (`references/design-md-spec.md`).
- **[low]** EXPERIENCE required defaults present; Responsive & Platform included appropriately (multi-breakpoint admin) ✓ (EXPERIENCE.md).

## Mechanical notes

- **Frontmatter:** Both spines `status: final`, dates 2026-10-25; DESIGN YAML includes `status`/`created`/`updated`; EXPERIENCE lacks `description` field (optional per spec).
- **Naming:** “Compose” vs mock `campaign-compose`; “Mail-to-send” vs mock “Inboxes & Mappings”—document alias in IA.
- **Directories:** No `wireframes/` or `imports/` under ux workspace; only `mockups/clickable/index.html` and `mockups/theme-clearwater.html`.
- **Cross-refs:** No broken `{colors.*}` / `{typography.*}` / `{components.*}` paths detected in DESIGN.md.
- **Mermaid:** None present.
