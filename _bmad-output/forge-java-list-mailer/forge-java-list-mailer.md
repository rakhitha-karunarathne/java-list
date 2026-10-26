---
title: Java list mailer — hardened decisions (draft)
status: draft
created: 2026-10-23
updated: 2026-10-23
source: party-mode session (installed product team)
---


# Java list mailer — hardened decisions

Not a phpList port. New product with phpList-like operator behaviors and interaction patterns so teams can leave PHP. First release = **V1**.

## Stack (stated)

- Backend: Java, Spring Boot (latest), Spring schedules for periodic work
- Frontend: React
- Database: MariaDB
- Delivery: single Docker container
- Plugin architecture: required later; **hook list deferred to architecture** (not further product-grilled here)
- No integrations beyond what is listed here (V1)
- **HTTPS everywhere** for app UI, unsubscribe, open pixel, rewritten image URLs, List-Unsubscribe — no plain HTTP public links
- **Public base URL** = Cloudflare (or equivalent) public HTTPS hostname used in pixels, unsubscribe, and rewritten images
- **Origin TLS (operator choice):** Cloudflare **Full** (HTTPS to origin; self-signed OK) **or** **Full (strict)** (Cloudflare Origin CA / trusted cert on the container). **Flexible** is not supported as an intended mode.

## Roles (login users)

1. **Admin** — full access
2. **List manager** — manage specific list(s)
3. **List sender** — send to specific list(s)

Terminology: login accounts = users. People on lists = **members** (phpList “users”). Do not overload the word.

## Core operator paths (in for V1)

- Campaign path (compose → choose lists → queue → process → finish), same interaction idea as phpList
- List management and list membership
- Bounce processing (disable members when bounce limit exceeded; global member status)
- Mail-to-send (IMAP ingest → campaign)

## Catalog pages (shared records; lists/senders refer, do not embed)

| Catalog | Notes |
|--------|--------|
| Sending paths | SMTP connection + **From** (full email). Lists map to one path. Many lists may share one path. |
| Bounce mailboxes | Polled for bounces; not part of list config. Many mailboxes. **Poll interval configurable per mailbox** (scheduled task). **Default: 15 minutes.** |
| Footers | Own page. List points at one. Mail-to-send mapping can point at one. |
| Notice sending path | One UI-configured path for rejections, issue notices, admin notices / digests, **consent (accept/reject) emails**, and other admin/notification mail. **Bypasses bounce-disable.** May email an **unsubscribed** address **only** for consent/admin notices. **Campaign queue** still honors disable and never sends to unsubscribed list memberships. |
| **Parameters** | Single **admin-only** page + entity for **all** system-wide UI-tunable non-credential settings: bounce limits, VERP, schedules (incl. bounce-process **02:00** + timezone default **UTC**), send pacing, image fetch timeout default **30s**, payload/image hard uppers, campaign TTL default/max, bounce log TTLs, audit retention, image-fetch mode, consent expiry, auto-block default **10/day**, hard bounce limit **3**, soft default **2× hard**, holdback multiplier **2**, etc. **Credentials** stay on sending paths / mailboxes only. **Signing secrets** for pixel / unsubscribe / consent tokens live in **env / secret file**, never Parameters plaintext. |
| **Attention dashboard** | Inbox of items needing human action. **V1 catalog:** uncorrelated bounces; From-domain / sender issues; mail-to-send holds; Mandatory image / payload failures. Not a second store — links into bounce log, issues, etc. **Admin** sees all. **List managers** see items for **their lists / members** only. System-wide items admin-only. |

## Lists

- Name, description, members
- Create / delete / edit description / add-remove members
- Grant manage and send (admin or list managers)
- Maps to **one sending path**
- Optional spam threshold (empty = no list override)
- Optional footer reference

## Recipients (members) — global status

- **Enabled** — may receive mail from lists they belong to (and are not unsubscribed on)
- **Disabled** — **no further sends** to that address globally (queue skips them). Show **`disable_reason`**: which limit hit (hard vs soft) + latest bounce error. Bounce disable is separate from unsubscribe.
- **No global “blocked” from unsubscribe.** Unsubscribe is **per list** (membership / list-sub status).

**Re-enable (bounce-disabled):** **manual**, or **re-add to a list** (with **confirm**) which re-enables and clears streak state — **unless** they are **unsubscribed from that list**, in which case re-add is **ignored**. **No** automatic re-enable from open/view hits.

**Unsubscribe (V1):** every outbound campaign email includes:
- Visible **unsubscribe link** in the **footer** (**HTTPS only**)
- **List-Unsubscribe** / **List-Unsubscribe-Post** for email apps — **HTTPS only** (no `mailto:`). One-click Post in V1.

Unsubscribe is **per list**. From a **multi-list campaign**, unsubscribe removes them from **all lists included in that campaign**. They may remain on other lists not in that campaign.

**Unsubscribe list UI:** admins see all; list managers see for **their lists**. Same views on the **member page**.

**Clearing unsubscribe (admin or list manager):** not unilateral. Request clear → email member via the **admin/notification (notice) channel** with **accept** (clear unsubscribe / re-subscribe) or **reject** (remain unsubscribed). That channel **bypasses disable** (and may reach an unsubscribed address for this notice only). Until accept, stay unsubscribed; re-add stays ignored. Pending request **expires** after **14 days** default (Parameters); then treat as no change (still unsubscribed). Record **invite expired** on the **member interaction log** (and audit). Audit request, response, and expiry.

**Audit log (single append-only store):** membership / unsubscribe / clear-unsubscribe / bounce disable / enable, campaign send / open / bounce outcomes, consent invite lifecycle (including **invite expired**), notice emails, related member–list actions, and **V1 config changes** — **Parameters** and similar settings (sending paths, bounce/mail-to-send mailboxes, footers, etc.): who, when, what changed. **Do not write raw secrets** (passwords/tokens) into the audit log — record that credentials were set/rotated only. Member self-unsubscribe audited (actor = member / system). **Member page timeline** and system-wide audit UI are **views** of this store.

**Audit visibility:** **Admin** — all entries (including Parameters / sending-path / mailbox config). **List manager** — **member-scoped** entries only (members on lists they manage). Config/Parameters audit rows are **admin-only**.

**Audit retention:** default **365 days**, configurable on **Parameters** (UI). Older entries purged by a scheduled job; purge clock = event **`created_at`**.

**Open / view tracking (V1):** **HTML tracking pixel only**. Text-only parts get **no** open signal. Used on member history and campaign reporting. Pixel, unsubscribe, and consent links use **signed tokens** (HMAC or equivalent). Signing secret in **env / secret file** (key rotation via key id preferred).

**Member page** (phpList-like “user” page):
- **Lists they are on** (active membership)
- **Lists they have unsubscribed from**
- **Admin** / **list managers** (managers: their lists only) can change membership. **Clearing unsubscribe** (admin or list manager) always sends the member an **accept/reject** email — **no unilateral clear** (audited)
- Global status (enabled/disabled + `disable_reason`)
- **Member timeline** = **audit log filtered by this member** (one store, not a second log). Event types include at least: campaign **sent**, **open**, **bounce** (+ reason), **disable** / **enable**, **unsubscribe**, clear-unsubscribe **invite sent** / **accepted** / **rejected** / **invite expired**, and other notice/admin emails to this member.

**Campaign detail / member list:** list of members the campaign was **sent to**, each with **first open time** and **open count** for that member, plus campaign **open stats**:
- **Unique opens** (first open per member) — **headline open rate** = unique opens / sent
- **Total opens** — every open hit counted (can be > unique)
- Also show sent count

**Bounce list UI:** each row shows the **error extracted** from that bounce (plus correlated/uncorrelated, etc.).

Bounce tracking is **global per email address**. A bounce must identify the **email** and the **campaign** (not the source list). Non-bounce replies in bounce mailboxes: **discard** (reply forwarding = future, not V1).

**Correlation:** match bounces using (store on the per-recipient queue row at send):

1. **VERP / Return-Path** when enabled
2. Custom header **`X-JavaList-Token`** (opaque id for the queue row)
3. **Standard email headers** that help correlation — at least **Message-ID** we generate; also use other present standard headers when useful (e.g. fragments of the original in the DSN)
4. **Subject** + **recipient email** (weakest fallback)

**Match order when VERP is on:** VERP → `X-JavaList-Token` → Message-ID / other standard headers → subject+email.  
**When VERP is off:** `X-JavaList-Token` → Message-ID / other standard headers → subject+email.  
**Uncorrelated / unmatched bounces:** **never** count toward automatic disable, **never** soft-link into the consecutive walk, and **never** count as a “no bounce” send for streak reset. Still **log** them (full content and headers) and surface them on the **Attention dashboard**. **Visibility:** **Admin** — all bounces system-wide. **List manager** — bounces whose recipient email is a **member of a list they manage**. Both can act manually within their authority.

**Bounce log TTLs** (system-wide, **Parameters** page; both delete full content and headers):

- **Processed** bounces: default **180 days**, clock **processed_at**.
- **Unprocessed** bounces (including holdback / unmatched): default **365 days**, clock **received_at**.

**VERP:** optional. **System-wide enable/disable on the Parameters page** (UI). **Default: off.** Use only when the outbound mail server supports per-message Return-Path. Operators who cannot use VERP leave it off and rely on the other signals.

**Consecutive bounce counting** (global per email address). **Hard** and **soft** are **separate walks**, each with its own limit and holdback. Hitting **either** limit **disables** the member (same outcome).

- **Hard** limit default **3** (Parameters)
- **Soft** limit: initial default = **2 × hard** (hard 3 → soft **6**). After that soft is **independent** — changing hard does **not** auto-update soft; admins set soft manually to any allowed value.
- **Holdback multiplier** on Parameters, default **2**: do not mark the newest `(multiplier × that class’s limit)` messages processed. Hard and soft each apply this with **their** limit.

**Scheduling (three kinds):**

- Bounce **mailbox poll** and mail-to-send **inbox poll** — per mailbox, default **15 minutes** (ingest / correlate / log).
- **Bounce-process** task — system-wide on **Parameters**, **default once per day at 02:00** in the **Parameters timezone** (**default timezone: UTC**). Owns consecutive counting and auto-disable. Does **not** reset streaks on SMTP SEND OK.
- Send-queue processor — system-wide, default **15 minutes**, plus the send-pacing Parameters above.

**Bounce-process algorithm** (per email with unprocessed bounce activity):

1. Take sends outer-joined to bounce records; order by send date oldest → newest.
2. Walk oldest → newest; count consecutive bounced sends (hard and soft separately).
3. A send with **no correlated bounce** resets that class’s consecutive count (this is **not** “SEND OK”; it is “no bounce linked when the daily job runs,” with holdback protecting recent sends).
4. If count reaches the class limit → **disable** (soft same as hard) and mark bounces processed as below.
5. If limit **not** hit → mark what can be marked **processed** so old rows are not recounted forever; **holdback:** do **not** mark the newest **(holdback_multiplier × class limit)** messages (late bounces may still arrive). Wait until the next bounce-process run.
6. When disabled by hitting the limit: mark **all** bounces for that email processed.

**Payload oversize** (mail-to-send / compose): **does not** increment the auto-block reject counter. Mail-to-send also writes a **drop-log** row (see payload limit).

## Campaigns

- May target multiple lists
- Same address on more than one selected list → **one** queue row for that campaign (dedupe)
- That row uses the **first selected list’s sending path** (and thus that path’s From, unless a mail-to-send substitute applies)
- UI-composed campaigns have no substitute From → path From only
- Support text and HTML, images, attachments
- **Send queue** processed by a system-wide scheduled task; schedule interval on **Parameters**, **default 15 minutes**
- **Send pacing** (system-wide **Parameters** only; **no per-list/path overrides**; no per-minute throttle):
  - **Delay per email** — pause before each SMTP send after the first in a run. **Default: 1000 ms**
  - **Batch size** — maximum SMTP sends in one scheduler run. **Default: 600**
  - **Batch duration** — the run stops before the next send after this time, even if the batch is not full. A send already in flight may finish. **Default: 10 minutes**
  - **Messages per hour** — maximum messages sent in the last 60 minutes. A run sends only the room left in that hour, and never more than the batch size. When the hour is full, the run sends nothing. **Default: 3600**
  - **Enforced only by the send-queue processor.** Lists/campaigns **only enqueue**; they do not check pacing.

### HTML body, images, attachments (mail-to-send and outbound)

Applies when an inbox message becomes a campaign (and the same rules should hold for anything we re-send as HTML):

1. **HTML with embedded / inline images** (e.g. CID / multipart related) — preserve so images render in the body correctly on send.
2. **Real attachments** — keep and send as attachments (not inlined into the body).
3. **Remote URL images** — system-wide **3-state** switch (not per list). **Default: Best Effort.** Why fetch/rewrite: mixed third-party image domains can spook spam filters; bulk hotlinking others’ image hosts is unwanted.
   - **Off** — leave remote URLs as-is; no download, no rewrite, no stored-image usage for those URLs.
   - **Best Effort** (default) — download/store/rewrite when possible; on fetch fail keep the **original** URL and **still send**; no hold. If the **stored-image cap** is hit mid-way: rewrite what fits in **HTML document order** (first images win storage), leave the rest as **original** URLs, **still send**. Every soft-skip (fetch fail or cap) is **logged on the campaign detail only** (original URL + reason) — not the daily digest, not the mail-to-send drop log.
   - **Mandatory** — every remote image must be fetched and rewritten; on any fetch fail → **fail the whole** message, **notify sender** with the error; no hold. (No campaign.) If the stored-image cap blocks a required rewrite → same fail-whole + notify.
   - **UI compose (V1)** honors the same three modes and the same cap / fail rules before queue; soft-skips appear on **campaign detail only**.
   - **SSRF guards (V1, all fetch modes):** `http`/`https` only; block loopback / private / link-local (incl. cloud metadata); **re-check after DNS**; per-image **time** and **size** caps (size aligned with stored-image limits). **Fetch timeout default: 30 seconds per image** (Parameters). SSRF refusal / timeout counts as a fetch fail for Best Effort / Mandatory rules.
4. **Serving rewritten images** — long-lived cache headers; **Cloudflare** (or equivalent) in front.
5. **TTL** — campaign content and stored media expire after send.
   - Clock starts when the campaign **finished sending**.
   - **System default: 180 days** (configurable).
   - **Hard maximum** on **Parameters** (UI), default **365 days**. No list may set a TTL **above** this ceiling.
   - **Per list** TTL in days (at or below the hard max); empty → system default.
   - Multiple lists on the campaign → **longer TTL wins** (keep media for the list that needs it longest), still capped by the hard max. Opposite of payload/spam “strictest” rules.
   - Scheduled job deletes expired campaign content and related media.

### Email payload limit (SMTP bulk size)

- Name: **email payload limit** — keeps what we push through SMTP small enough for bulk send / downstream servers.
- **Includes:** HTML/text body + CID/inline related parts + file attachments.
- **Excludes:** rewritten URL images stored in our DB/CDN (clients load those via our links).
- **Hard upper** on **Parameters** (UI), default **5 MB**. No list may set a limit **above** this ceiling.
- **Per list** may set its own limit (at or below the hard upper). Empty list override → use the hard upper / system value.
- Multiple target lists → **strictest** effective limit; all-or-nothing.
- **Mail-to-send:** other checks pass but payload over limit → no campaign; **notify sender** (notice path) to reduce email size; **drop-log row** with reason; **counts toward auto-block = no**.
- **UI compose:** same **payload** gate before queue (block with clear error). Stored-image gate follows the image-fetch mode (Best Effort soft / Mandatory hard).

### Stored image limit (DB / CDN)

- Separate from email payload. Caps remote-URL image bytes we download and keep (**per campaign total** of rewritten images) so the DB does not fill and body images load fast via CDN.
- **Hard upper** on **Parameters** (UI), default **5 MB**. No list may set above this ceiling.
- **Per list** may set its own limit (at or below the hard upper). Multiple target lists → **strictest**; all-or-nothing.
- Over this limit: **Best Effort** — stop storing further images, keep originals for the rest, still send. **Mandatory** — fail whole + notify (same as fetch fail). **UI compose** under Mandatory — block with error before queue.

## Footers

- Catalog page; lists and mappings only reference them
- UI campaign: list footer only
- Mail-to-send: if the **sender+inbox+list** mapping references a footer, that wins over the list footer; else list footer; else none

## Mail-to-send

### Shape

- Inboxes: own page (IMAP only; **no POP3**)
- **Poll interval configurable per inbox** (scheduled task) — same idea as bounce mailboxes. **Default: 15 minutes.**
- Per inbox: authorized senders; inbox → list mappings
- Authorized sender is **per inbox**
- Substitute From is **per sender + inbox + list** (mapping row). Empty → sending path From
- Substitute domain must match the domain of that list’s sending-path From (domain = part after `@` on the path’s From, not SMTP hostname)
- Each accepted inbox message → **one campaign**; mapping lists = campaign lists; dedupe inside that campaign
- Two copies already in the inbox → two campaigns (nothing we can do)
- IMAP **CREATE** preferred for hold folders; org assumed to control the mail host

### From-domain issues

- Changing a sending-path From/domain is **allowed** (non-blocking save)
- Re-validate mappings; open items go to **Issues** page (not the drop log)
- From-issue on any target list → **all-or-nothing**: no partial campaign for any list
- Prefer: move issue-sender mail to **hold folder per sender per inbox**; notify sender; process folder into campaigns when issue clears; then remove folder
- If CREATE unavailable/fails → **delete-and-notify** (must resend after fix)
- Notices must state clearly: held (do not resend) vs deleted (must resend)
- One notice sending path for system mail

### Auth and spam (V1)

Prefer mail-server edge rejection; app also validates inbox mail.

**Order (authorized From on this inbox):**

1. SPF / DKIM / DMARC (DMARC fail always treated as **p=reject**, ignore published p=none/quarantine)
2. Spam (only if score available — see below)
3. From-issue / create campaign

**Outcomes:**

| Case | To sender | Drop log | Auto-block count |
|------|-----------|----------|------------------|
| Auth/DMARC/spam fail, From authorized on this inbox (spoof / bad score) | Silent delete | Yes (reason + counts?=no) | No |
| Auth pass, not approved on **any** inbox | Silent delete | Yes | Yes |
| Auth pass, not approved on **this** inbox, approved on **another** | Reject + **notify** (wrong mailbox) | Yes | No |
| Spam / auth fail paths for strangers | Silent to sender | Yes (as applicable) | Yes when validation fail counts |
| From-issue (config) | Hold or delete-and-notify texts | Issues page only | No |

**Spam V1:** read score **header if present**. No header / no provider → **skip spam** (not a fail). Operators who need scoring configure it on the mail server. **V2** = external spam services; **V3** = AI — both out of first release.

**Thresholds:** global, per mailbox, per authorized sender, per list. **Strictest** of those that apply wins. Multi-list mapping: if any target list’s threshold fails → reject **entire** message for all lists (no partial send).

### Drop log and blocks

- Drop log: per mailbox + **combined** view filterable by mailbox; mailbox config links pre-filtered
- Each row: **reason** + **whether it counted toward auto-block**
- Admin actions on rows: resolve, block sender, block domain, ignore
- **Blocks are global** (sender list + domain list in UI; admin can unblock)
- Auto-block: after N counted rejects — **Parameters** (UI); **sender only**, never auto domain. **Default: 10 per day** (strict; healthy installs should see near-zero rejects). Each installation may raise/lower. **No hourly cap** unless added later.
- **Operational assumption:** mail-to-send inbox addresses are **not public** — shared only with authorized people and **hard to guess**, so random drive-by reject storms are unlikely.
- From-issue events do **not** use this log or this counter
- Daily digest on notice path **only if new log items that day**; else no mail

### Unapproved / security

- No courtesy rejection to random unapproved strangers (backscatter risk)
- Exception: cross-inbox authorized notice (above)
- Blocked sender/domain checked after auth; silent drop

## Out of V1 (explicit)

- Spam detection service integration (V2)
- AI spam scoring (V3)
- Reply forwarding / processing non-bounce replies (beyond discard)
- Plugin implementations (architecture placeholder only until seams are named)
- Public subscribe / preference center / template library — not committed in party; confirm in PRD if needed
- Click tracking — out of V1 unless added later

## Still open (for PRD / next pass)

- Origin cert supply in container for Full vs Full Strict (ops/docs)
- Which extra standard headers beyond Message-ID to parse in DSNs (document in architecture)

## Next BMAD step

Turn this draft into a PRD (`bmad-prd`) or bind an initiative (`bmad`) then PRD. Party may continue for remaining opens or wrap.
