---
name: List Mailer
status: final
sources:
  - ../initiative-java-list-mailer.md
  - ../prd-java-list-mailer/prd-java-list-mailer.md
updated: 2026-10-25
---

# List Mailer — Experience Spine

## Foundation

**Form factor:** responsive web admin (desktop primary; mobile-friendly read/light edit).  
**Visual identity:** `{../DESIGN.md}` — Clearwater, light only, airy SaaS.  
**Product name in chrome:** List Mailer.  
**Stack context (not UX):** React SPA in Spring Boot jar; operator JWT in sessionStorage; login id is email.

Closest product analogy for *jobs*: phpList. Visual/IA chrome: modern SaaS, not phpList skin.

## Information Architecture

Sidebar shows **indexes and tools**, not entity detail pages. Hide a sidebar **category** when role hides all its items.

| Surface | In sidebar? | Reached from | Purpose |
|---|---|---|---|
| Sign in | — | Unauthenticated | Email + password; MFA step-up when required |
| Attention | Yes (role) | Sidebar | Human-action inbox; Admin sees ops errors highlighted |
| Campaigns | Yes | Sidebar | Campaign index; filter Draft · Queued · Sending · Finished · Stopped |
| Campaign detail | No | Campaigns row | Status badge, stats, Stop / Requeue / Add List, deep links |
| Compose | Yes | Sidebar | Create/edit Draft; Queue Send; Save Draft; Stop; preview + test send |
| Lists | Yes | Sidebar | List index |
| List detail | No | Lists / deep links | Page sections: settings through Save · members table · bulk add |
| Members | Yes | Sidebar | Searchable member index |
| Member detail | No | Members / deep links | Status, lists, timeline |
| Inboxes (Mail-to-send) | Yes | Sidebar | Inboxes index only (no global mappings overview); **Add Mail Box** registers an existing server mailbox for polling (does not provision IMAP) |
| MTS inbox detail | No | Inboxes row | Mail box account edit, authorized senders, list-grant overview |
| MTS authorized sender | No | Sender email on inbox detail | Grant Lists (table); Substitute From + Footer per List |
| Bounce mailboxes | Yes | Sidebar | Mailbox index; **Add Mail Box** registers an existing server mailbox for bounce polling (does not provision IMAP) |
| Bounce mailbox detail | No | Index row | IMAP / poll / credentials |
| Sending paths | Yes | Sidebar | Path index |
| Sending path detail | No | Index row | SMTP/From, Used By |
| Footers | Yes | Sidebar | Footer index + detail via row |
| Parameters | Yes (Admin) | Sidebar | System tunables — stacked fields + descriptions |
| Users | Yes (Admin) | Sidebar | User index; role and list grants only |
| User detail | No | Users row | Page sections: **Title** · **Account** (Role, Enabled, Save) · **List Grants** — no MFA |
| My Account | Yes (every role) | Header user id and bottom of sidebar | Self-service: **Password** then **MFA** |
| Ops Logs | Yes | Sidebar | Send / bounce poll / MTS poll logs |
| Audit | As specified in mock/IA | Sidebar or Admin group | Append-only audit (scoped) |

**Catalogs:** no delete while referenced; detail shows navigable **Used By**.

→ Composition reference (**mock approved 2026-10-26**): `mockups/clickable/index.html` (Clearwater + full page map; `.working/clickable-mockup/index.html` legacy). Theme swatch: `mockups/theme-clearwater.html`. Spines win on conflict.

## Voice and Tone

| Do | Don't |
|---|---|
| “Sign in with your email” | “Enter username” |
| “Authenticator code” / “Backup code” | “Enter OTP from SMS” |
| “This list is used by 3 campaigns — remove those first” | Silent failed delete |
| “Test send ignored bounce-disable for these addresses” | Hide test-send rules |
| Short operator English; Title Case labels | Cute marketing microcopy on admin chrome |
| Field labels = **valid English phrases** (spaced words; never CamelCase / glued keys like `fromEmail`) | Showing API property names as labels |

## Component Patterns

| Pattern | Behavior |
|---|---|
| Sidebar nav | Title Case items; UPPERCASE categories; active = `{colors.accent-subtle}` treatment |
| Index → detail | Row click / Edit opens detail; detail not in sidebar |
| Record index | **HTML table** (not a list of buttons); row click opens detail; optional action column for Stop/etc. |
| Create form | **Own page** (not on the index): index shows table + Create/Add action only; opens a dedicated page with back to index (not a second card under the table; not a sidebar item) |
| Add Mail Box | Mail-to-Send and Bounce: label is **Add Mail Box** (not Create) — mailbox already exists on the mail server; we only add it to the polling list |
| Test Connection | Sending Path (SMTP), MTS inbox, and Bounce mail box (IMAP): button attempts connect only; shows success or failure with error detail; does not save or send mail |
| Connection field order | Host then Port; Username then Password (then From / poll / TLS as applicable) |
| Compose preview | HTML / Text tabs; live sync from editors |
| Compose body editors | HTML Body and Text Body stack vertically (label above each field) — not side-by-side on one horizontal line |
| Compose lists | **HTML table** (Include / Name / Order / Reorder) — not a stack of checkbox rows; first included list sets the sending path |
| Compose sections | Content · **Attachments** (immediately under content) · Preview · Lists · **Testing** · Actions (Save Draft / Queue) |
| Test send | Paste addresses; uses first list path; ignores bounce-disable; failures → Ops Logs / Admin Attention |
| Campaign status badge | Exact label Draft / Queued / Sending / Finished / Stopped (text + color; never color alone) |
| Stop | Available on Draft, Queued, Sending (index row and detail); not on Finished/Stopped |
| Requeue | On Stopped always; on Finished only after Add List (control disabled until a new List is attached) |
| Used By | Navigable links; blocks delete while in use |
| Destructive delete | Always confirm first (cannot undo); Cancel leaves data; OK calls delete API. Covers catalog Delete, bounce/inbox Delete, remove sender, Unblock, MFA unenroll |
| Form field | **Stacked:** Title Case label **above** the control (never inline beside it); SPA `label.field` CSS enforces this |
| Boolean field | Scalar true/false (or on/off) settings use a **toggle** (switch + On/Off text), never a bare checkbox; multi-select / per-row permission checks may still use checkboxes |
| Field label wording | Valid English with spaces between words (`From Email`, `HTML Body`, `Use TLS`); SPA `fieldLabel()` — never raw camelCase keys |
| Parameter field | Label → **description (between label and control)** → **control + Save on one row**; boolean Parameters use a toggle in that row; on narrow widths the control shrinks until its min length, then Save wraps |
| Unsaved edits | Changed controls + their Save/Create button use `{colors.warning}` treatment (not accent alone); title/hint “Unsaved changes” so color is not the only cue |
| Attention error rows | Visually highlighted for Admin ops failures |
| MFA Security panel | On **My Account** only (every role): enroll, verify, backup codes, unenroll. Not on User detail |
| My Account | Two cards: **Password** (current, new, confirm) then **MFA**. Open from the header user id (email) or the **My Account** item pinned at the bottom of the sidebar |
| Page sections | Multi-job pages: stack `.card` panels in `.page-sections`. **Everything is a card** — including the record title when that page asks for it. Never leave page content on bare canvas. One job per card; break the white box between jobs. **Used By** is always its own card on catalog detail. Exemplars: User (Title · Account · List Grants); My Account (Password · MFA); Sending Path / Footer (Form · Used By); MTS inbox (Title · Account · Senders · List Grants); Compose (Content · Attachments · Preview · Lists · Testing · Actions); List detail (Settings through Save · Members table · Bulk Add) |

## State Patterns

| State | Treatment |
|---|---|
| Loading indexes | Skeleton or quiet spinner in card — no blank flash |
| Empty index | One sentence + primary create action |
| Delete blocked | Explain Used By; do not navigate away |
| Unsaved form | Dirty fields + Save/Create button warn-colored; leaving the page (sidebar, back, sign out, or closing the tab) asks confirm — do not discard silently |
| MFA mandatory, not enrolled | After password, force enroll or block with path to Security (Admin configuring self must complete enroll) |
| MFA enrolled | After password, TOTP (or backup code) step on same login card flow |
| Validation error | Field-level message; `{colors.danger}` |
| Ops failure | Ops Log entry + Attention highlight for Admin |
| Campaign Finished Requeue gated | Requeue disabled until operator adds ≥1 List; helper explains new-list-only, skip already-sent |
| Campaign Stopped | Show Requeue; explain unsent held until Requeue |

## Interaction Primitives

- Primary navigation: sidebar (drawer on small screens).
- Tables: click row to detail where applicable; horizontal scroll on narrow viewports.
- Dialogs: one level deep (confirm destructive, test send, MFA backup codes display once).
- Banned for V1: hover-only critical actions on touch; dark mode toggle; plugin marketplace UI.

## Accessibility Floor

- Target WCAG 2.2 AA contrast on Clearwater tokens (ink on canvas/surface; accent buttons with white label).
- Visible focus ring using `{colors.focus-ring}`.
- Keyboard: tab order follows reading order; Esc closes top dialog.
- MFA and login: inputs properly labelled; errors linked to fields.
- Do not convey status by color alone (badge text + color).

## Responsive & Platform

| Breakpoint | Behavior |
|---|---|
| Desktop | Sidebar visible; Compose can show editor + preview side-by-side when width allows |
| Narrow | Sidebar → drawer; tables scroll-x; stacked Parameter fields unchanged |

## Key Flows

### Flow 1 — Ava stands up Parameters (Admin)

1. Ava signs in with **email** + password (MFA step-up if enrolled or mandatory).
2. Opens **Parameters**.
3. Sees each tunable as stacked field with description (purpose, values, range) — e.g. ops log retention default 90; MFA mandatory default off.
4. Saves; confirmation is quiet and clear. **Climax:** catalogs/Parameters ready for lists.

### Flow 2 — Login with MFA step-up (Jordan, List manager)

1. Jordan enters email + password.
2. System challenges for authenticator code (enrolled).
3. Jordan opens **My Account** (header email or the bottom nav item) to change password or rotate MFA — not from Users & Grants (Admin only).
4. **Climax:** lands on Attention/Campaigns per role with session in sessionStorage.

### Flow 3 — Compose and test send (Sam, List sender)

1. Sam opens **Compose**, writes HTML/text; preview tabs stay in sync (status **Draft**).
2. Runs **Test send** to pasted addresses; bounce-disable ignored for test.
3. Failure appears in Ops Logs and, for Admin, Attention highlight.
4. **Climax:** successful test preview confidence before queue send.

### Flow 4 — Campaign Stop and Requeue (Sam)

1. Sam queues a Campaign (Draft → Queued → Sending). Mid-send he **Stops** → **Stopped**; unsent held.
2. He **Requeues** → Queued; remaining unsent continue.
3. After **Finished**, he **Add List…** then **Requeue**; only new-list addresses not already sent get mail.
4. **Climax:** status badges and actions match Draft · Queued · Sending · Finished · Stopped rules.

### Flow 5 — Detail-not-nav discipline (Ava)

1. Ava opens **Lists**, clicks a row → List detail (bulk add).
2. From a membership table she deep-links to **Member detail** — Members index stays the sidebar entry.
3. **Climax:** she never expects List/Member/Campaign detail as top-level nav items.
