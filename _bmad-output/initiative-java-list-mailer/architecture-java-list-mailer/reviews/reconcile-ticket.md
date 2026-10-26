# Reconcile: initiative ticket ↔ architecture spine

**Date:** 2026-10-25  
**Ticket:** `initiative-java-list-mailer.md`  
**Spine:** `architecture-java-list-mailer.md`  
**Method:** Full read of both; map R1–R34 and C1–C8 to ADs, capability table, conventions, Deferred.

---

## Verdict

**Partial reconcile — build substrate is strong; product law is thin.** The spine credibly binds stack, slice inventory, public URL shapes, auth split, Compose topology, and queue/pixel mechanics. It **claims** `binds: [R1-R34, C1-C8]` but **defers C5–C8 to the ticket** in the capability map, and most **behavioral, default, and IA requirements** appear only as slice names in the map—not as AD rules or Parameters keys. Implementers can ship AD-compliant code that **violates the ticket** on bounce/MTS policy, role-scoped Attention, retention, throttles, and sidebar IA unless stories re-import the ticket.

---

## (a) Requirements / constraints missing or weakly covered

Legend: **Missing** = no spine rule/map detail an implementer must follow; **Weak** = slice named or one-line map only; ticket detail absent from ADs/Parameters/conventions.

| ID | Coverage | Gap |
| --- | --- | --- |
| **R2** | Weak | Per-List grants on User detail; **grant removal effective immediately** — not in `users` ADs or AD-6. |
| **R4** | Weak | Catalog + poll on detail; **default poll interval 15 min** not a declared Parameters key (AD-15 lists R7/R25/R28 only). |
| **R5** | Weak | Used-by/delete in R9 pattern; **UI campaigns use List footer only** — not stated (forge/UX risk if compose owns footer choice). |
| **R6** | Weak | `notice-mail` slice named; **Notice path bypasses bounce-disable; campaign queue never does** — no AD/rule. |
| **R8** | Weak | `attention` slice only; **Admin all + highlighted ops errors; LM list/member scope; List senders no inbox** — absent. |
| **R10** | Weak | `lists` slice; **spam threshold, payload/image/TTL overrides, one sending path, no public/private flag** — overrides not in spine (flag correctly Deferred). |
| **R11** | Weak | Bulk-paste add rules (skip invalid/dupes/on-list/unsub, counts); **List senders cannot** — absent. |
| **R12** | Weak | **Global Enabled/Disabled, `disable_reason`, bounce disable ≠ unsubscribe, queue skip** — absent from `members`/bounce collaboration. |
| **R13** | Weak | Re-enable manual/re-add with confirm (**clears streaks** unless unsub on list); **opens never auto re-enable** — absent. |
| **R15** | Weak | Consent URL/token in AD-17/18; **14-day expiry default, no unilateral clear, audited** — expiry not Parameters; audit link generic. |
| **R16** | Weak | Searchable **Members index in sidebar** (Admin all / LM scoped) — only `members` slice; **C8 overlap**. |
| **R18** | Weak | Multi-list one queue row; **first selected List’s sending path** — not in campaigns/send-queue ADs. |
| **R20** | Weak | Compose/test-send split in AD-16; **test to pasted addresses, first list path, ignores bounce-disable, not sent-stats, failures → ops/Admin Attention** — absent. |
| **R21** | Weak | AD-13 sequential heavy path; **system-wide throttle only; lists only enqueue** — throttle not specified. |
| **R22** | Weak / tension | Image modes + blobs; **SSRF guards** not in kernel/port ADs; **CDN TTL after campaign finished** vs AD-23 “CDN caching … not a V1 goal” (see contradictions). |
| **R23** | Weak | Blob limits in AD-20/23; **per-List ≤ ceiling; multi-list strictest (TTL longest wins)** — aggregation rule missing. |
| **R24** | Weak | AD-11 pixel async; **headline unique opens, per-member first/count, signed tokens** — reporting model not AD-bound beyond tracking slice. |
| **R25** | Weak | AD-15 binds R25; **ops log retention default 90 days** as Parameters field — default/key not declared (ticket Decision 2026-10-25). |
| **R26** | Weak | Bounce slice + AD-12 poll; **VERP→token→Message-ID→subject+email ladder; uncorrelated logged + Attention** — algorithm not in spine (addendum lives on ticket). |
| **R27** | Weak | **Daily bounce-process; separate hard/soft walks + holdback; either limit disables; soft independent after init** — absent. |
| **R28** | Weak | AD-15 binds R28; **VERP toggle default off** — not explicit Parameters default. |
| **R29–R32** | Weak | `send-to-mail` + poll AD-12; **IMAP-only, authorized senders, substitute From domain, footer override, From-domain Issues all-or-nothing, spam/DMARC matrix, drop log + auto-block N/day, no stranger courtesy reject** — policy matrix not in ADs. |
| **R33** | Weak | `audit` slice + C6 logging row; **append-only, LM member-scoped, retention default 365 days** — retention/default missing (ops 90d in ticket but audit 365d not in AD-15). |
| **R34** | Weak | AD-17 public base Parameter, HTTPS paths; **origin TLS Full or Full Strict only** — not in AD-7/deploy rule (C4). |
| **C4** | Weak | Mapped under R34; **Flexible TLS unsupported** — no AD for deploy/Tunnel mode. |
| **C7** | **Missing** | **Uncorrelated bounces never auto-disable / never streak** — not in bounce ADs despite R26–R28 map (critical product safety). |
| **C8** | **Missing** | Capability map: **“C5–C8 … Deferred / ticket”** — sidebar IA (detail pages from indexes/deep links only; Compose/Ops Logs index exceptions) not architecture-bound despite header `binds`. |

### Adequately covered (for reconcile scope)

R1 (users + JWT AD-6/19), R3/R9 (catalog slices + delete guards AD-3), R7 (AD-15), R14 (AD-17 unsub one URL + POST), R17 (members — timeline as audit filter is ticket-level), R19 (blobs AD-20), R30/R31 partially named only under R29–32 weak bucket above — still weak as ADs; R34 public paths strong. C1–C3, C5 (boundary/deferred non-goals), C6 (AD-15 + conventions) — OK at spine altitude.

---

## (b) Spine AD / document contradictions vs ticket

| Item | Spine says | Ticket says | Notes |
| --- | --- | --- | --- |
| **binds vs capability map** | Front matter: `binds: [R1-R34, C1-C8]` | C8 (and C5–C7 product bounds) are constraints | Map row **“C5–C8 product bounds \| Deferred / ticket”** contradicts full bind claim; C7/C8 should not be deferred if spine is source of truth. |
| **AD-23 vs R22** | “CDN caching of `/dl` **not a V1 goal**” | “Rewritten images **CDN TTL after campaign finished**” | Not necessarily identical (TTL headers vs edge cache product), but spine reads as **opting out of CDN behavior** the ticket expects post-finish; needs explicit reconcile (Cache-Control/TTL at origin vs “no CDN goal”). |
| **AD-23 vs R22 security posture** | `GET /dl` **public, no read ACL** | Image-fetch modes + **SSRF guards** on ingest | Not a direct contradiction if guards are on fetch; **public read of campaign GUID bucket** is a stronger exposure model than ticket text implies — flag for security story, not ticket denial. |

No AD **directly negates** R6 Notice bypass, C7 uncorrelated rule, or List-Unsubscribe-Post — those are **omissions**, not opposing rules.

---

## (c) Quiet product tones dropped (present on ticket, absent/spine-thin)

These are outcome/behavior phrases that shape operator trust and phpList-like parity; the spine does not carry them in AD prose or conventions:

1. **“phpList-like operator behaviors … stronger defaults (HTTPS, Attention, consent clear-unsub)”** (Description/Outcome) — spine is stack/slice shaped, not parity/trust shaped.
2. **Bounce disable ≠ unsubscribe** (R12) — legal/operator mental model; not in members/bounce cross-slice rules.
3. **Consent clear-unsub: no unilateral clear; audited** (R15, Outcome #4) — URL crypto yes; **process tone** (member agency) not in `consent` AD.
4. **Uncorrelated bounces: never punitive auto-disable; logged + Attention** (R26, C7, Done when #3) — safety tone missing from bounce ADs (C7 missing entirely).
5. **Mail-to-send: strangers silent / logged; no stranger courtesy reject** (Done when #2, R32) — anti-abuse/reputation tone absent from `send-to-mail`.
6. **From-domain Issues: all-or-nothing; hold or delete-and-notify** (R30) — operator clarity tone absent.
7. **Soft bounces independent after init; either hard/soft limit disables** (R27) — algorithm civility not in spine.
8. **Admin Attention: highlighted ops errors** (R8, R25) — ops visibility tone vs generic `attention` slice.
9. **Test send and ops errors funnel to Admin Attention** (R20, R25) — cross-slice UX contract not stated.
10. **Tracer path** (Boundaries): catalogs → lists/members → compose/MTS → queue → bounce/ops → Attention/audit — useful onboarding narrative; spine mermaid is package topology only.

---

## Recommended spine patches (minimal)

1. Add **AD or conventions block “Product parameters (defaults)”**: ops log retention 90d, audit retention 365d, consent token 14d, bounce poll 15m, VERP off — per ticket Decisions.
2. Add **bounce AD fragment** binding **C7** + R26–R27 correlation/holdback (or explicit pointer: “bounce slice implements addendum algorithm verbatim”).
3. Add **send-to-mail AD fragment** for R29–R32 outcome matrix + stranger silence + no courtesy reject.
4. Add **notice-mail rule** for R6 bypass vs campaign queue.
5. Add **Attention visibility AD** for R8 (+ ops error highlight for Admin).
6. Resolve **C8** in spine (React routing/IA convention) or narrow header `binds` to exclude C8 with explicit rationale.
7. Reconcile **AD-23 CDN sentence** with R22 TTL intent (one line: origin Cache-Control/TTL after campaign complete; edge CDN optional).

---

## Summary counts

| Category | Count |
| --- | --- |
| R1–R34 weak/missing at spine | 22 requirement IDs with material gaps (R2, R4–R6, R8, R10–R13, R15–R18, R20–R29–32 block split, R25–R28, R33–R34 partial) |
| C1–C8 weak/missing | C4 weak, **C7 missing**, **C8 missing** (plus C5–C8 defer row vs binds) |
| AD/ticket contradictions | 2–3 (binds defer, AD-23/R22 CDN, public /dl vs SSRF nuance) |
| Quiet tones dropped | 10 listed |
