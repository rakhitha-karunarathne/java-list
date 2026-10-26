# Architecture Spine Rubric Review — Java list mailer

**Artifact:** `architecture-java-list-mailer.md`  
**Altitude:** initiative (build substrate)  
**Rubric:** good-spine checklist (Reviewer Gate) + user criteria (divergence, AD enforceability, Deferred safety, stack pins, operational envelope, silent dimensions)  
**Mechanical lint:** `lint_spine.py` — 0 findings (placeholders, AD fields, stack versions)

## Overall verdict

**Conditional pass (draft).** The spine nails the highest-risk build-time forks: feature-slice/kernel boundaries, single-app Compose topology, Flyway vs ddl-auto, operator JSON API + pagination/errors, public URL shapes, member vs operator auth, scheduler overlap semantics, and V1 scale assumptions (single instance, in-process events, DB blobs). It is credible as the consistency contract for parallel epics. It is not yet decision-closed for initiative altitude because the **operational/environmental envelope** is only partially owned, **one Deferred row can still fork token implementations**, and several **initiative constraints (C4, C8) and R1 bootstrap** are mapped but not ruled.

---

## 1. Fixes real divergence points (level below)

**Adequate — strong on code/deploy seams, gaps on ops and cross-cutting product architecture.**

### What is well fixed

| Divergence class | Spine anchor | Assessment |
| --- | --- | --- |
| Package coupling | AD-1, AD-2, AD-3, AD-16, AD-21 | Clear vertical slices + kernel ports/events; inventory closes “who owns campaigns vs compose vs notice-mail.” |
| Operator vs public surface | AD-4, AD-6, AD-17, AD-18 | Prevents BFF/SSR drift, JWT on pixels, ad-hoc URL shapes. |
| Persistence truth | AD-8, AD-20, AD-23, AD-24 | Flyway + validate; blob identity; GUID public ids. |
| Runtime topology | AD-7, AD-9, AD-10 | Compose roles, React-in-jar, single replica — aligns schedulers and locks story. |
| Heavy paths | AD-11, AD-12, AD-13 | Pixel async, sequential inbox poll, send-queue vs concurrent job types. |
| Shared settings | AD-15 | Single Parameters port — blocks per-slice settings tables. |
| API consistency | AD-14 + Consistency Conventions table | Blocks per-slice pagination/error JSON. |
| Future split | AD-22 | Marks send-queue/tracking without forcing premature modules. |

### Missed or under-fixed divergence points

- **Operator RBAC enforcement layer** — R1–R2, R8, R16 scope rules live in PRD/initiative; spine assigns `users` slice but has no AD for “JWT + grant checks on every `/api` route” vs public paths. Two feature slices could interpret “scoped” endpoints differently.
- **Notice path vs campaign queue** — AD-16 names `notice-mail` + Notice sending path; AD-2 keeps SMTP at kernel port. Risk: campaign/send-queue vs notice-mail both calling smtp-adapter with different disable/unsub rules unless a port contract names bypass semantics (product: R6, R15).
- **Image fetch / SSRF (R22)** — Product mandates fetch mode + SSRF guards; spine silent. Compose/campaigns could implement outbound fetch differently.
- **Append-only audit + retention jobs (R33, R25)** — Conventions mention no secrets in Audit; no AD for single audit store, purge schedules, or ops-log retention job ownership (`ops-log` vs `audit` vs kernel scheduler).
- **Navigation IA (C8)** — Capability map lists C5–C8 as “Deferred / ticket”; C8 is an explicit initiative constraint. UX and frontend epics can diverge on sidebar vs index-first navigation without a spine hook.
- **HTTPS / origin TLS policy (C4, R34)** — Referenced only via map to C4; no AD Rule. Edge-only vs app HSTS/redirect, and “Flexible unsupported,” are not contractually bound for deploy/docs epics.

### Findings

- **high** C8 navigation constraint unbound (Capability map, C8) — Detail pages not as primary sidebar destinations is initiative law; spine does not prevent a React epic from promoting Member/Campaign detail to sidebar nav. *Fix:* One AD or Consistency Convention row: primary sidebar destinations fixed; detail = index/deep-link only.
- **high** HTTPS / Cloudflare origin mode not AD-owned (R34, C4, diagram) — Public HTTPS and Parameters public base URL are covered; **Flexible TLS rejected** and origin **Full / Full Strict** are forge/initiative constraints without a Prevents/Rule pair. Deploy runbooks and app config can diverge. *Fix:* Short AD or bind C4 explicitly under AD-7 deploy envelope.
- **medium** Notice vs campaign send semantics not port-bound (R6, AD-16, AD-2) — Bypass bounce-disable and unsub rules for notice-mail are product-critical; architecture only names slices. *Fix:* Kernel or notice-mail port contract: campaign queue honors disable/unsub; notice path exceptions enumerated.
- **medium** SSRF / image-fetch architecture silent (R22) — Outbound fetch from compose/campaigns is a shared security boundary. *Fix:* Defer with “Ticket Boundaries” only if paired with “all fetch via kernel-approved client + one config source”; otherwise AD stub pointing to Parameters keys and shared fetch helper in kernel or blob-storage edge.
- **medium** RBAC enforcement not stated as cross-cutting rule (R1–R2, R8) — JWT presence (AD-6) ≠ grant checks. *Fix:* AD or kernel rule: `/api/**` authorized by role + list grants; public paths whitelist only AD-17 set.

---

## 2. Every AD Rule enforceable and prevents stated divergence

**Adequate — most ADs are build- or review-enforceable; several rely on convention until tooling exists; two Rules soften prevention.**

### Per-AD notes (summary)

| AD | Prevents as stated? | Enforceability |
| --- | --- | --- |
| AD-1–3 | Yes, if imports policed | **Weak until ArchUnit** (Deferred) — Maven compiles feature→feature imports today. |
| AD-4 | Yes | SPA + `/api` in one Boot app — structure + Spring MVC layout. |
| AD-5 | Yes | No broker dependency; code search. |
| AD-6 | Yes | Spring Security matcher split JWT vs public. |
| AD-7 | Yes | `docker-compose.yml` bind/profile review. |
| AD-8 | Yes | `application.properties` + Flyway dir. |
| AD-9 | Yes | Maven frontend plugin packaging. |
| AD-10 | Yes | Ops/doc + replica count; not automatic in code. |
| AD-11 | Yes | Controller + async handoff pattern review. |
| AD-12 | Yes | Poller implementation review. |
| AD-13 | Yes | Per-job executor/skip-if-running — testable. |
| AD-14 | **Partial** | “or equivalent” allows shape drift; needs shared response wrapper or test contract. |
| AD-15 | Yes | Port + single parameters slice table. |
| AD-16 | Yes | Package placement + code review. |
| AD-17–18 | **Partial** | Paths enforceable; token *format* deferred (see §3). |
| AD-19 | Yes | Frontend sessionStorage only. |
| AD-20–24 | Yes | Schema + API review. |

### Findings

- **high** AD-3 prevention depends on Deferred ArchUnit (AD-3, Deferred table) — Stated divergence is feature→feature imports; Rule is clear but **not mechanically enforceable** for V1 start. Acceptable only if opening epic mandates ArchUnit before second slice merges, or kernel publishes forbidden import list in CI grep. *Fix:* Tighten Deferred row: “ArchUnit before epic N merges” or interim `import-rules` script in CI.
- **medium** AD-14 “or equivalent” weakens pagination contract (AD-14) — Prevents “per-slice shapes” but allows `data`/`meta` vs `items`/`total` fork. *Fix:* Drop “or equivalent”; reference one canonical JSON shape in Consistency Conventions only.
- **medium** AD-18 allows two crypto families (AD-18) — “AES-GCM **or** random opaque server-side ids” — each slice could pick one; Prevents “readable base64 JSON + HMAC” but not inconsistency across pixel/unsub/consent. Overlaps Deferred codec row. *Fix:* Rule: **one** kernel opaque-token helper API used by all three surfaces.

---

## 3. Deferred items — divergence safety

**Mostly safe — one high-risk deferral, one enforcement deferral.**

| Deferred item | Can two units diverge wrongly? |
| --- | --- |
| ShedLock / multi-instance | No — AD-10 forbids locks and multi-replica schedule design in V1. |
| V2 queue/pixel edge | No — AD-11/22 bound V1 behavior; deferral is explicit upgrade path. |
| Object store blobs | No — AD-20 locks DB blobs. |
| Maven multi-module | No — AD-21 single module until split. |
| JWT refresh / multi-tab | No — frontend-only; AD-19 fixed for V1. |
| External broker | No — AD-5 in-process only. |
| Public subscribe / click / POP3 / plugins | No — Ticket Boundaries; out of V1 scope. |
| List public/private | No — aligned with initiative Boundary. |
| **Exact opaque-token codec** | **Yes** — tracking, unsubscribe, consent epics could ship incompatible token formats despite AD-17 paths and AD-18 intent. |
| **ArchUnit** | **Yes (temporary)** — slice coupling until rules land. |

### Findings

- **high** Deferred token codec vs AD-2 “opaque-token helper” (Deferred, AD-2, AD-18) — Kernel owns helper; implementation pick deferred. Without a V1 **single API + algorithm choice**, three slices can diverge. *Fix:* Move minimum codec decision into AD-18 (e.g. “kernel `MemberLinkToken` encrypt-then-MAC or UUID lookup table — pick one in AD-18”) and defer only rotation/key-id details.
- **low** ArchUnit timing (Deferred) — Document owner epic for dependency rules; pair with AD-3 finding above.

---

## 4. Named tech verified-current

**Pass (assumed web-pinned per review brief).**

Stack table pins Java 21, Spring Boot 4.1.2, React 19.3.1, MariaDB 12.3.3, Flyway via Boot 4.1.x — satisfies lint `version_pin`. Docker Compose components named without image digest pins (appropriate at initiative altitude).

### Findings

*(none under assumed web-pinned policy.)*

---

## 5. Operational / environmental envelope

**Inadequate for initiative altitude — deploy topology present; operations partially silent.**

**Present:** AD-7 (app + MariaDB + phpMyAdmin, private PMA, no embedded DB), diagram (Cloudflare → app), secrets in env/secret file (conventions), single instance (AD-10), public edge path list.

**Silent or thin:**

- **Environments** — dev/staging/prod parity, config promotion: not decided, deferred, or open.
- **Backup / restore** — MariaDB volume strategy, RPO/RTO: silent (high for self-hosted product Done when #5).
- **Health / readiness** — no AD for `/actuator/health` or compose `depends_on` semantics.
- **Observability** — ops-log slice named; no convention for structured logging, log shipping, or metrics.
- **TLS policy** — see §1 C4 gap.
- **First Admin bootstrap (R1)** — AD-2 mentions kernel “bootstrap”; no Rule for install-time seed vs migration — deploy and `users` epic can diverge.
- **Scheduled retention** — audit 365d / ops 90d are Parameters product defaults; no spine owner for purge jobs.

### Findings

- **high** Operational envelope incomplete (initiative Done when #5, AD-7) — Self-hosted “single Docker deploy” is stated; **backup, health checks, and environment strategy** are whole dimensions silent at initiative altitude. *Fix:* Add “Operational envelope” subsection: decide or explicitly defer each with “won’t fork builders” rationale (e.g. backup = operator responsibility + documented `mysqldump` volume path; health = Spring Actuator liveness required in app image).
- **medium** R1 install bootstrap not ruled (R1, AD-2) — “Install yields initial Admin” has no Flyway seed vs first-run wizard vs env bootstrap AD. *Fix:* One Rule under `users` or kernel bootstrap: e.g. Flyway seed single Admin when empty + forced password change on first login (product detail can follow).

---

## 6. Silent dimensions at initiative altitude

**Several dimensions neither decided nor listed as Deferred/open.**

| Dimension | Status in spine |
| --- | --- |
| Paradigm / slices | Decided (AD-1, AD-16) |
| Stack | Decided (Stack table) |
| Deploy topology | Decided (AD-7) |
| Data migration | Decided (AD-8) |
| Public/member edge | Decided (AD-17–18) |
| Auth mechanisms | Partial (AD-6, AD-19; grants silent) |
| HTTPS / TLS product policy | **Silent as AD** (C4 pointer only) |
| Navigation IA | **Silent** (C8) |
| Testing / CI | Silent (acceptable if out of altitude — not listed Deferred) |
| Backup / DR | **Silent** |
| Plugin seams | Open in initiative Notes; spine Boundaries via Deferred ticket items — OK |

Initiative binds R1–R34, C1–C8. Capability map covers all R rows; C5–C8 collapsed to “Deferred / ticket” which **under-specifies C4 and C8** as architecture-relevant constraints.

### Findings

- **high** Whole dimensions silent without deferral record (§5, C4, C8) — Rubric treats silent ops/TLS/IA as findings at initiative altitude. *Fix:* For each: AD, Consistency Convention, or explicit Deferred row with “why no fork.”

---

## 7. Ticket / parent coverage (initiative R1–R34, C1–C8)

**Requirements map:** All R1–R34 rows present in Capability → Architecture Map with governing ADs — good traceability.

**Constraints:**

- C1–C3 — covered (AD-7, AD-8, stack, AD-9, AD-13).
- C4 — pointer only; gap above.
- C5 — non-goal; N/A for spine.
- C6 — conventions (logging/Audit); adequate.
- C7 — product logic in bounce slice; no architecture fork expected.
- C8 — under-bound.

**Forge ratification:** Spine tightens token story vs forge “HMAC or equivalent” (AD-18 rejects readable HMAC payloads) — intentional strengthening, not contradiction. MariaDB, Compose, HTTPS intent align with forge.

**Brownfield:** Greenfield product; “ratifies codebase” N/A.

---

## 8. Recommended fix priority (for Finalize)

1. **Close token codec deferral** — single kernel helper + algorithm in AD-18; shrink Deferred row to rotation/key-id only.
2. **Operational envelope subsection** — backup stance, health endpoint, env strategy (decide or defer explicitly).
3. **AD or convention for C4 HTTPS** and **C8 navigation IA**.
4. **RBAC + notice-path port contracts** — short ADs or kernel port bullets.
5. **ArchUnit / CI import gate** — date or epic gate in Deferred.
6. **Tighten AD-14** — remove “or equivalent.”

---

## Mechanical cross-check

- `lint_spine.py`: **ok**, 0 findings.
- AD-1–AD-24: monotonic, all have Binds / Prevents / Rule.
- Stack: all rows versioned.

---

## Summary table

| Rubric criterion | Grade |
| --- | --- |
| Real divergence points fixed | Adequate (ops, RBAC, SSRF, C8 gaps) |
| AD Rules enforceable → Prevents | Adequate (AD-3, AD-14, AD-18 soft spots) |
| Deferred divergence-safe | Mostly (token codec **no**) |
| Stack verified-current | Pass (assumed pinned) |
| Operational envelope | Inadequate |
| No silent dimensions | Fail partial (ops, TLS AD, C8, bootstrap) |

**Gate recommendation:** Proceed to epic slicing with spine as draft contract; treat §8 items 1–3 as **pre-epic or opening-epic** closure for high-risk parallel work (frontend nav, public tokens, deploy/ops docs).
