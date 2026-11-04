# List Mailer — user guide

Operator help for the web UI at http://127.0.0.1:8080 (or your deployed URL). For install and env vars, see the [README](../README.md) and [deploy notes](deploy.md).

## Sign in

1. Open the app URL.
2. Sign in with email and password.
3. If MFA is enabled for your user, enter the authenticator code when prompted.

**First install (empty database):** the bootstrap Admin is created automatically — default `admin@example.com` / `changeme` unless you set `MAILER_BOOTSTRAP_*`. Change that password after first login. **Receive Notification Emails** is off for that seeded Admin (dummy mailbox); turn it on under **My Account** or **Users & Grants** once you use a real Admin address.

Session is a JWT stored in the browser for this tab. **Sign out** clears it. Leaving a page with unsaved edits prompts to confirm.

## Roles

| Role | Typical use |
| --- | --- |
| **ADMIN** | Full sidebar: catalogs, MTS, bounces, parameters, users, ops/audit |
| **LIST_MANAGER** | Lists, members, campaigns, bounce messages, issues, audit (no catalog/MTS admin tools) |
| **LIST_SENDER** | Compose and campaigns for granted lists; starts on Compose |

Sidebar items you cannot use are hidden. Detail screens open from indexes (open a row) — they are not separate top-level nav destinations.

## Recommended first-time setup (Admin)

Do this once before sending mail:

1. **Catalogs → Sending Paths** — SMTP host, port, username/password, From name/email, TLS/SSL. Use **Test connection**. Optional Return-Path must share the From domain.
2. **Catalogs → Footers** — HTML/text footers reused on campaigns (optional).
3. **Catalogs → Notice Sending Path** — path used for system/notice mail if configured.
4. **Bounces → Bounce Mailboxes** — IMAP mailbox that receives bounces; test connection; enable polling as needed.
5. **Lists & Members → Lists** — create a list and assign a **sending path**.
6. **Lists & Members → Members** — create members, or open a list and **bulk-add** emails.
7. **System → Parameters** — set public base URL and pacing/retention as needed (must be `https://…` in real deploys).
8. **System → Users & Grants** — create managers/senders and grant list access.

Passwords for mailboxes and paths are stored encrypted (not shown again after save). MFA authenticator seeds are encrypted the same way.

## Lists and members

- **Lists** — create/edit list name and sending path; open a list for members, pause/resume, bulk import. On **list detail** (manage access), **Delete** removes the list after confirmation. Optionally check **also delete recipients who are not on any other list** — those orphans and their send/bounce/open history are removed; recipients still on another list are kept. Delete is blocked while a Queued, Sending, or Paused campaign includes the list.
- **Members** — global member directory (email, enabled). Membership and unsubscribe state live on each list.

Subscribers who unsubscribe via the link in campaign mail are marked unsubscribed on the campaign’s lists (other lists unchanged unless they were included).

## Campaigns

### Compose

**Campaigns → Compose** (or edit a draft from the campaign detail):

1. Subject, HTML and/or text body.
2. **Embed Images In Message** (default on) — when remote images are fetched at queue, include them inside the email (CID) so recipients do not load them from this server’s hosted `/dl` links. Only images at or under the per-image Parameter limit are embedded; total embedded size cannot exceed the total Parameter limit (defaults **256 KB** each / **2048 KB** total under **System → Parameters**). Larger images still use `/dl`. Editable on Draft only.
3. Choose one or more **lists** (first list’s sending path is used unless you override where the UI allows).
4. Optional attachments, sending-path override, test send to yourself (test send follows the same embed rules).
5. **Queue** — saves and enqueues sends (status becomes Queued / Sending).

Duplicate addresses across selected lists are sent **once** per campaign.

### Campaign detail

From **Campaigns**, open a row:

- Stats (sent, opens) and recipient rows with status search.
- **Stop** / **Pause** / **Resume** / **Requeue** depending on status.
- **Add list** after Finished, then Requeue — only new addresses are queued.
- **Retry failed** for failed recipients when available.
- Open pixel counts unique/total opens when tracking links are present in HTML.

## Mail-to-Send (MTS) — Admin

Turn email-to-list into queued campaigns:

1. **Mail-to-Send → Inboxes** — IMAP inbox; poll interval; **Embed Images In Message** (default on, copied onto each new campaign from this inbox); test connection. Later inbox changes do not alter existing campaigns.
2. Open the inbox → add **authorized senders** and **list grants** (which lists that sender may target). Optional per-sender substitute From / sending path.
3. Poll (scheduled or **Poll now**). Authorized mail becomes a **Queued** campaign; strangers are dropped (see Drop Log), not rejected with a courtesy bounce.
4. Images that already arrived **inline** in the received mail stay inline. Remote URL images follow the campaign embed flag and Parameter KB limits (same as Compose).
5. **Drop Log** — review/resolve drops. **Global Blocks** — block sender or domain. **Issues** — From-domain / related holds. **Mail-To-Send Processing Log** — poll processing history.

## Bounces

1. **Bounce Mailboxes** — configure IMAP; poll ingest bounce messages.
2. **Bounce Messages** — inspect correlated events.
3. Run bounce **process** when available (or rely on schedule) — hard/soft handling disables or soft-marks members as designed.
4. **Bounce Processing Log** — what the process step did.

Idle process may skip work when nothing new has been correlated since the last run.

## Catalogs (Admin)

| Screen | Purpose |
| --- | --- |
| Sending Paths | Outbound SMTP used by lists/campaigns |
| Footers | Shared campaign footers |
| Notice Sending Path | Path for notice/system mail |

Delete is blocked while a catalog row is still referenced (UI shows **Used by**).

## System (Admin)

- **Parameters** — runtime settings (retention, pacing, public URL, feature toggles, campaign image inline max/total KB). Not for JWT or mailbox passwords.
- **Secrets** (on the Parameters page) — shows current encryption key version and outdated count; **Migrate secrets to latest key** rewraps rows after you add a higher key version in config.
- **Users & Grants** — create users, roles, list grants; MFA setup flows where offered.
- **Ops Logs** / **Queue Sending Log** / **Audit Log** — operational and security-relevant history (passwords are not written to audit).

## My Account

Every role: change password (where enabled), MFA enrollment, **display time zone** for timestamps in the UI, and **Receive Notification Emails** (whether this account may get Admin ops notices on the Notice Sending Path).

**Enroll MFA:** tap **Enroll MFA**, scan the **QR code** with an authenticator app (Google Authenticator, Microsoft Authenticator, etc.), then enter the 6-digit code to confirm. If you cannot scan, enter the shown secret key manually. Backup codes appear once after confirm — store them safely.

## Admin notices (Notice Sending Path)

When **Catalogs → Notice Sending Path** is set, enabled **Admin** users with **Receive Notification Emails** on also receive:

| Subject | When |
| --- | --- |
| **Queue Processed** | A send-queue run had pending work — start/finish times, sent and errored counts per campaign, messages/hour |
| **Bounces Processed** | Bounce process disabled one or more members — lists those emails |
| **Bounce(s) Received** | Bounce fetch ingested mail — counts per campaign (plus uncorrelated) |
| **Campaign Queued: …** | Campaign becomes Queued — Admins get a copy of the campaign body |
| Confirm re-subscribe / MTS wrong-inbox / From-domain deleted | Existing consent and MTS notices (to member or sender) |

If the Notice path is unset, notices are skipped (logged) and processing continues.

## Attention

**Dashboard → Attention** (not List Sender): open admin/manager attention items from ops failures and similar highlights. Resolve or act from the linked context.

## What subscribers see

- Campaign HTML may include an open pixel and an **unsubscribe** link (opaque token).
- Unsubscribe is HTTPS-only in production; success and already-unsubscribed/expired paths show a simple confirmation page.
- List-Unsubscribe headers are set on outbound mail when configured.

Operators do not manage those pages in the sidebar; they are public endpoints.

## Tips

- Prefer **Test connection** on SMTP/IMAP before relying on polls or queues.
- After adding a new `MAILER_SECRETS_KEY_n` in Compose/env, use **Migrate secrets** (or `MAILER_SECRETS_MIGRATE_ON_STARTUP=true` once) before removing old `MAILER_SECRETS_KEY_*` versions.
- If send stays Queued, check Ops Logs, sending path credentials, and that the list has a valid path.
- LIST_SENDER only sees lists they are granted — ask an Admin for grants.
- Form fields show a short help line under the label; Parameter screens also show each setting’s description there.
- To prefer hosted image links over CID embedding, turn **Embed Images In Message** off on the Draft campaign (or on the MTS inbox before new mail arrives).
