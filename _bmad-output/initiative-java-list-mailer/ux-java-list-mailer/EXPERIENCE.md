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
| Campaigns | Yes | Sidebar | Campaign index |
| Campaign detail | No | Campaigns row | Status, stats, deep links |
| Compose | Yes | Sidebar | Create/edit campaign content + preview + test send |
| Lists | Yes | Sidebar | List index |
| List detail | No | Lists / deep links | Settings, bulk add members |
| Members | Yes | Sidebar | Searchable member index |
| Member detail | No | Members / deep links | Status, lists, timeline |
| Mail-to-send | Yes | Sidebar | Inboxes index + mappings overview |
| MTS inbox detail | No | MTS inbox click | Settings, senders, mappings |
| Bounce mailboxes | Yes | Sidebar | Mailbox index |
| Bounce mailbox detail | No | Index row | IMAP / poll / credentials |
| Sending paths | Yes | Sidebar | Path index |
| Sending path detail | No | Index row | SMTP/From, Used By |
| Footers | Yes | Sidebar | Footer index + detail via row |
| Parameters | Yes (Admin) | Sidebar | System tunables — stacked fields + descriptions |
| Users | Yes (Admin) | Sidebar | User index |
| User detail | No | Users row | Profile, roles, list grants, **Security** (MFA) |
| Ops Logs | Yes | Sidebar | Send / bounce poll / MTS poll logs |
| Audit | As specified in mock/IA | Sidebar or Admin group | Append-only audit (scoped) |

**Catalogs:** no delete while referenced; detail shows navigable **Used By**.

→ Composition reference: `.working/clickable-mockup/index.html` and `mockups/clickable/index.html` (Clearwater + full page map). Theme swatch: `mockups/theme-clearwater.html`. Spines win on conflict.

## Voice and Tone

| Do | Don't |
|---|---|
| “Sign in with your email” | “Enter username” |
| “Authenticator code” / “Backup code” | “Enter OTP from SMS” |
| “This list is used by 3 campaigns — remove those first” | Silent failed delete |
| “Test send ignored bounce-disable for these addresses” | Hide test-send rules |
| Short operator English; Title Case labels | Cute marketing microcopy on admin chrome |

## Component Patterns

| Pattern | Behavior |
|---|---|
| Sidebar nav | Title Case items; UPPERCASE categories; active = `{colors.accent-subtle}` treatment |
| Index → detail | Row click / Edit opens detail; detail not in sidebar |
| Compose preview | HTML / Text tabs; live sync from editors |
| Test send | Paste addresses; uses first list path; ignores bounce-disable; failures → Ops Logs / Admin Attention |
| Used By | Navigable links; blocks delete while in use |
| Parameter field | Label → control → helper (purpose, supported values, range) always visible |
| Attention error rows | Visually highlighted for Admin ops failures |
| MFA Security panel | On User detail only: enroll QR, verify, backup codes, unenroll |

## State Patterns

| State | Treatment |
|---|---|
| Loading indexes | Skeleton or quiet spinner in card — no blank flash |
| Empty index | One sentence + primary create action |
| Delete blocked | Explain Used By; do not navigate away |
| MFA mandatory, not enrolled | After password, force enroll or block with path to Security (Admin configuring self must complete enroll) |
| MFA enrolled | After password, TOTP (or backup code) step on same login card flow |
| Validation error | Field-level message; `{colors.danger}` |
| Ops failure | Ops Log entry + Attention highlight for Admin |

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
3. Jordan opens **Users → (self) → Security** later to rotate backup codes — not a separate MFA app section in the sidebar.
4. **Climax:** lands on Attention/Campaigns per role with session in sessionStorage.

### Flow 3 — Compose and test send (Sam, List sender)

1. Sam opens **Compose**, writes HTML/text; preview tabs stay in sync.
2. Runs **Test send** to pasted addresses; bounce-disable ignored for test.
3. Failure appears in Ops Logs and, for Admin, Attention highlight.
4. **Climax:** successful test preview confidence before queue send.

### Flow 4 — Detail-not-nav discipline (Ava)

1. Ava opens **Lists**, clicks a row → List detail (bulk add).
2. From a membership table she deep-links to **Member detail** — Members index stays the sidebar entry.
3. **Climax:** she never expects List/Member/Campaign detail as top-level nav items.
