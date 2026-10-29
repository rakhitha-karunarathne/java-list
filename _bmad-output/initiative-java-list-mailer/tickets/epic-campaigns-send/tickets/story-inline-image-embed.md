---
id: 15
type: story
title: "Inline CID embed for fetched campaign images"
parent: epic-campaigns-send
covers: [R5, R6, R17]
after: [5, 7]
hitl: false
risk: medium
refined: true
---

# Inline CID embed for fetched campaign images

## Description

Today, remote `<img src="https://…">` URLs are fetched **at Campaign queue** (Image-fetch Best Effort / Mandatory), stored as campaign blobs, and rewritten to public `/dl/<campaignId>/…` links at send. Inbound MTS messages that already use MIME CID inline images are a separate path.

**Embed images in message** (on/off) controls whether **fetched/stored remote** images are sent as CID inline parts instead of `/dl` links, subject to Parameter size limits (values in **KB**).

| Control | Who | Default |
| --- | --- | --- |
| Campaign flag | List Manager & List Sender (compose / campaign edit) | **On** |
| MTS inbox flag | Admin only | Copied onto new MTS Campaigns at create (inbox default **On** to match compose) |
| Size limits | Admin via Parameters only (not on Campaign/inbox) | Per image **256 KB**; total CID budget **2048 KB** (2 MB) |

UI label everywhere: **Embed images in message**. Help text must explain in plain language that images are included inside the email (recipients do not load them from this server’s `/dl` URLs), that only images at or under the per-image Parameter limit are embedded, that the total of embedded images cannot exceed the total Parameter limit, and that larger images still use hosted `/dl` links. Show the **current Parameter values** (KB) in that help text.

## Requirements

1. **Parameters (KB)**
   **Given** seeded Parameters
   **When** defaults apply
   **Then** `campaign.image.inline.max.kb` = `256` (per-image ceiling for CID eligibility)
   **And** `campaign.image.inline.total.max.kb` = `2048` (max sum of CID image bytes per outbound message)
   **And** both are Admin-editable like other Parameters; stored/compared as KB integers (× 1024 → bytes at send)
   **And** they are separate from `campaign.image.max.bytes` / `campaign.payload.max.bytes`

2. **Campaign flag (compose default on)**
   **Given** List Manager or List Sender on Compose (or campaign edit where content is editable)
   **When** they toggle **Embed images in message**
   **Then** the flag is persisted on the Campaign
   **And** new manually composed Campaigns default to **on**
   **And** Admin may also set it where they have compose access; size limits are never edited on the Campaign

3. **MTS inbox flag → copy once**
   **Given** Admin edits an MTS inbox **Embed images in message** toggle (default **on**)
   **When** MTS creates a Campaign from that inbox
   **Then** the Campaign flag is set to the inbox value at create time
   **And** later inbox changes do not alter existing Campaigns
   **And** List Manager / List Sender do not edit this on the inbox (Admin only); they may still change the Campaign flag after create if the campaign UI allows content settings

4. **Already-inline MTS images unchanged**
   **Given** an authorized MTS message already contains CID / `multipart/related` inline images
   **When** the Campaign is created and later sent
   **Then** those inline parts stay inline (CID) end-to-end
   **And** this story must **not** rewrite those inbound CID images to `/dl` or force them through the remote-fetch embed rules
   **And** the Campaign embed flag applies to **remote URL images fetched into blobs**, not to stripping or converting original inbound CID

5. **Send — flag on (fetched blobs)**
   **Given** Campaign embed is on and HTML references campaign image blobs from remote fetch (not original inbound CID)
   **When** building a recipient message (including **test send**)
   **Then** each such image with size ≤ per-image max KB is a candidate for CID
   **And** candidates are embedded in arrival/HTML order until adding the next would exceed total max KB; remaining candidates use `/dl`
   **And** any single image over per-image max KB always uses `/dl`
   **And** mixed CID + `/dl` in one message is allowed
   **And** MIME uses `multipart/related` (and `multipart/mixed` when file attachments exist) with proper `Content-ID` / `cid:` src

6. **Send — flag off**
   **Given** Campaign embed is off
   **When** sending or test-sending
   **Then** fetched remote blobs use `/dl` as today
   **And** inbound-original CID images still remain CID (requirement 4)

7. **Queue still owns remote fetch**
   **Given** Image-fetch Best Effort or Mandatory
   **When** the Campaign is queued
   **Then** remote URLs are fetched/stored as today
   **And** embed does not re-fetch at per-recipient send time

8. **Existing payload ceilings**
   **Given** overall `campaign.payload.max.bytes` (and list overrides)
   **When** queue or send would exceed that hard ceiling
   **Then** fail with a clear error as today
   **And** the inline KB parameters only choose CID vs `/dl`; they do not raise the hard payload ceiling

9. **Automated tests (required)**
   **Given** this feature ships
   **When** `mvn test` runs
   **Then** coverage includes at least:
   - **Flag off** — fetched blob images appear as `/dl` URLs in outbound MIME/HTML (no CID for those blobs)
   - **Flag on, under per-image KB** — image becomes `cid:` + related part; byte size ≤ `campaign.image.inline.max.kb`
   - **Flag on, over per-image KB** — that image stays `/dl` even when flag is on
   - **Flag on, total KB exceeded** — first images that fit are CID; remaining demoted to `/dl` (mixed message OK)
   - **Test send** — same embed/`/dl` rules as production send for the campaign flag
   - **MTS copy** — inbox embed flag is copied onto the created Campaign; changing inbox afterward does not change that Campaign row
   - **Inbound CID preserved** — MTS message with existing inline CID images still has those CIDs after create/send (not rewritten to `/dl`)
   - **Defaults** — new compose Campaign embed default **on**; Parameter seeds **256** / **2048** KB
   Prefer focused unit/integration tests (GreenMail and/or MIME assertions) over full UI E2E unless already cheap in-suite.

## Boundaries

- In: two Parameter keys (KB); `campaigns.embed_images`; `mts_inboxes.embed_images`; compose + campaign UI toggle + help; MTS inbox Admin toggle + help; MTS create copy; preserve inbound CID; SMTP related/CID path; test-send parity; **automated tests in req 9**.
- Out: Changing Image-fetch Off/Best Effort/Mandatory; per-list embed override; editing size limits on Campaign or inbox.

## References

- parent — epic-campaigns-send
- related — story-image-fetch-modes-ssrf-and-payload-ceilings; story-blob-storage-and-public-dl; MimeBodyParser CID handling; epic-mail-to-send
- code — `RemoteImageFetchService`, `CampaignHtmlRewriter`, `CampaignSendService`, `JavaMailSmtpAdapter`, `MtsCampaignFactory`, `MtsInboxEntity`, Parameters

## Notes

- Decision: 2026-10-29 — User answers: label + help with Parameter KB values; sizes in KB (256 / 2048); compose default on; MTS Admin toggle copied to campaign; LM/Sender toggle campaign; test send honors; inbound CID unchanged; mixed OK; total budget demotes excess to `/dl`; **automated tests required** (req 9).
- Reconcile: earlier “not editable on campaign” applied to **size limits** (Parameters only). Campaign **flag** is toggled by List Manager / List Sender; MTS inbox flag by Admin.
- Decision: Campaign embed flag editable only while **Draft** (same as other compose fields).

## Dev Agent Record

### Completion Notes

- Flyway **V42**: `campaigns.embed_images`, `mts_inboxes.embed_images` (default true), `campaign_attachments.content_id`.
- Parameters `campaign.image.inline.max.kb` (256) / `campaign.image.inline.total.max.kb` (2048).
- `InlineImagePlanner` + SMTP `multipart/related` for CID; MimeBodyParser keeps inbound `cid:`.
- Compose + MTS inbox UI toggles with help text showing KB limits; MTS create copies inbox flag.
- Tests: `InlineImagePlannerTest`, MimeBodyParser CID preserve, MTS copy, controller mocks updated.
