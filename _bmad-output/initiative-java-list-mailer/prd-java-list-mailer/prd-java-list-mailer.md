---
title: Java list mailer
status: final
created: 2026-10-25
updated: 2026-10-25
---

# PRD: Java list mailer

## 0. Document Purpose

This PRD defines V1 product requirements for a self-hosted list mailer with phpList-like operator behaviors on a new stack. It is for product owners, UX, architecture, and epic breakdown. Vocabulary is Glossary-anchored; features carry globally numbered FRs. Prior decisions live in `_bmad-output/forge-java-list-mailer/forge-java-list-mailer.md` (party-mode hardened draft). Implementation choices (stack, schedules, algorithms as ops detail) are in `addendum.md`.

## 1. Vision

Operators who run member lists and campaigns today on PHP-based list managers need a path off PHP without relearning how list mail works. Java list mailer is a **new product** — not a port — that preserves the interaction patterns operators already know: compose and queue campaigns, manage lists and memberships, process bounces into global disable, and turn approved inbox mail into campaigns (mail-to-send).

V1 ships as a single deployable unit behind HTTPS (public hostname via Cloudflare or equivalent). Admins configure catalogs (sending paths, bounce mailboxes, footers, notice path, system Parameters) once; list managers and senders work within scoped lists. Members stay safe: unsubscribe is per-list and consent-gated to clear; bounce disable is global and never driven by unmatched noise.

The product wins when an existing phpList-shaped ops team can run the same daily paths with clearer catalogs, stronger bounce/mail-to-send security defaults, and an Attention inbox for human-only decisions — without waiting for a plugin marketplace in V1.

## 2. Target User

### 2.1 Jobs To Be Done

- **Functional:** Send campaigns to one or more lists with throttling, footers, and delivery paths they control; ingest approved sender mail into campaigns; keep bounce-bad addresses from receiving more mail; clear unsubscribe only with member consent.
- **Operational:** See what needs a human (uncorrelated bounces, From-domain issues, holds, Mandatory image failures) in one Attention place; audit who changed membership, config, and consent.
- **Migration:** Leave PHP list software without abandoning list/campaign/bounce mental models.
- **Trust:** Prefer silent drop over backscatter; HTTPS-only member-facing links; signed pixels and unsubscribe tokens.

### 2.2 Non-Users (v1)

- End-member self-serve preference centers / public subscribe flows (deferred past V1; see §5 / §6.2).
- Marketing automation / multi-channel journey builders (Mautic-class).
- Teams that need click tracking or reply-desk workflows in V1.
- Operators who require Cloudflare Flexible (HTTP to origin) — unsupported.

### 2.3 Key User Journeys

Internal multi-role tool — light journeys; named operators for downstream UX.

- **UJ-1. Ava (Admin) stands up catalogs and Parameters.** Ava signs in as Admin, creates sending paths and bounce mailboxes, sets the notice sending path, and tunes Parameters (bounce limits, throttle, image mode). She confirms the public HTTPS base URL and that origin TLS is Full or Full Strict. **Climax:** catalogs save; Attention is empty. **Edge:** Flexible mode is rejected / documented as unsupported.

- **UJ-2. Mei (List manager) maintains membership and watches Attention.** Mei opens her lists, paste-adds a block of addresses (skips invalid/dupes/already-on-list), removes members, reviews bounce-disabled members with `disable_reason`, and acts only on Attention items for her lists/members. **Climax:** membership and status match reality; scoped Attention is clear.

- **UJ-3. Sam (List sender) composes and queues a multi-list campaign.** Sam picks lists, composes HTML+text, passes payload/image gates, queues. Deduped recipients use the first selected list’s sending path. Queue processor sends under system throttle. **Climax:** campaign finishes; open stats show unique vs total opens.

- **UJ-4. Sam sends via mail-to-send.** Sam’s authorized From hits the mapped inbox; auth/spam pass; campaign is created (or held/deleted-and-notified on From-issue). **Climax:** one inbox message → one campaign; wrong-mailbox gets a notice, strangers get silence.

- **UJ-5. Jordan (Member) unsubscribes and later is invited back.** Jordan clicks HTTPS unsubscribe (or one-click List-Unsubscribe-Post) and leaves all lists in that campaign. Later Mei requests clear-unsubscribe; Jordan gets accept/reject via notice channel (even if bounce-disabled); reject or 14-day expiry leaves them unsubscribed. **Climax:** consent is recorded on the member timeline / audit store.

## 3. Glossary

- **User** — Login account (Admin, List manager, or List sender). Not a list recipient.
- **Member** — Person/address on lists (phpList “user”). Global enable/disable status lives on the Member.
- **List** — Named collection of Members with optional footer, spam threshold, payload/image/TTL overrides; maps to exactly one Sending path.
- **Sending path** — Catalog SMTP connection + From (full email). Many Lists may share one path.
- **Bounce mailbox** — Catalog IMAP mailbox polled for bounce/DSN mail (not part of List config).
- **Footer** — Catalog reusable footer content; Lists and mail-to-send mappings reference it.
- **Notice sending path** — Single UI-configured path for rejections, issue notices, admin digests, consent emails, and other system mail. **Bypasses bounce-disable**; may reach an unsubscribed address **only** for consent/admin notices.
- **Parameters** — Admin-only system-wide UI-tunable non-credential settings (limits, schedules, throttle, TTLs, modes, etc.). Credentials stay on Sending paths / mailboxes; signing secrets stay in env/secret file.
- **Attention dashboard** — Inbox of items needing human action (links into bounce log, Issues, etc. — not a second store).
- **Campaign** — Outbound send targeting one or more Lists; may be UI-composed or created from mail-to-send.
- **Send queue** — Per-recipient rows for a Campaign (deduped by address across selected Lists).
- **Mail-to-send** — IMAP inbox ingest that turns authorized messages into Campaigns via inbox→list mappings.
- **Drop log** — Per-mailbox (and combined) log of rejected/dropped mail-to-send messages with reason and auto-block contribution flag.
- **Issues** — Operator surface for From-domain / mapping problems (not the Drop log).
- **Audit log** — Single append-only store of membership, unsubscribe/consent, bounce disable/enable, campaign send/open/bounce, notices, and V1 config changes. Member timeline and system audit UI are views of this store.
- **Hard bounce / Soft bounce** — Bounce classes with separate consecutive counts, limits, and holdback; hitting either limit disables the Member globally.
- **VERP** — Optional per-message Return-Path correlation; system-wide Parameters toggle; default off.
- **Public base URL** — Cloudflare (or equivalent) public HTTPS hostname used in pixels, unsubscribe, rewritten images, List-Unsubscribe.
- **Image-fetch mode** — System-wide Off / Best Effort / Mandatory for remote URL images in HTML.

## 4. Features

### 4.1 Access and roles

**Description:** Three login roles with scoped authority. Admin has full access; List managers manage assigned Lists (and see Attention/audit scoped to their Lists’ Members); List senders send to assigned Lists. Realizes UJ-1–UJ-3.

**Functional Requirements:**

#### FR-1: Role model

Admin can access all product surfaces. List managers can manage only Lists they are granted. List senders can send only to Lists they are granted. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- A List manager cannot edit Parameters or system-wide catalog credentials.
- A List sender cannot change membership or clear unsubscribe.
- A List sender cannot open the Attention dashboard as an operator inbox.

#### FR-2: List grants

Admin (and List managers where granted) can grant manage and send rights on specific Lists. Grants are edited on the User detail page (FR-34 UI) together with that User’s settings.

**Consequences (testable):**
- Removing a grant immediately removes that User’s access to those List actions.

### 4.2 Catalogs and Parameters

**Description:** Shared catalogs for Sending paths, Bounce mailboxes, Footers, Notice sending path, Parameters, and Attention. Lists and mappings reference catalogs; they do not embed SMTP/credentials. Realizes UJ-1.

**Functional Requirements:**

#### FR-3: Sending paths catalog

Admin can create/edit/delete Sending paths (SMTP + From email), subject to FR-37. Lists map to exactly one Sending path; many Lists may share one path.

**UI:** Sidebar **Sending paths** lists paths. Edit From/SMTP/credentials on a path detail page opened only by clicking a path (not a sidebar item). Detail shows navigable **Used by** references (FR-37).

**Consequences (testable):**
- Changing a path’s From/domain is allowed without blocking save; dependent mappings are re-validated and Issues opened as needed (see FR-28).
- Path detail is reachable from the Sending paths index, not as its own nav entry.

#### FR-4: Bounce mailboxes catalog

Admin can configure many Bounce mailboxes with per-mailbox poll interval (default 15 minutes).

**UI:** Sidebar **Bounce mailboxes** lists mailboxes. Edit IMAP/credentials/poll on a mailbox detail page opened only by clicking a mailbox (not a sidebar item).

**Consequences (testable):**
- Changing a mailbox’s poll interval affects only that mailbox’s next scheduled poll cadence.
- Mailbox detail is reachable from the Bounce mailboxes index, not as its own nav entry.

#### FR-5: Footers catalog

Admin can manage Footers as standalone records, subject to FR-37. Lists optionally reference one Footer. UI-composed Campaigns use the List Footer only (no mapping Footer). Mail-to-send: mapping Footer overrides List Footer when set; else List Footer; else none.

**UI:** Sidebar **Footers** lists footers. Edit content on a Footer detail page opened only by clicking a footer. Detail shows navigable **Used by** references (FR-37).

**Consequences (testable):**
- A UI Campaign for a List with no Footer sends without a Footer catalog block (unsubscribe link requirements in FR-12 still apply via product footer rules — `[ASSUMPTION: unsubscribe link is always injected even when Footer catalog is empty]`).

#### FR-37: Referenced config — no delete while in use

Catalog / configuration records that other entities reference (at least **Sending paths** and **Footers**; any other shared config with incoming references follows the same rule) **cannot be deleted** while any reference remains. The detail page for such a record lists every referencing record (**Used by**), with links that navigate to those records (e.g. List detail, Notice Sending Path, mail-to-send inbox/mapping). Delete is disabled or rejected with a clear message until references are cleared or reassigned. Applies to Admin catalog maintenance. Realizes UJ-1.

**Consequences (testable):**
- Attempting to delete a Sending path still referenced by a List fails; path remains.
- Footer detail lists each List and mail-to-send mapping that references it; each row links to that List or inbox detail.
- After all references are removed, delete succeeds.

#### FR-6: Notice sending path

Admin can configure exactly one Notice sending path used for system/consent/admin notices. Mail on this path bypasses bounce-disable; campaign Send queue never bypasses disable or unsubscribed memberships.

**Consequences (testable):**
- A bounce-disabled Member still receives a clear-unsubscribe consent email via the Notice sending path.
- The same Member does not receive Campaign mail while Disabled.

#### FR-7: Parameters (admin-only)

Admin can edit all system-wide non-credential tunables on a single Parameters page/entity (bounce limits, VERP, schedules including bounce-process default 02:00 and timezone default UTC, throttle, image fetch timeout default 30s, payload/image hard uppers, campaign TTL default/max, bounce log TTLs, audit retention, image-fetch mode, consent expiry, auto-block default 10/day, hard bounce limit default 3, soft default initially 2× hard then independent, holdback multiplier default 2, etc.). Credentials remain only on Sending paths / mailboxes. Signing secrets for pixel/unsubscribe/consent are not stored as Parameters plaintext.

**Consequences (testable):**
- Changing hard bounce limit does not change an already-customized soft limit.
- Parameters UI never displays or accepts the pixel/unsubscribe signing secret as a field.

#### FR-8: Attention dashboard

Admin and List managers see an Attention inbox of items needing human action. V1 item types: uncorrelated bounces; From-domain/sender Issues; mail-to-send holds; Mandatory image/payload failures; **ops errors** from FR-39 (send failures, bounce-mailbox poll failures, mail-to-send inbox poll failures) — these ops errors are **Admin-only**, visually highlighted (severity/severity) as errors, and link into Ops Logs. Admin sees all; List managers see only items for their Lists/Members; system-wide items (including ops errors) are Admin-only. List senders do not use Attention as an operator inbox in V1 (they may still receive notice emails for Mandatory/payload failures on their sends). Items link into existing stores (bounce log, Issues, Ops Logs, etc.) — Attention is not a second store.

**Consequences (testable):**
- An item leaves Attention when the underlying condition is cleared (e.g. Issue resolved, hold processed, ops error resolved) or when an authorized operator dismisses it.
- Dismiss does not delete the underlying bounce/Issue/drop/ops-log record.
- A List manager never sees Admin-only system-wide Attention items (including ops poll/send errors).
- An Admin Attention list shows ops errors with error highlighting distinct from warn-level items.

### 4.3 Lists and membership

**Description:** CRUD Lists and membership; global Member enable/disable; per-list unsubscribe. Realizes UJ-2, UJ-5.

**Functional Requirements:**

#### FR-9: List lifecycle

Authorized Users can create, delete, edit description, and add/remove Members on Lists they manage. Each List maps to one Sending path; optional spam threshold, Footer, payload/image/TTL overrides. V1 has **no** public/private visibility flag — Admin and List managers add Members as today (including FR-35); Members unsubscribe per FR-12. Post-V1 visibility rules are deferred in §6.2 (not implemented in V1).

#### FR-35: Bulk paste add Members on List page

On the List page, an authorized User (Admin or List manager for that List) can paste a list of email addresses into a text box and add them as Members in one action (phpList-like). Accept newline-, comma-, or semicolon-separated addresses. The system automatically excludes: invalid addresses; duplicates within the paste (case-insensitive); addresses already active Members of this List. Existing global Members not on this List are linked (not duplicated). Addresses unsubscribed from this List are skipped (re-add ignored per FR-11). After submit, show counts: added, skipped-invalid, skipped-duplicate-in-paste, skipped-already-on-list, skipped-unsubscribed. Audited as membership adds. Realizes UJ-2.

**Consequences (testable):**
- Pasting the same valid new address twice in one box adds the Member once.
- Pasting an address already on the List adds zero memberships for that address and reports it as already-on-list.
- Pasting `not-an-email` does not create a Member and reports skipped-invalid.
- A List sender cannot use bulk paste add.

#### FR-10: Global Member status

A Member is Enabled (may receive campaign mail on Lists they belong to and are not unsubscribed from) or Disabled (Send queue skips them globally). Disable shows `disable_reason` (which limit: hard vs soft, plus latest bounce error). Bounce disable is separate from unsubscribe. There is no global “blocked” from unsubscribe.

#### FR-11: Re-enable after bounce disable

Operators can re-enable manually, or re-add the Member to a List (with confirm) which re-enables and clears streak state — unless the Member is unsubscribed from that List (re-add ignored). Open/view hits never auto re-enable.

#### FR-12: Unsubscribe (per List)

Every outbound Campaign email includes a visible HTTPS unsubscribe link in the Footer and List-Unsubscribe / List-Unsubscribe-Post (HTTPS only; no mailto; one-click Post in V1). Unsubscribe is per List. From a multi-list Campaign, unsubscribe removes the Member from all Lists included in that Campaign; other Lists are unchanged.

#### FR-13: Unsubscribe visibility

Admins see all List unsubscribes; List managers see only their Lists. Same data appears on the Member page.

#### FR-14: Clear unsubscribe (consent)

Admin or List manager cannot unilaterally clear unsubscribe. Request clear → email Member via Notice sending path with accept (clear / re-subscribe) or reject (remain unsubscribed). Until accept, stay unsubscribed; re-add ignored. Pending request expires after Parameters consent expiry (default 14 days) with no change; record invite expired on Member timeline/audit. Audit request, response, expiry.

#### FR-36: Members index

Admin and List managers have a sidebar **Members** page: a searchable list of Members (email search at minimum). Admin sees all Members; List managers see Members who belong to (or are unsubscribed from) Lists they manage. Selecting a row opens the Member page (FR-15). List senders do not get this page. Realizes UJ-2.

**Consequences (testable):**
- Typing a substring of an email filters the visible rows.
- A List manager never sees a Member who has no relationship to their Lists.
- Member detail is not a top-level sidebar item — entry is from this index (or in-context links from List / Campaign / Bounce views).

#### FR-15: Member page

Member page shows Lists they are on, Lists they unsubscribed from, global status + `disable_reason`, and Member timeline = Audit log filtered by this Member (send, open, bounce, disable/enable, unsubscribe, consent lifecycle, notices). Opened from the Members index (FR-36) or deep links elsewhere — not a sidebar destination.

### 4.4 Campaigns, queue, throttle, content limits

**Description:** Compose or ingest Campaigns; dedupe; process Send queue under system throttle; enforce payload and stored-image rules; TTL for campaign media. Realizes UJ-3.

**Functional Requirements:**

#### FR-16: Multi-list Campaign and dedupe

A Campaign may target multiple Lists. Same address on more than one selected List → one Send queue row. That row uses the first selected List’s Sending path (and that path’s From unless a mail-to-send substitute applies). UI-composed Campaigns have no substitute From.

#### FR-17: Content types

Campaigns support text and HTML, images, and attachments. HTML with CID/inline images is preserved. Real attachments stay attachments. Remote URL images follow Image-fetch mode (FR-19).

**UI:** Campaign compose includes a **preview pane** (HTML and text views) alongside the editors so operators can check rendered content before queue.

**Consequences (testable):**
- Changing HTML or text body updates the corresponding preview without leaving the compose page.
- Operator can switch preview between HTML and text.

#### FR-38: Test send (compose)

On Campaign compose (phpList-like), an authorized sender can enter a list of email addresses and **send a test** of the current content for troubleshooting. Accept newline-, comma-, or semicolon-separated addresses; skip invalid / in-paste duplicates and report counts. Test uses the same content and the first selected List’s Sending path (From/path as for a real send of this draft). Test sends are **not** Campaign membership queue rows: they do not count toward Campaign sent/open stats and do not apply list unsubscribe membership rules. **Test send ignores bounce-disable** (and may deliver to a globally Disabled Member address) so operators can troubleshoot delivery. Failures are recorded in Ops Logs (send) and surface on Admin Attention per FR-8 / FR-39. Audited. Realizes UJ-3.

**Consequences (testable):**
- Operator can send a test to one or more addresses without queuing the Campaign to list Members.
- Invalid addresses in the test box are skipped and counted, not sent.
- A successful test does not increment Campaign “sent” for list recipients.
- A test to a bounce-disabled address is attempted (not skipped for disable); production Campaign queue still skips Disabled Members.

#### FR-39: Ops Logs (send / poll issues)

Admins have Ops Logs covering at least: **send issues** (production queue and test-send SMTP/transport failures); **bounce mailbox polling issues**; **mail-to-send inbox polling issues**. UI may be one filtered log with category views or separate detail pages linked from an index — each entry shows time, category, severity, summary, and detail. **Error**-severity entries are highlighted on the **Admin Attention** list (FR-8) with navigation into the Ops Log entry. List managers / List senders do not get Ops Logs in V1. Retention follows a Parameters-configurable TTL or audit-adjacent purge — `[ASSUMPTION: default 90 days for ops log detail; confirm with Parameters defaults later.]`

**Consequences (testable):**
- A bounce IMAP poll auth failure creates an Ops Log error and an Admin Attention item linking to it.
- A mail-to-send poll failure likewise.
- A Campaign or test-send SMTP failure appears under send issues and on Admin Attention when severity is error.

#### FR-18: Send queue processor

A system-wide scheduled Send queue processor (interval on Parameters, default 15 minutes) sends queue rows. Lists/Campaigns only enqueue; they do not enforce throttle. Send failures are recorded per FR-39.

#### FR-19: System throttle (Parameters only)

Throttle is system-wide only (no per-list/path overrides in V1): message interval default 1s after each successful send; messages per run default 900; message rate default 3600/hour rolling. Enforced only by the Send queue processor.

#### FR-20: Image-fetch mode

System-wide Off / Best Effort (default) / Mandatory. Off leaves remote URLs. Best Effort fetch/store/rewrite when possible; on fail or mid-way stored-image cap, keep original URL for remainder, still send; soft-skips logged on Campaign detail only. Mandatory requires every remote image rewritten; any fail or cap block fails the whole message and notifies sender (no Campaign / block before queue for UI compose). UI compose honors the same modes. SSRF guards on all fetch: http/https only; block loopback/private/link-local (incl. cloud metadata); re-check after DNS; per-image time (default 30s) and size caps. SSRF refusal/timeout = fetch fail for mode rules.

#### FR-21: Serving rewritten images and TTL

Rewritten images are served with long-lived cache headers behind the Public base URL CDN. Campaign content and stored media expire after the Campaign finished sending: system default 180 days; hard max on Parameters default 365; per-List TTL ≤ hard max (empty → system default); multi-list → longer TTL wins, still capped. Scheduled job deletes expired content/media.

#### FR-22: Email payload limit

Email payload = HTML/text + CID/inline + file attachments (excludes rewritten URL images). Hard upper on Parameters default 5 MB; per-List ≤ hard upper; multi-list → strictest. Mail-to-send over limit: no Campaign, notify sender, Drop log row, does **not** count toward auto-block. UI compose: block with clear error before queue.

#### FR-23: Stored-image limit

Separate per-Campaign total of rewritten image bytes. Hard upper default 5 MB; per-List ≤ hard upper; multi-list → strictest. Over limit: Best Effort soft-skip remaining; Mandatory fail-whole + notify.

#### FR-24: Campaign reporting

Campaign detail lists Members sent to with first open time and open count; campaign stats: unique opens (headline open rate = unique/sent), total opens, sent count. Open tracking is HTML tracking pixel only (no open signal on text-only). Pixel/unsubscribe/consent links use signed tokens; signing secret in env/secret file (key id rotation preferred).

### 4.5 Bounce processing

**Description:** Correlate bounces to Send queue rows; consecutive hard/soft walks with holdback; disable globally; never auto-disable on uncorrelated. Realizes UJ-2.

**Functional Requirements:**

#### FR-25: Bounce correlation

At send, store correlation signals on the queue row. Match order when VERP on: VERP → `X-JavaList-Token` → Message-ID/other standard headers → subject+email. When VERP off: skip VERP. A bounce must identify email and Campaign (not source List). Non-bounce replies in Bounce mailboxes are discarded in V1. Uncorrelated bounces never count toward disable, never soft-link into consecutive walks, never count as “no bounce” for streak reset; they are logged (full content/headers) and surfaced on Attention. Visibility: Admin all; List manager when recipient is Member of a managed List.

#### FR-26: Bounce-process and consecutive limits

Bounce mailbox poll (ingest/correlate/log) is separate from bounce-process (consecutive counting + auto-disable). Bounce-process runs system-wide per Parameters (default once daily 02:00 in Parameters timezone, default UTC). Hard and soft are separate walks/limits/holdback; hitting either disables. Hard default 3; soft initial default 2× hard then independent (changing hard does not auto-update soft). Holdback multiplier default 2: do not mark the newest (multiplier × that class’s limit) messages processed. Algorithm: walk sends outer-joined to bounces oldest→newest; no correlated bounce resets that class; on limit hit disable and mark all bounces for that email processed; otherwise mark processable rows processed respecting holdback. Bounce-process does not reset streaks on SMTP SEND OK. Payload oversize does not increment auto-block. Bounce log TTLs: processed default 180 days from processed_at; unprocessed default 365 days from received_at (both delete full content/headers). Bounce list UI shows extracted error per row.

#### FR-27: VERP toggle

Admin can enable/disable VERP on Parameters (default off). Use only when outbound mail supports per-message Return-Path.

### 4.6 Mail-to-send

**Description:** IMAP-only inboxes, authorized senders, mappings, auth/spam gates, holds, Drop log, global blocks. Realizes UJ-4.

**Functional Requirements:**

#### FR-28: Inboxes and mappings

Admin configures IMAP inboxes (no POP3) with per-inbox poll interval (default 15 minutes), authorized senders, and inbox→List mappings. Substitute From is per sender+inbox+List; empty → Sending path From. Substitute domain must match the domain of that List’s Sending-path From. Each accepted message → one Campaign; mapping Lists = Campaign Lists; dedupe inside Campaign. Two inbox copies → two Campaigns. Prefer IMAP CREATE for hold folders. Footer: mapping Footer wins over List Footer over none.

**UI:** Sidebar **Inboxes & mappings** is an index of inboxes plus an all-mappings overview. Per-inbox settings, authorized senders, and that inbox’s mappings live on an inbox detail page opened only by clicking an inbox (or mapping row that targets it) — not a sidebar item.

#### FR-29: From-domain issues

On Sending-path From/domain change, re-validate mappings; open Issues (not Drop log). From-issue on any target List → all-or-nothing (no partial Campaign). Prefer move issue-sender mail to hold folder per sender per inbox; notify sender; process when cleared; then remove folder. If CREATE fails → delete-and-notify (must resend). Notices clearly state held vs deleted. From-issue does not use Drop log or auto-block counter.

#### FR-30: Auth and spam order

For authorized From on an inbox: (1) SPF/DKIM/DMARC — DMARC fail always treated as p=reject regardless of published policy; (2) spam only if score header present (else skip, not fail); (3) From-issue / create Campaign. Outcomes: auth/spam fail with From authorized → silent delete + Drop log (no auto-block); auth pass not approved on any inbox → silent delete + Drop log + auto-block count; auth pass approved on another inbox but not this → reject + notify + Drop log (no auto-block); strangers on fail paths → silent + Drop log as applicable; From-issue → hold or delete-and-notify + Issues only. Spam thresholds: global, per mailbox, per authorized sender, per List — strictest wins; multi-list mapping fail on any List rejects entire message. V1 reads spam score header only if present.

#### FR-31: Drop log and auto-block

Drop log per mailbox + combined filterable view; mailbox config links open the Drop log pre-filtered to that mailbox; each row has reason + whether it counted toward auto-block. Admin can resolve, block sender, block domain, ignore. Blocks are global (sender + domain lists; Admin can unblock). Auto-block after N counted rejects/day (Parameters, default 10) — sender only, never auto domain. No hourly cap in V1. Daily digest on Notice path only if new Drop log items that day. Prefer mail-server edge rejection; the app still validates inbox mail. No courtesy rejection to random unapproved strangers (backscatter); exception: cross-inbox authorized notice. Blocked sender/domain checked after auth; silent drop. `[ASSUMPTION: mail-to-send inbox addresses are not public and are hard to guess.]`

### 4.7 Audit, retention, HTTPS surface

**Description:** One Audit log; retention; all member-facing and rewritten-image URLs HTTPS via Public base URL; origin TLS Full or Full Strict only.

**Functional Requirements:**

#### FR-32: Audit log

Single append-only Audit log covers membership/unsubscribe/clear-unsubscribe/bounce disable/enable, Campaign send/open/bounce outcomes, consent lifecycle (including invite expired), notice emails, related member–list actions, and V1 config changes (Parameters and catalogs): who, when, what changed. Never write raw secrets — record credentials set/rotated only. Member self-unsubscribe audited (actor = member/system). Admin sees all (including config); List manager sees member-scoped entries only for Members on Lists they manage; config/Parameters rows Admin-only. Retention default 365 days on Parameters; purge by created_at via scheduled job.

#### FR-33: HTTPS and Public base URL

App UI, unsubscribe, open pixel, rewritten image URLs, and List-Unsubscribe use HTTPS only (no plain HTTP public links). Public base URL is the Cloudflare (or equivalent) public HTTPS hostname. Origin TLS operator choice: Cloudflare Full or Full (strict) only; Flexible is not an intended supported mode.

#### FR-34: User bootstrap and auth

Install produces an initial Admin. Admins can create/disable Users and assign role plus List manage/send grants. V1 authentication is local username/password stored by the app (no SSO required). `[ASSUMPTION: password policy and lockout follow common defaults; exact policy set in architecture.]`

**UI:** Sidebar **Users & grants** lists Users. Clicking a User name or Edit opens a User detail page (not a sidebar item) to edit settings (username, identity fields, role, status, password reset) and per-List manage/send grants. Admin role does not require list grants (full access).

**Consequences (testable):**
- A disabled User cannot sign in.
- Only Admin can create Users.
- User detail is reachable from the Users & grants index, not as its own nav entry.

## 5. Non-Goals (Explicit)

- Not a phpList port or PHP compatibility layer.
- No POP3 for mail-to-send or bounce ingest.
- No click tracking in V1.
- No reply forwarding / processing of non-bounce replies beyond discard.
- No external spam service (V2) or AI spam scoring (V3) in V1.
- No plugin implementations in V1 (architecture may name seams only).
- No public subscribe / preference center / template library in V1 (confirmed). No List public/private visibility flag in V1. **Deferred intent (post-V1, not requirements yet):** each List is public or private — **private:** only Admins may add Members (List managers may not); Members may still unsubscribe; **public:** a public subscribe page is available. Until then, operator add + Member unsubscribe behave per FR-9 / FR-12 / FR-35.
- No Cloudflare Flexible as supported origin mode.
- No automatic re-enable from opens.
- No unilateral clear of unsubscribe.
- Uncorrelated bounces never auto-disable.

## 6. MVP Scope

### 6.1 In Scope

- Roles: Admin, List manager, List sender
- Catalogs: Sending paths, Bounce mailboxes, Footers, Notice sending path, Parameters, Attention; referenced configs blocked from delete while in use (Used by on detail)
- Lists, Members index (searchable) + Member page, bulk paste add on List page, global disable, per-list unsubscribe + consent clear
- Campaigns (UI + mail-to-send), dedupe, queue, system throttle, compose test send
- HTML/text/images/attachments with Image-fetch modes, payload + stored-image limits, TTL
- Bounce poll + daily bounce-process with hard/soft/holdback/VERP optional
- Mail-to-send IMAP auth/spam/From-issue/hold/Drop log/auto-block
- Ops Logs (send / bounce poll / mail-to-send poll); errors on Admin Attention
- Audit log + Member timeline views; open pixel tracking
- Single Docker deploy; HTTPS Public base URL; Full / Full Strict origin TLS

### 6.2 Out of Scope for MVP

- Spam service integrations (V2) / AI spam (V3)
- Click tracking; reply desk
- Plugin marketplace / implementations
- Public subscribe / preference center / template library
- List **public / private** visibility and the post-V1 rules: private → Admin-only member add (Members may unsubscribe); public → public subscribe page
- Per-list or per-path throttle overrides
- Hourly auto-block cap

## 7. Success Metrics

**Primary**

- **SM-1**: Operator can complete UJ-3 (compose → queue → finish) and UJ-4 (authorized mail-to-send → Campaign) on a fresh install without undocumented steps. Validates FR-16–FR-19, FR-28–FR-30.
- **SM-2**: Zero auto-disables attributable to uncorrelated bounces in acceptance tests / staging. Validates FR-25.
- **SM-3**: Unsubscribe and clear-unsubscribe consent paths are HTTPS-only and audited end-to-end. Validates FR-12, FR-14, FR-32, FR-33.

**Secondary**

- **SM-4**: Attention surfaces uncorrelated bounces, From-issues, holds, and Mandatory failures with correct Admin vs List manager scoping. Validates FR-8.
- **SM-5**: Soft and hard limits behave independently after soft is customized (changing hard does not rewrite soft). Validates FR-26.

**Counter-metrics (do not optimize)**

- **SM-C1**: Maximize open rate via dark patterns or non-pixel tracking — counterbalances SM-3/SM-4 reporting honesty.
- **SM-C2**: Maximize mail-to-send throughput by weakening auth/silent-drop rules — counterbalances SM-1 security defaults.
- **SM-C3**: Minimize Attention noise by auto-resolving uncorrelated bounces into disable — forbidden by FR-25.

## 8. Open Questions

1. Origin certificate supply/docs for Full vs Full Strict inside the container (ops) — non-blocking for FR wording; needed before production runbooks.
2. Which additional standard headers beyond Message-ID to parse in DSNs — architecture detail; product requires Message-ID at minimum (FR-25).
3. Exact local-password policy / lockout thresholds (architecture; FR-34 assumes common defaults).

## 9. Assumptions Index

- Inline assumption from §4.6 FR-31 — mail-to-send inboxes are not public / hard to guess.
- Inline assumption from §4.7 FR-34 — local username/password auth in V1; no SSO required.
- Inline assumption from §4.4 FR-39 — Ops Log detail retention default 90 days (Parameters later).
- Inline assumption from §4.1 FR-5 — unsubscribe link always injected even when Footer catalog empty.
- Inline assumption from §10 — WCAG 2.1 AA target for operator UI (confirm in UX).
- `[ASSUMPTION: competitive landscape is Listmonk/Keila/Mautic/phpList; product differentiates on phpList-like operator paths + mail-to-send/bounce rigor on a non-PHP stack — not on matching Listmonk throughput claims.]`
- `[ASSUMPTION: single primary language UI for V1; i18n not required for MVP.]`
- `[ASSUMPTION: stakes = production-ready internal/self-hosted operator tool — rigor of an internal launch PRD, not hobby.]`

## 10. Cross-Cutting NFRs

- **Security:** SSRF guards on image fetch; signed tokens for pixel/unsubscribe/consent; no raw secrets in Audit log or Parameters; silent drop vs backscatter rules for mail-to-send; DMARC fail = reject treatment.
- **Privacy / data governance:** Audit and bounce log TTLs; campaign media TTL; Member timeline is filtered Audit view.
- **Reliability / ops:** Separate poll vs bounce-process vs send-queue schedules; holdback to tolerate late DSNs; Attention for human-only cases.
- **Deployment:** Single Docker container; HTTPS everywhere public; origin Full or Full Strict only.
- **Accessibility:** `[ASSUMPTION: WCAG 2.1 AA target for operator UI — confirm in UX.]`

## 11. Constraints and Guardrails

- **Safety:** Notice channel may email disabled/unsubscribed only for consent/admin notices; Campaign queue never does.
- **Cost / capacity:** Payload and stored-image hard uppers; throttle caps; bounce/audit retention purge jobs.
- **Privacy:** HTTPS-only member-facing links; no mailto List-Unsubscribe in V1.

## 12. Platform

- Operator UI: web (React). Backend API + schedulers. Single-container deploy. Member surfaces: unsubscribe/consent HTTPS pages and open pixel (not a full member portal).
