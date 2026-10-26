# Adversarial architecture review — Java list mailer spine

**Artifact:** `architecture-java-list-mailer.md` (initiative spine, AD-1–AD-24)  
**Reviewer stance:** Two independent epic teams each implement their slice literally obeying every AD; integration still fails.  
**Date:** 2026-10-25  

---

## Verdict

**NOT PARALLEL-SAFE.** Dependency direction (kernel-only imports, utility adaptors, single module) is clear enough to prevent classpath spaghetti. What is missing is a **kernel-published contract layer** for ports, events, and reference-registration that fixes **one writer per shared fact** and **one canonical shape per cross-slice identifier**. Until those contracts exist, AD-compliant implementations can disagree on entity ownership boundaries, DTO shapes, and mutation order without violating any written AD.

---

## Method

For each hole below:

1. Name two (sometimes three) **feature packages / epics** one level under the spine.
2. Show how **each side satisfies every relevant AD** (no feature→feature imports, correct slice inventory, ports/events, parameters, public URLs, etc.).
3. Show the **integration fracture**: clashing data shapes, dual owners, or incompatible mutation paths.
4. State the **hole to close** (minimum spine/kernel addition — not a rewrite).

AD text is treated as normative; PRD/initiative requirements are cited only where the spine is silent and teams would guess differently.

---

## Hole pairs (integration fractures)

### H1 — `campaigns` × `compose`: two writers, one Campaign aggregate

| AD compliance | Team A — **campaigns** | Team B — **compose** |
| --- | --- | --- |
| AD-1, AD-3, AD-16 | Owns campaign entity, lifecycle, list targeting, queue handoff; exposes `/api/campaigns/**`; mutates via campaigns persistence. | “Editor/test-send only”; no feature imports; uses kernel **CampaignCommandPort** (campaigns implements) for saves. |
| AD-4 | REST under `/api/campaigns/...` | REST under `/api/compose/...` or `/api/campaigns/{id}/compose/...` — allowed: slices own routes. |
| AD-20, AD-23 | On delete, calls **BlobStoragePort** cascade; bucket = campaign GUID. | Uploads images via **BlobStoragePort** during editing; bucket = “current campaign GUID”. |

**Fracture (dual owner + shape clash):**

- **Campaign body shape:** campaigns persists `subject`, `html_body`, `text_body`, `list_ids`, `status` on `campaign` row. compose persists parallel `compose_draft` row (same GUID) with `html`, `text`, `preview_meta`, `dirty` flag — both AD-valid (“layers inside a slice”; compose not importing campaigns).
- **Mutation path:** Operator saves in compose UI → compose updates draft table and optionally calls CampaignCommandPort with partial DTO `{ html, text }`. Operator clicks “Queue” on campaigns UI → campaigns reads **only** its table, queues stale content. Or compose calls port with full body while campaigns API also accepts PATCH on same fields — last-write-wins without shared transaction boundary (not forbidden by AD-3).
- **Blob bucket timing:** compose allows upload **before** campaigns allocates GUID (client provisional UUID). campaigns on “create campaign” assigns **server** GUID and never rewrites blob buckets or HTML `/dl/{guid}/` URLs. AD-23 requires bucket = campaign GUID but does not mandate **who re-homes** blobs on GUID assignment.

**Hole to close:** Spine must pin **single persistence owner** for campaign content (campaigns table only; compose is stateless editor + test-send orchestration **or** compose-owned draft with explicit **promote-on-queue** port): one `SaveCampaignContent` command, one row set, explicit rule for blob bucket when GUID changes.

---

### H2 — `send-queue` × `bounce`: queue row vs correlation ownership

| AD compliance | Team A — **send-queue** | Team B — **bounce** |
| --- | --- | --- |
| AD-16, AD-22 | Owns send queue persistence and processor; uses **smtp-adapter** via kernel port. | Owns bounce ingest, correlation, bounce-process; uses **mailbox-query** via kernel port. |
| AD-3 | bounce queries queue via **SendQueueQueryPort**; no import of `sendqueue` package. | Same. |
| AD-12, AD-13 | Sequential send path; stores minimal row at enqueue. | Pollers separate; bounce-process scheduled job. |

**Fracture (shared-data shape + mutation path):**

- PRD/addendum: correlation signals stored **on queue row at send** (VERP, `X-JavaList-Token`, Message-ID, subject+email).
- send-queue team: queue row at enqueue = `{ id, campaign_id, member_id, email, status }`; at SMTP send, smtp-adapter adds headers but **does not write back** to queue row (adapter is stateless utility per AD-16).
- bounce team: implements correlation by writing `bounce_correlation` table keyed by `message_id` + `email`, populated from **BounceReceivedEvent** payload that assumes fields exist on a **SendCompletedEvent** — never reads send-queue table (port returns DTO without correlation columns).
- Result: VERP toggle (AD-15 / FR-27) on in send-queue path; bounce expects Return-Path on row for match order — compliant with AD-2 (no bounce rules in kernel) but **incompatible algorithms** at runtime.

**Hole to close:** Kernel **SendQueueRow** contract: mandatory mutable fields written **once at send completion** by send-queue (owning writer); bounce port read-only; document match order against that shape. Optionally one event `OutboundSendRecorded` with canonical payload.

---

### H3 — `unsubscribe` × `lists` × `campaigns`: which lists does unsub touch?

| AD compliance | **unsubscribe** | **lists** | **campaigns** |
| --- | --- | --- | --- |
| AD-16, AD-17 | Owns `/unsubscribe/<token>` public surface; AD-18 tokens. | Owns list membership rows. | Owns campaign ↔ list association for sends. |
| AD-3 | Calls **ListMembershipPort.remove(...)** implemented by lists. | Implements port; publishes `MemberRemovedFromListEvent`. | Exposes **CampaignQueryPort** for campaign metadata. |

**Fracture (conflicting mutation semantics):**

- R14 / AD-17: one URL per send; token binds member + campaign; removes from **all lists on that campaign**.
- campaigns team A: stores `campaign_list_snapshot` at queue time (immutable for unsub). lists removes membership for list IDs in snapshot via port args from unsubscribe.
- campaigns team B: stores only current `campaign_list` editable until send completes; unsub port loads **live** list IDs from campaigns at click time.
- unsubscribe team: token decrypts to `{ memberId, campaignId }` only (AD-18 “conveys nothing” — no list IDs in token). Must call campaigns for list set — **two AD-valid campaign APIs** → member removed from wrong list set (over-unsub vs under-unsub).

**Hole to close:** Requirement on spine: **list set frozen at enqueue** (or explicit snapshot table owned by campaigns); `UnsubscribePort` input = snapshot id or list id set from campaigns-only query; lists must not infer campaign lists without campaigns port.

---

### H4 — `sending-paths` / `footers` × `send-to-mail`: R9 Used-by graph incomplete

| AD compliance | **sending-paths** / **footers** | **send-to-mail** |
| --- | --- | --- |
| AD-16, R9 | Catalog slice owns delete guard and Used-by UI; queries referencers via kernel ports. | Owns inbox mappings (substitute From, footer override per PRD FR-28). |
| AD-3 | `ReferenceQueryPort` implemented by each referrer type. | Uses **SendingPathPort** / **FooterPort** read-only; no imports. |

**Fracture (dual reference truth):**

- sending-paths delete guard asks **ListsPort** + **NoticePathPort** only (examples in PRD). send-to-mail stores `footer_id` and derived sending path on **mapping** rows — never registers as referencer (AD does not list MTS as mandatory port implementer for R9).
- footers team ships delete guard that passes; MTS mappings still hold `footer_id` → runtime send uses deleted footer id (404/internal error) or null FK if DB allows.
- Same pattern for **substitute From** tied to sending-path domain validation (FR-29) if paths team deletes path lists no longer reference but MTS mapping still implies domain.

**Hole to close:** Spine **Reference registry**: any slice storing FK-like ids to catalog entities must implement `ReferenceQueryPort` (send-to-mail mandatory for footers + sending-paths + lists on mappings). R9 row in capability map must name MTS, campaigns (cached path), notice-mail.

---

### H5 — `tracking` × `campaigns` × `send-queue`: recipient identity in events

| AD compliance | **tracking** | **campaigns** | **send-queue** |
| --- | --- | --- | --- |
| AD-11, AD-18 | Pixel async persist; opaque token. | Campaign reporting UI (FR-24). | Emits send lifecycle facts. |
| AD-5 | Subscribes to in-process events. | Subscribes to events; no import tracking. | Publishes `SendSucceeded` / queue events. |

**Fracture (clashing event shapes):**

- send-queue events: `{ campaignId, recipientEmail, queueRowId }` (email natural key for SMTP).
- tracking token payload: `{ campaignId, memberId }` (GUID per AD-24).
- campaigns reporting joins opens to “members sent to” on **memberId** from its sent list sourced from send-queue port returning **email-only** rows.
- All AD-valid: no feature imports; GUID rule applies to “public entity ids” but teams differ on **internal join key** for reporting.

**Hole to close:** Kernel event DTO **RecipientRef** `{ memberId, email }` required on enqueue, send, open, unsub token minting; campaigns sent list and tracking must use same ref.

---

### H6 — `campaigns` × `blob-storage` × TTL job: two purge authorities

| AD compliance | **campaigns** | **blob-storage** |
| --- | --- | --- |
| AD-20, AD-23 | Triggers cascade delete on campaign delete via port. | Owns blob bytes and `/dl` handler. |
| AD-13 | Registers scheduled jobs on kernel scheduler. | No scheduler requirement. |

**Fracture (conflicting mutation paths):**

- FR-21: scheduled job deletes expired **campaign content and media** after send finished; TTL from Parameters + per-list rules.
- campaigns team: job deletes campaign content columns + calls **BlobStoragePort.deleteBucket** when `finished_at + ttl` elapses.
- blob-storage team: separate job deletes rows where `created_at + parameter` (misread AD-23 `created_at` as TTL anchor) without campaigns status check.
- AD-15 allows both to read Parameters via port with **different keys** declared in each slice (`campaign.content.ttl` vs `blob.retention.days`) — no AD violation.

**Hole to close:** Single **ContentRetentionPort** owner (campaigns) orchestrates blob purge; blob-storage deletes only via port; one parameter family for TTL anchor (`finished_sending_at`).

---

### H7 — `members` × `bounce` × `send-queue`: global disable write path

| AD compliance | **bounce** | **members** | **send-queue** |
| --- | --- | --- | --- |
| AD-16 | bounce-process disables per FR-26. | Owns member record + disabled flag. | Skips disabled at dequeue via port. |

**Fracture:**

- bounce calls **MemberPort.setDisabled(email, reason)** — members team implements.
- members **also** exposes `disable_reason` for UI on member timeline; bounce writes reason code `BOUNCE_HARD`; send-queue only checks boolean via **MemberQueryPort.isDisabled(email)**.
- Parallel **lists** team implements “removed from list” as soft unsub without touching members — fine.
- Alternative AD-valid bounce team: writes disable only to **attention** slice for visibility, publishes event but **does not** call MemberPort (interpreting disable as ops concern). send-queue never skips; campaigns stats show “sent to disabled” — product violation but AD-silent on **mandatory** MemberPort call.

**Hole to close:** Spine rule: **bounce-process MUST mutate disable only through MemberPort**; send-queue MUST consult same port; attention is derivative. One enum for `disable_reason`.

---

### H8 — `notice-mail` × `send-queue` × `smtp-adapter`: parallel send pipelines

| AD compliance | **notice-mail** | **send-queue** |
| --- | --- | --- |
| AD-16 | Notice/consent/admin sends; Notice path + smtp-adapter. | Production queue + smtp-adapter. |
| AD-13 | May run concurrently with other schedule types. | Sequential heavy path. |

**Fracture (ops + shape clash, not SMTP itself):**

- Both log failures to **ops-log** via separate category writers; AD-14 envelope consistent but **ops-log** slice defines category enum independently — notice-mail uses `NOTICE_SEND_FAIL`, send-queue uses `SEND_FAIL` vs FR-39 “send issues” single category.
- Both emit **Attention** items with different severity rules → duplicate Attention for same SMTP outage (notice path down + queue path down) or missed Attention if ops-log schemas differ (`detail` JSON shapes).

**Hole to close:** Kernel-shared **OpsLogCategory** + **AttentionSeverity** mapping table in spine or kernel conventions (AD-2 allows logging conventions).

---

### H9 — `parameters` × multiple slices: key collision and type drift

| AD compliance | **parameters** | **lists**, **bounce**, **send-queue**, etc. |
| --- | --- | --- |
| AD-15 | Owns persistence + admin API. | Each declares keys (constant + default + type) on port. |

**Fracture:**

- lists declares `spam.threshold` as `BigDecimal`; send-to-mail declares `spam.threshold.global` as `Integer`; both AD-valid “declare keys in slice”.
- Admin UI shows two keys; strictest-wins algorithm (FR-30) undefined at architecture level → incompatible runtime if MTS reads only its key and bounce mailbox reads another.

**Hole to close:** Spine **Parameters registry** (name, type, owner slice, consumers); collisions forbidden at bootstrap; cross-cutting keys owned by one slice (e.g. kernel documents spam threshold resolution owner = send-to-mail for ingest, bounce-mailboxes for poll — or single `delivery.spam.*` owner).

---

### H10 — `consent` × `notice-mail` × `members`: clear-unsub state owner

| AD compliance | **consent** | **notice-mail** | **members** |
| --- | --- | --- | --- |
| AD-16, AD-17 | `/approve/<token>` surface. | Sends consent email. | Member timeline / unsub state. |

**Fracture:**

- consent team persists `consent_request` rows and on approve sets `member.consent_cleared_at` via **MemberPort** — members owns field.
- notice-mail team persists send metadata only; consent approval writes **ConsentApprovedEvent**; members team listens and sets flag; consent slice **also** writes flag via port on approve — double path if both implement approve handler chain.
- Token single-use: consent invalidates token in consent table; members still shows “pending consent” if event order wrong (AD-5 in-process sync listeners order undefined).

**Hole to close:** One writer for consent outcome (**members** or **consent** — pick one); notice-mail send-only; explicit listener order or transactional outbox within monolith (V1: `@TransactionalEventListener` phase documented on spine).

---

## Cross-cutting gaps (amplify all holes)

1. **No kernel port catalog** — AD-2 lists port *names* in prose but not method signatures, DTOs, or owning implementer. AD-3 is unenforceable without it (ArchUnit deferred per spine).
2. **No domain event catalog** — AD-5 allows Spring events; payload and publisher ownership unspecified → H2, H5, H10.
3. **“Internal processes” (AD-23)** — compose, send-to-mail, send-queue may all write blobs; write ACL is role-based at API but **no conflict rules** for same `(bucket, fileName)`.
4. **Campaign GUID public in `/dl` (AD-17) vs AD-24** — consistent, but teams may still mint pre-save client GUIDs (H1) without spine prohibition.
5. **R9 Used-by** assigned to “owning catalog slices” only — referrers must opt in (H4); spine should require opt-in list per catalog type.

---

## What held up under attack

- Feature→feature import ban + utility adaptor pattern is coherent for V1 monolith.
- Public vs operator auth split (AD-6, AD-17, AD-18) closes major security forks if token helper is centralized in kernel.
- Single instance + in-process skip-if-running (AD-10, AD-13) avoids distributed lock arguments for parallel teams.
- send-queue / tracking extract marks (AD-22) remain plausible **if** H2/H5 contracts are kernel-stable early.

---

## Recommended spine amendments (minimal)

| Priority | Amendment |
| --- | --- |
| P0 | **Kernel port + event appendix**: DTOs for Campaign, QueueRow, RecipientRef, ListSnapshot, BlobRef; implementer matrix. |
| P0 | **Campaign content single-writer** rule (H1) + blob re-home on GUID assignment. |
| P0 | **Unsubscribe list snapshot** rule (H3). |
| P1 | **ReferenceQueryPort** registrars including send-to-mail (H4). |
| P1 | **Parameters key registry** with single owner per logical tunable (H9). |
| P1 | **Retention job single orchestrator** (H6). |
| P2 | Ops log / attention category conventions (H8); consent approval single writer (H10). |

---

## Review metadata

- Findings count: **10 hole pairs** (exceeds adversarial skill minimum).
- No code reviewed; spine + initiative/PRD cross-check only.
- `.memlog.md` shows prior blob/campaign GUID revisions — current AD-17/23 text is internally consistent; H1/H6 attack **process** gaps, not known doc contradictions.
