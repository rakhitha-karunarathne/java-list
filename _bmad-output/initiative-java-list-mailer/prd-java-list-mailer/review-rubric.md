# PRD Quality Review — Java list mailer

## Overall verdict
This is a thesis-driven capability PRD that earns its production/self-hosted stakes: bounce, mail-to-send, consent, throttle, and HTTPS rules are concrete, trade-offs are mostly explicit, and the shape fits a multi-role operator tool without persona theater. What is at risk for green-light is not vision or coherence but a handful of launch-blocking gaps — Attention lifecycle, User/auth bootstrap, an unresolved Non-Goal open question, and uneven testable consequences on catalog/membership FRs — plus an Assumptions Index that does not roundtrip.

## Decision-readiness — adequate

Trade-offs are largely stated as decisions, not smoothed into “balance.” Silent drop over backscatter (FR-30/FR-31, §5), Flexible TLS rejected (UJ-1, FR-33, §5), soft bounce independence after init (FR-26, SM-5), Mandatory vs Best Effort image modes (FR-20), all-or-nothing From-issue campaigns (FR-29), and notice-path bypass limited to consent/admin (FR-6, §11) are honest and actionable. The addendum’s rejected list reinforces what was given up.

Gaps for a decision-maker: Open Question #3 still asks to “Confirm Non-Goal: no public subscribe…” while §5 and MVP Out of Scope already treat it as out — that is a rhetorical open item with an answer already written, not a clean deferral. There are almost no `[NOTE FOR PM]` callouts at real tensions (e.g. multi-list Campaign using “first selected List’s Sending path” in FR-16 without naming what happens when selected Lists disagree on path/From). Accessibility is parked as an assumption to “confirm in UX” (§10) rather than a product decision. For internal-launch rigor this is still shippable as a PRD draft, but not yet decision-closed.

### Findings
- **high** Open Q #3 vs Non-Goal still open (§8 #3, §5, §6.2) — Public subscribe / preference center / template library is listed as Non-Goal and MVP out-of-scope, yet Open Questions still ask to confirm it; forge ambiguity is carried forward instead of closed. *Fix:* Close #3 as confirmed Non-Goal (or promote to In Scope with FRs); remove the open question.
- **medium** Multi-list Sending-path rule undersells the trade-off (FR-16) — “first selected List’s Sending path” is a decision without naming the failure mode when Lists share recipients but disagree on path/From, or whether UI should warn. *Fix:* Add one sentence: warn on mixed paths, or block queue if paths differ — pick one.
- **medium** Missing `[NOTE FOR PM]` at deferred product tensions (§8, §10) — Origin cert supply and WCAG are ops/UX follow-ons; a11y especially affects launch checklist and is only an assumption. *Fix:* Tag WCAG and any remaining product forks with `[NOTE FOR PM]` or close them.

## Substance over theater — strong

Personas (Ava, Mei, Sam, Jordan) are few and load-bearing: they drive Attention scoping, consent via notice channel, and role consequences in FR-1/FR-8/FR-14. Vision is not swappable — “phpList-like operator paths + Attention + bounce/mail-to-send security on a non-PHP stack” is specific. NFRs in §10 are thin on adjectives alone, but the real thresholds live in FR-7/FR-19–FR-26 and the addendum defaults table — that is substance, not boilerplate. Competitive digest sits correctly in the addendum with a clear wedge. No innovation theater; “new product — not a port” is earned by Non-Goals.

### Findings
*(none — strong dimensions need no furniture findings.)*

## Strategic coherence — strong

Thesis is clear in §1: preserve known list-mail mental models while improving catalogs, bounce/mail-to-send defaults, and human-only Attention — without a V1 plugin marketplace. Feature sections follow that arc (catalogs → membership/consent → campaigns → bounce → mail-to-send → audit/HTTPS). Prioritization matches problem-solving MVP for an operator tool, not “easy first.” Success Metrics validate the thesis (UJ completion, zero false auto-disables, HTTPS consent audit) rather than vanity activity; counter-metrics SM-C1–C3 explicitly forbid dark-pattern opens, weakened auth, and auto-resolving uncorrelated bounces into disable. Addendum competitive framing matches the Assumptions Index differentiation claim.

### Findings
*(none.)*

## Done-ness clarity — adequate

Core operator paths are unusually done-able: FR-1 consequences, FR-12/FR-14 consent, FR-16–FR-23 queue/throttle/limits, FR-25–FR-26 bounce correlation and process, FR-28–FR-31 mail-to-send gates, FR-32–FR-33 audit/HTTPS all carry verifiable conditions. SM-1–SM-5 give acceptance hooks.

Unevenness shows on catalog and surface FRs that read as capability sentences without consequences: FR-4 (bounce mailboxes), FR-5 (footers), FR-7 (Parameters page), FR-9 (list lifecycle), FR-13 (unsubscribe visibility), FR-15 (member page), FR-17 (content types), FR-27 (VERP) lack the “Consequences (testable)” pattern used elsewhere. Soft “Prefer …” language (IMAP CREATE in FR-28/FR-29, edge rejection in FR-31) is mostly backed by failure branches for From-issue, but Attention (FR-8) lists item types and scoping without resolve/dismiss/expiry/auto-clear rules — engineers cannot know when an Attention item is “done.” FR-2’s “Admin (and List managers where granted)” leaves grant-of-grants circular for RBAC stories.

### Findings
- **high** Attention lifecycle unspecified (FR-8, SM-4) — Item types and Admin vs List manager scoping are defined; create/resolve/dismiss/auto-clear, ownership, and whether notice emails create Attention rows are not. Downstream UX and SM-4 cannot close without this. *Fix:* Add consequences: who can resolve which types; whether resolved items leave the inbox; any auto-expiry; link vs store rules stay as-is.
- **high** Several FRs lack testable consequences (FR-4, FR-5, FR-7, FR-9, FR-13, FR-15) — Catalog CRUD and membership surfaces will spawn ambiguous stories. *Fix:* Add 1–2 consequences each (e.g. delete Sending path blocked while Lists reference it; Footer change affects next Campaign only; Parameters change audited per FR-32).
- **medium** Grant authority circular (FR-2) — “List managers where granted” can grant manage/send without stating who may delegate grants or whether managers grant send-only. *Fix:* State: only Admin grants in V1, or managers may grant send (not manage) on Lists they manage — one rule.
- **medium** Soft “Prefer” without universal failure matrix (FR-28, FR-31) — From-issue CREATE-fail is specified; generic IMAP CREATE preference and “prefer mail-server edge rejection” are ops wishes more than FR done-ness. *Fix:* Keep product outcomes mandatory; move pure ops preference to addendum only.

## Scope honesty — adequate

Non-Goals (§5) and MVP Out of Scope (§6.2) do real work: not a port, no POP3, no click tracking, no reply desk, no plugins, no Flexible, no auto re-enable, no unilateral clear, uncorrelated never disables. Inline `[ASSUMPTION]` on inbox obscurity (FR-31) and deferred public subscribe (§5) are appropriate. Open Questions are mostly architecture/ops and correctly labeled non-blocking for FR wording — except #3 (product scope).

For production self-hosted launch, omissions that readers may silently assume include User lifecycle (create Admin, invite managers/senders, auth method, password/reset/SSO), session/security for operator login, and backup/restore — none appear as Non-Goals or Assumptions. That is the main honesty gap relative to stakes: roles are specified; getting a User into the system is not. i18n and competitive landscape appear only in the Assumptions Index (not inline), which weakens the “user didn’t confirm” contract.

### Findings
- **high** User/auth bootstrap absent (§4.1, §12) — FR-1 assumes “signs in” / login roles; there is no FR or Non-Goal for account provisioning, credentials, SSO, or first-Admin bootstrap. Production operator tool cannot treat this as pure architecture silence. *Fix:* Add FR for Admin-managed Users (create/disable, role assignment) and auth method for V1, or an explicit `[NON-GOAL for MVP]` plus assumption naming the external IdP.
- **medium** Assumptions only in index (competitive, i18n, stakes) (§9) — These never appear inline, so readers of Features alone miss them; stakes assumption belongs in Document Purpose. *Fix:* Inline the tags at Vision/Platform/Document Purpose, or drop index-only entries.
- **low** Open-items density acceptable if #3 closes (§8) — Remaining opens are ops/architecture; density matches stakes once product Non-Goals are confirmed.

## Downstream usability — adequate

This PRD is chain-top (feeds UX, architecture, epics). Glossary is strong and mostly stable (User vs Member, Sending path, Attention, Drop log vs Issues, Notice sending path). FR-1–FR-33, UJ-1–UJ-5, SM-1–SM-5 + SM-C1–C3 are contiguous and cross-references resolve (e.g. FR-28 → FR-16 dedupe, FR-20 ↔ FR-23, SM-2 → FR-25). UJs each name a protagonist with climax/edge. Addendum correctly isolates stack, bounce algorithm detail, and defaults so Sofia can cite FR ids without ingesting Spring/MySQL as product law.

Weak spots: Assumptions Index roundtrip fails (see Mechanical notes); §10 NFR bullets partially duplicate FR thresholds without ids; “Issues” vs Attention vs Drop log is glossary-clear but easy to confuse in stories without always citing terms.

### Findings
- **medium** Assumptions Index roundtrip broken (§9 vs inline tags, §10) — Index lists competitive/i18n/stakes without inline anchors; WCAG assumption in §10 is not indexed. *Fix:* Make every index entry appear inline once; add WCAG to §9; remove paraphrases that are not tags.
- **low** NFR section not FR-linked (§10) — Security/privacy bullets restate FR-20/FR-24/FR-31/FR-32/FR-33 without ids, inviting duplicate tickets. *Fix:* One-line cross-refs to governing FRs.

## Shape fit — strong

Calibrated correctly for an internal multi-role self-hosted operator tool: light UJs with named operators, heavy capability FRs, operational Success Metrics, stack and algorithm detail in addendum, not PRD body. Not over-formalized into consumer journey theater; not under-formalized into a backlog dump. Brownfield-adjacent mental model (phpList-like) is handled as “new product — not a port” with Non-Goals — right call. Stakes line in Assumptions matches the requested production internal-launch rigor.

### Findings
*(none.)*

## Mechanical notes

- **Glossary:** User / Member distinction held consistently; Sending path, Bounce mailbox, Notice sending path, Attention dashboard, Drop log, Issues, Audit log, Image-fetch mode used with stable casing. Minor: `disable_reason` is a field name amid prose glossary — fine if architecture owns the column.
- **ID continuity:** FR-1–FR-33 contiguous; UJ-1–UJ-5; SM-1–SM-5 and SM-C1–SM-C3. Cross-refs checked (FR-28, FR-20, FR-25/26, FR-12/14/32/33) resolve. No duplicate ids found.
- **Assumptions Index roundtrip:** Fail. Inline: FR-31 inbox obscurity; §5 public subscribe; §10 WCAG (unindexed). Index-only: competitive landscape, i18n, stakes. Indexed paraphrases for FR-31/§5 omit the exact tag form.
- **UJ protagonists:** All five UJs name Ava / Mei / Sam / Jordan with climax; no floating journeys.
- **Required sections for stakes:** Vision, users/UJs, Glossary, Features/FRs, Non-Goals, MVP scope, Success Metrics (+ counters), Open Questions, Assumptions, NFRs, Constraints, Platform — present. Auth/User provisioning is the notable product hole relative to production rigor.
- **Addendum alignment:** Defaults table matches FR-7/FR-19/FR-21–FR-23/FR-26 numbers; bounce algorithm matches FR-26; rejected list matches Non-Goals. No contradiction found on locked product numbers.
