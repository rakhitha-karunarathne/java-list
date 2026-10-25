---
title: Reconcile forge → PRD — Java list mailer
status: complete
created: 2026-10-25
updated: 2026-10-25
inputs:
  - _bmad-output/forge-java-list-mailer/forge-java-list-mailer.md
  - _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/prd-java-list-mailer.md
  - _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/addendum.md
---

# Reconciliation: forge-java-list-mailer → PRD + addendum

Verdict: **Near-complete**. Forge V1 product decisions are largely reflected as FRs, Non-Goals, MVP scope, or correctly parked in the addendum. A few thin product/UX details and one role-scoping ambiguity remain.

## 1. Covered decisions

Forge hardened decisions that appear in the PRD (capability or explicit Non-Goal) without contradiction:

| Forge area | PRD home |
|------------|----------|
| New product, not phpList port; phpList-like paths | Vision §1, Non-Goals §5 |
| Roles: Admin / List manager / List sender; users ≠ members | Glossary, FR-1, FR-2 |
| Catalogs: sending paths, bounce mailboxes, footers, notice path, Parameters, Attention | FR-3–FR-8, Glossary |
| Lists + membership CRUD; one sending path; optional spam/footer/limits | FR-9 |
| Global enable/disable + `disable_reason`; no global block from unsubscribe | FR-10 |
| Re-enable manual or re-add (confirm); ignore if unsubscribed; no open auto-re-enable | FR-11, Non-Goals |
| Unsubscribe HTTPS + List-Unsubscribe-Post; per-list; multi-list campaign = all campaign lists | FR-12 |
| Unsubscribe visibility Admin vs LM; Member page | FR-13, FR-15 |
| Clear-unsubscribe consent via notice path; 14d expiry; audit | FR-14, FR-6 |
| Single audit store; retention 365d; Admin vs LM visibility; no raw secrets | FR-32 |
| Open pixel only; signed tokens; campaign open stats (unique/total) | FR-24 |
| Multi-list campaign, dedupe, first-list sending path | FR-16 |
| Content types; image-fetch Off/Best Effort/Mandatory; SSRF; payload + stored-image; TTL | FR-17, FR-19–FR-23 |
| Send queue + system throttle only (defaults) | FR-18, FR-19 |
| Bounce correlation order, VERP default off, uncorrelated never disable | FR-25, FR-27 |
| Bounce-process vs poll; hard/soft/holdback; TTLs; bounce UI error | FR-26 |
| Mail-to-send IMAP, mappings, From-issue hold/delete, auth/spam order, Drop log, auto-block | FR-28–FR-31 |
| HTTPS / Public base URL; Full or Full Strict only | FR-33, Non-Users, Non-Goals |
| Out of V1: click tracking, reply forward, spam V2/AI V3, plugins, public subscribe/pref/templates | §5, §6.2, Open Q #3 |
| Opens still open: origin cert supply; extra DSN headers | §8 + addendum |

## 2. Gaps (product capability in forge, weak or missing in PRD)

Product-facing forge items not fully stated as PRD requirements:

1. **UI campaign footer binding** — Forge: “UI campaign: list footer only.” PRD FR-5/FR-28 cover catalog footers and mail-to-send mapping override, but never state that UI-composed campaigns use only the list footer (no mapping substitute). Capability implied, not FR-explicit.

2. **Drop log deep-link from mailbox config** — Forge: mailbox config links to a pre-filtered Drop log. FR-31 requires per-mailbox + combined filterable views; the config→pre-filtered navigation is absent.

3. **Prefer mail-server edge rejection** — Forge guidance that edge rejection is preferred and the app also validates. FR-30 specifies app-side auth/spam only; edge preference is not a product/NFR statement.

None of these block MVP epic breakdown; they are polish / ops-preference gaps.

## 3. Mismatches

Content contradictions between forge and PRD/addendum: **none** on limits, defaults, bounce rules, roles, or Non-Goals.

**Ambiguity (treat as soft mismatch):**

1. **List sender × Attention** — Forge scopes Attention to Admin (all) and List managers (their lists); List sender is not named. PRD FR-8 opens with “Users see an Attention inbox” then only defines Admin/LM scopes. §4.1 description says LMs see Attention; List senders “send to assigned Lists” only. SM-4 also scopes Admin vs LM only. **Resolve:** state explicitly that List senders do not use Attention (or define a send-scoped subset).

**Dual-homing (not a content conflict):** Bounce-process algorithm steps and Parameter default table appear both as FR-26/FR-7 summary and in addendum. Aligned with PRD purpose (capability in body; how/defaults downstream), but FR-26 still embeds algorithm detail that the addendum also owns.

## 4. Items correctly parked in addendum

| Item | Why parking is correct |
|------|------------------------|
| Stack: Java, Spring Boot, schedules, React, MySQL, Docker | Implementation; PRD §12 keeps platform thin |
| Plugin architecture later; hook list deferred | Non-Goal + architecture input |
| Origin TLS Full vs Full Strict mechanics; Flexible rejected | Ops; FR-33 keeps product constraint |
| Bounce-process numbered algorithm + correlation signal list | Implementation spec; FR-25/26 keep product rules |
| Default Parameters table (all forge numbers) | Sizing locked in forge; not capability statements |
| Rejected alternatives list | Guardrails / discovery residue |
| Competitive landscape digest | Discovery; PRD Assumptions Index points at differentiation |
| Open: origin cert in container; extra DSN headers | Matches forge “Still open” and PRD §8 |

## 5. Suggested follow-ups (optional PRD edits)

- FR-8: replace “Users see” with Admin + List manager only (exclude List sender), or add List-sender rule.
- FR-5 or FR-17: one line — UI-composed campaigns attach the target list footer(s) only.
- FR-31: mailbox config opens Drop log pre-filtered to that mailbox.
- Optional NFR: prefer mail-server edge rejection; app validation remains mandatory.

## 6. Summary

Forge → PRD ingestion for Java list mailer is **tight**. Core operator paths, catalogs, bounce/mail-to-send security, consent unsubscribe, and HTTPS constraints are covered. Remaining issues are three small capability/UX gaps and one Attention role ambiguity — not structural product misses.
