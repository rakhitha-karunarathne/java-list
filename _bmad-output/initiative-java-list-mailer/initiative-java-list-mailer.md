---
type: initiative
title: Java list mailer
parent: none
sources:
  - _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/prd-java-list-mailer.md
  - _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/addendum.md
companions:
  - _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/
  - _bmad-output/forge-java-list-mailer/forge-java-list-mailer.md
  - _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/
risk: high
---

# Java list mailer

## Description

Self-hosted list mailer with phpList-like operator behaviors on a new stack (not a PHP port). Operators manage lists and memberships, compose or ingest campaigns, process bounces into global disable, and run mail-to-send — with catalogs, Attention, audit, and HTTPS member surfaces. Requirements distilled from the PRD via SPEC coaching; UX, forge, and architecture spine (final) are companions.

## Outcome

Operators can leave PHP list software and run campaigns, lists, bounce handling, and mail-to-send on a self-hosted Java stack with phpList-like paths and stronger defaults (HTTPS, Attention, consent clear-unsub).

## Requirements

R1 (prd FR-1, FR-34): Install yields an initial Admin; Admins create/disable Users with local email + password (email is the login id); optional TOTP authenticator-app MFA (enroll + backup codes); Admin may set a Parameter to make MFA mandatory for all operators; roles Admin / List manager / List sender enforce scoped access.

R2 (prd FR-2, FR-34): Per-List manage and/or send grants are edited on User detail; removing a grant takes effect immediately; Admin needs no list grants.

R3 (prd FR-3, FR-37): Sending paths (SMTP + From) are catalogued; detail shows navigable Used By; delete blocked while referenced.

R4 (prd FR-4): Bounce mailboxes are catalogued with per-mailbox poll interval (default 15 min), last polled time, and last-poll Result (OK or Errors). The index columns are Name, Poll interval, Last polled, and Result; Host is not on the index. The poll job wakes every 60 seconds and polls enabled mailboxes that are due. Admin Poll is only on an enabled mailbox, starts that one mailbox, and returns at once with “process started”. A disabled mailbox has no Poll button. A per-mailbox lock blocks a second poll until it is released, broken, or Last Polling Update is older than 1 hour (`mailer.poll.lock-timeout`). Last Polling Update is set at polling start, after each message read, and when polling is done. While locked, detail shows a Polling section and hides it otherwise. Break lock is shown only when that time is at least 60 seconds old. Detail shows that mailbox’s Bounce Processing Log rows when logging is on. Those rows store the mailbox id and the screen shows the name. Existing Bounce and Mail-To-Send processing-log rows are deleted once.

R5 (prd FR-5, FR-37): Footers are catalogued; detail shows Used By (Lists, mappings); delete blocked while referenced; UI campaigns use List footer only.

R6 (prd FR-6): One Notice sending path for system/consent/admin mail; bypasses bounce-disable; Campaign queue never does.

R7 (prd FR-7): Admin-only Parameters hold non-credential tunables (including ops log retention default 90 days, and whether operator MFA is mandatory); each Parameter on the Admin page shows a description covering purpose, supported values, and value range; credentials on paths/mailboxes; signing secrets and MFA secrets/backup codes not in Parameters.

R8 (prd FR-8, FR-39): Attention inbox for human action; Admin sees all including highlighted ops errors; List managers see only their Lists/Members; List senders have no Attention inbox.

R9 (prd FR-37): Shared referenced configs cannot be deleted while in use; Used By links to referencing records.

R10 (prd FR-9): Authorized Users CRUD Lists they manage; each List maps to one Sending path; optional footer, spam threshold, payload/image/TTL overrides; no public/private flag in V1.

R11 (prd FR-35): List page bulk-paste add Members; skips invalid, in-paste dupes, already-on-list, unsubscribed; shows counts; List senders cannot.

R12 (prd FR-10): Member global Enabled/Disabled; Disabled skipped by Campaign queue; `disable_reason` shown; bounce disable ≠ unsubscribe.

R13 (prd FR-11): Re-enable manual or re-add-to-list with confirm (clears streaks) unless unsubscribed on that List; opens never auto re-enable.

R14 (prd FR-12, FR-13): HTTPS unsubscribe + List-Unsubscribe-Post; per List; multi-list campaign unsub removes from all Lists in that campaign; Admin/LM visibility scoped.

R15 (prd FR-14): Clear unsubscribe only via consent email on Notice path; expiry default 14 days; no unilateral clear; audited.

R16 (prd FR-36): Searchable Members index in sidebar; Admin all / LM scoped; open → Member page; detail not a sidebar destination.

R17 (prd FR-15): Member page shows lists on/unsubscribed, status, timeline = audit filtered by Member.

R18 (prd FR-16): Multi-list Campaign; same address → one queue row; uses first selected List’s Sending path.

R19 (prd FR-17): Text + HTML, images, attachments; compose has HTML/Text preview pane.

R20 (prd FR-38): Test send from compose to pasted addresses; uses first List’s path; ignores bounce-disable; not campaign sent-stats; failures → Ops Logs / Admin Attention.

R21 (prd FR-18, FR-19, FR-41, FR-45): System send-queue processor wakes every 60 seconds from application.yml; system-wide pacing only (delay per email 1000 ms, batch size 600, batch duration 10 minutes, 3600 messages per rolling hour; a run sends only the room left in that hour); Lists only enqueue. Parameter `send.paused` (default false) stops the queue before the next recipient; test sends and Notice-path mail still go out. One SMTP failure becomes Failed-Retry Pending and other recipients continue; after 3 further tries (`send.retry.count`, default 3), each delayed 5 minutes (`send.retry.delay.minutes`), the row is Send Failed. No button runs the send queue. Pause flags are cached and cleared in this process when set to paused. Multi-instance cache clear is V2+.

R22 (prd FR-20, FR-21): Image-fetch Off / Best Effort / Mandatory + SSRF guards; rewritten images CDN TTL after campaign’s most recent Finished (Requeue that finishes again restarts TTL; Stopped does not).

R23 (prd FR-22, FR-23): Email payload and stored-image hard uppers; per-List ≤ ceiling; multi-list strictest (TTL longest wins).

R24 (prd FR-24): Campaign detail recipients table lists every recipient with Email, Status (Pending, Failed-Retry Pending, Sent, Send Failed, Opened, Bounced), First Open, and Opens. Bounced wins over Opened; first open and open count still show. One search box matches email or status. Retry failed requeues Send Failed rows. Stats: unique opens (headline), total opens, sent count; HTML pixel only.

R25 (prd FR-39): Ops Logs for send / bounce poll / mail-to-send poll; errors on Admin Attention; retention via Parameters (default 90 days).

R26 (prd FR-25): Bounce correlation (VERP optional → token → Message-ID/headers → subject+email); uncorrelated never auto-disable / never streak; logged + Attention.

R27 (prd FR-26): Daily bounce-process; separate hard/soft walks + holdback; either limit disables; soft independent after init.

R28 (prd FR-27): VERP toggle on Parameters; default off.

R29 (prd FR-28): IMAP-only mail-to-send; index columns Name, Poll interval, Last polled, Result (Host and Enabled are not index columns); poll interval default 15 min; 60-second wake polls due enabled inboxes; Admin Poll is only on an enabled inbox and returns at once with “process started”; a disabled inbox has no Poll button; a per-inbox lock blocks a second poll until released, broken, or Last Polling Update is older than 1 hour; Last Polling Update is set at start, each message, and done; while locked, detail shows a Polling section and hides it otherwise; Break lock shows only when that time is at least 60 seconds old; detail shows that inbox’s Mail-To-Send Processing Log rows when logging is on, by inbox id, with the name on screen; authorized senders; mappings; substitute From domain rule; footer override.

R30 (prd FR-29): From-domain Issues (not Drop log); all-or-nothing; hold or delete-and-notify.

R31 (prd FR-30): Auth then spam-header-if-present; DMARC fail = reject treatment; outcome matrix (silent / notify / auto-block rules).

R32 (prd FR-31): Drop log + global blocks; auto-block sender-only after N/day; no stranger courtesy reject.

R33 (prd FR-32): Single append-only Audit log; no raw secrets; Admin all / LM member-scoped; retention default 365 days; includes Campaign status changes (Queue, Stop, Requeue, Finished, list added for Finished Requeue).

R34 (prd FR-33): HTTPS only for UI, unsub, pixel, rewritten images, List-Unsubscribe; Public base URL; origin TLS Full or Full Strict only.

R35 (prd FR-40): Campaign status is exactly one of Draft · Queued · Sending · Paused · Finished · Stopped. Compose until queue → Draft. Queue, Requeue, or Resume → Queued; when the send-queue processor starts work → Sending; after the final recipient email for the current send pass is sent → Finished (Failed-Retry Pending keeps the pass unfinished; Send Failed does not). Operator may Pause while Queued or Sending → Paused. Resume → Queued without Requeue. Operator may Stop while Draft, Queued, or Sending → Stopped (remaining unsent rows are not sent while Stopped). **Requeue:** Stopped → Queued (resume: remaining unsent rows, plus any newly added List members whose addresses were not already sent for this Campaign). Finished may be Requeued only after at least one additional List is added → Queued; that pass enqueues addresses on the newly added List(s) only, excluding any address already sent for this Campaign.

R36 (prd FR-42): Campaign Pause from Queued or Sending sets status Paused. Resume is an action, not a status, and sets Queued without Requeue. Admin, List manager, and List sender may do this on a campaign they can open.

R37 (prd FR-43): Each List has Pause and Resume, a hold rather than a campaign status. Admin any List; List manager lists they manage; List sender cannot. Rows already queued for a paused List wait. An address queued under a List that is not paused still sends. Mail-to-send may still create a campaign for a paused List; those rows wait until the List is resumed.

R38 (prd FR-44): Each of the Bounce, Queue Sending, and Mail-To-Send processing logs has its own age Parameter (default 30 days; a stored 90 is updated to 30) and its own keep-count Parameter (default 5000). The nightly job drops rows that are too old or outside the newest N. Ops log stays 90 days. Audit stays 365 days.

## Constraints

C1 (addendum / forge): Relational store is MariaDB (not MySQL).

C2 (addendum / forge / architecture AD-7): V1 app + schedulers in one container; Docker Compose also runs MariaDB and phpMyAdmin. MariaDB is not embedded in the app image. phpMyAdmin is private-only — not on the Cloudflare public hostname/tunnel.

C3 (addendum / forge): Backend Java + Spring Boot with Spring schedules; frontend React.

C4 (prd FR-33 / Boundaries): Flexible TLS mode unsupported; public member/operator links HTTPS-only.

C5 (prd Non-Goals): Not a phpList port or PHP compatibility layer.

C6 (prd FR-7 / FR-34): Signing secrets and mailbox/path credentials never stored as Parameters plaintext or written raw into Audit.

C7 (prd FR-25): Uncorrelated bounces must never drive auto-disable or streak reset.

C8 (ux / prd IA): Detail pages for List, Member, Campaign, inbox, bounce mailbox, sending path, footer, user, ops log are reached from indexes/deep links — not as primary sidebar destinations (Compose and Ops Logs index as already specified).

## Done when

1. Admin can stand up catalogs + Parameters and send a multi-list campaign end-to-end.
2. Mail-to-send authorized path creates a campaign; strangers stay silent / logged.
3. Bounce-process disables on hard/soft limits; uncorrelated never auto-disable; Attention surfaces ops errors.
4. Member unsubscribe + consent clear-unsub work over HTTPS and are audited.
5. Single Docker deploy with MariaDB; public links HTTPS only (Full / Full Strict).

## Boundaries

- Out of V1: public subscribe / preference center / template library; click tracking; POP3; **plugin pack implementations** (filter-chain **seams** ship in platform — AD-28); Cloudflare Flexible; external spam service / AI spam scoring.
- No List public/private visibility flag in V1 (deferred intent only).
- Touch point: architecture spine — binds deploy and module boundaries when finalized; owner: architecture-java-list-mailer
- Tracer path: catalogs → lists/members → compose or mail-to-send → queue → bounce/ops → Attention/audit

## References

- prd — `_bmad-output/initiative-java-list-mailer/prd-java-list-mailer/prd-java-list-mailer.md`
- prd-addendum — `_bmad-output/initiative-java-list-mailer/prd-java-list-mailer/addendum.md` (stack, defaults, bounce algorithm)
- ux — `_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/` (mock + memlog; spines when final)
- forge — `_bmad-output/forge-java-list-mailer/forge-java-list-mailer.md`
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/` (final spine)

## Notes

- Decision: 2026-10-25 — Outcome, Done when, Boundaries approved (SPEC coaching).
- Decision: 2026-10-25 — Requirements R1–R34 and Constraints C1–C8 approved in batches.
- Decision: 2026-10-25 — Ops log retention is a Parameters field, default 90 days (closes prior Assumption).
- Decision: 2026-10-25 — Compose: app + MariaDB + phpMyAdmin; phpMyAdmin private-only / off Cloudflare public path (architecture AD-7).
- Decision: 2026-10-25 — SPEC self-validate Pass 1 (coherence) and Pass 2 (preservation) recorded in memlog.
- Decision: 2026-10-25 — Plugin seam: filter chains on each kernel port attachment and each API endpoint (architecture AD-28). V1 ships hooks; plugin pack implementations remain out of V1 (Boundary).
- Decision: 2026-10-25 — Initiative sliced into 9 epics (platform → users → catalogs → lists/members → campaigns/send → unsub/consent → bounce → MTS → ops/Attention/audit). First product demo after campaigns epic; MTS after bounce for mailbox-query; thin Compose demo after platform/users.
- Decision: 2026-10-26 — Frontend Clearwater SPA sliced into 5 new epics after API epics 1–9: epic-frontend-shell → catalogs → lists-members → campaigns → ops-mts (stories refined; UX EXPERIENCE/DESIGN companions).
- Decision: 2026-10-26 — Clearwater clickable mock approved (`ux-java-list-mailer/mockups/clickable/index.html`); build target for frontend epics; spines win on conflict.
- Decision: 2026-10-25 — Operator username/login id is email (R1).
- Decision: 2026-10-25 — Operator TOTP MFA optional by default; Admin can make MFA mandatory via Parameters (R1/R7).
- Decision: 2026-10-25 — Each Parameter on Admin page shows description: purpose, supported values, value range (R7).
- Decision: 2026-10-25 — Campaign states: Draft · Queued · Sending · Finished · Stopped (R35 / FR-40). Draft until queue; Queued then Sending; Finished after last send; Stop from Draft/Queued/Sending.
- Decision: 2026-10-25 — Stopped can Requeue → Queued. Finished can Requeue only after adding another List; new pass sends new-list addresses excluding already-sent (R35).
- Assumption: 2026-10-25 — Mail-to-send–created Campaigns enter Queued (skip Draft compose). “Already sent” = address that received a production queue send for this Campaign (test sends do not count).
- Decision: 2026-10-25 — Propagated R35 to architecture AD-16, UX EXPERIENCE Flow 4, PRD glossary/UJ-3/FR-21/FR-32, epics campaigns/MTS/ops/lists.

