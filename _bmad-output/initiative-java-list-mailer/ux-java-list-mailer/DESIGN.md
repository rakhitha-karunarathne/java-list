---
name: List Mailer
description: Clean modern light SaaS admin for self-hosted list mailer operators.
colors:
  canvas: '#F1F5F9'
  surface: '#FFFFFF'
  border: '#E2E8F0'
  ink: '#0F172A'
  muted: '#64748B'
  accent: '#0891B2'
  accent-hover: '#0E7490'
  accent-subtle: '#ECFEFF'
  success: '#16A34A'
  danger: '#DC2626'
  warning: '#D97706'
  focus-ring: '#0891B2'
typography:
  body:
    fontFamily: 'DM Sans'
    fontSize: 15px
    fontWeight: '400'
    lineHeight: '1.5'
  label:
    fontFamily: 'DM Sans'
    fontSize: 13px
    fontWeight: '600'
    lineHeight: '1.35'
  heading:
    fontFamily: 'DM Sans'
    fontSize: 24px
    fontWeight: '700'
    lineHeight: '1.25'
    letterSpacing: '-0.03em'
  heading-sm:
    fontFamily: 'DM Sans'
    fontSize: 18px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: '-0.02em'
  nav-item:
    fontFamily: 'DM Sans'
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.4'
  nav-category:
    fontFamily: 'DM Sans'
    fontSize: 11px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: '0.06em'
  mono:
    fontFamily: 'JetBrains Mono'
    fontSize: 13px
    fontWeight: '400'
    lineHeight: '1.45'
  helper:
    fontFamily: 'DM Sans'
    fontSize: 13px
    fontWeight: '400'
    lineHeight: '1.45'
rounded:
  sm: 6px
  md: 8px
  lg: 12px
  full: 9999px
spacing:
  '1': 4px
  '2': 8px
  '3': 12px
  '4': 16px
  '5': 20px
  '6': 24px
  '8': 32px
  '10': 40px
  gutter: 24px
  page: 40px
  sidebar: 220px
components:
  button-primary:
    background: '{colors.accent}'
    foreground: '#FFFFFF'
    radius: '{rounded.md}'
  button-primary-hover:
    background: '{colors.accent-hover}'
  button-ghost:
    background: '{colors.surface}'
    foreground: '{colors.ink}'
    border: '{colors.border}'
    radius: '{rounded.md}'
  card:
    background: '{colors.surface}'
    border: '{colors.border}'
    radius: '{rounded.lg}'
    shadow: '0 1px 2px rgb(15 23 42 / 0.04)'
  sidebar:
    background: '{colors.surface}'
    border: '{colors.border}'
    width: '{spacing.sidebar}'
  nav-active:
    background: '{colors.accent-subtle}'
    foreground: '{colors.accent-hover}'
    radius: '{rounded.md}'
  input:
    background: '{colors.surface}'
    border: '{colors.border}'
    foreground: '{colors.ink}'
    radius: '{rounded.md}'
  badge:
    background: '{colors.accent-subtle}'
    foreground: '{colors.accent-hover}'
    radius: '{rounded.full}'
  table-header:
    foreground: '{colors.muted}'
status: final
created: 2026-10-25
updated: 2026-10-26
---

# List Mailer — Design Spine

## Brand & Style

**List Mailer** is a self-hosted operator console for list mail — catalogs, campaigns, bounce, and mail-to-send. The visual posture is **clean modern SaaS admin**: light, airy, and calm. It keeps phpList’s *job map* (sidebar groups, indexes, deep-link details) but rejects dense utilitarian chrome.

Brand mark in chrome: the words **List Mailer**. No mascot required for V1. Voice of the surface: professional, quiet, tool-like — not playful consumer marketing.

Theme name: **Clearwater**.

## Colors

| Token | Hex | Role |
|---|---|---|
| `{colors.canvas}` | `#F1F5F9` | App background behind cards |
| `{colors.surface}` | `#FFFFFF` | Sidebar, cards, inputs |
| `{colors.border}` | `#E2E8F0` | Dividers, input borders |
| `{colors.ink}` | `#0F172A` | Primary text |
| `{colors.muted}` | `#64748B` | Secondary text, nav categories, table headers |
| `{colors.accent}` | `#0891B2` | Primary buttons, links, focus |
| `{colors.accent-hover}` | `#0E7490` | Hover / pressed accent |
| `{colors.accent-subtle}` | `#ECFEFF` | Active nav, soft badges |
| `{colors.success}` | `#16A34A` | Success / healthy |
| `{colors.danger}` | `#DC2626` | Errors, destructive |
| `{colors.warning}` | `#D97706` | Warnings / Attention highlight cues |

Accent is cyan — not purple, not terracotta. Do not add a second brand accent. Destructive actions use `{colors.danger}`, never accent.

V1 is **light mode only**.

## Typography

**DM Sans** for UI; **JetBrains Mono** for tokens, IDs, code-like values.

| Role | Use |
|---|---|
| `{typography.heading}` | Page titles |
| `{typography.heading-sm}` | Card / section titles |
| `{typography.body}` | Body copy |
| `{typography.label}` | Field labels |
| `{typography.helper}` | Parameter descriptions, hints |
| `{typography.nav-item}` | Sidebar items — **Title Case** |
| `{typography.nav-category}` | Sidebar categories — **UPPERCASE** |
| `{typography.mono}` | GUIDs, raw headers, technical values |

Page headings and menu items: Title Case. Sidebar categories: UPPERCASE.

## Layout & Spacing

Airy SaaS density: generous page padding (`{spacing.page}`), card gaps (`{spacing.6}`), comfortable table row height. Content sits on `{colors.canvas}` with `{components.card}` panels — not wall-to-wall tables on gray.

- Sidebar fixed ~`{spacing.sidebar}` on desktop; drawer on small viewports.
- Main column fluid; tables scroll horizontally on narrow screens rather than crushing columns.
- Forms (Parameters, settings, all operator inputs): **stacked fields** — Title Case label **above** the control (never beside it), then helper when present — with clear vertical rhythm (`{spacing.4}`–`{spacing.5}` between fields). SPA: `label.field` uses column flex; scalar booleans use toggles; multi-select checkbox/radio rows stay horizontal.
- Field labels are **valid English phrases** (words separated by spaces; never CamelCase or glued API identifiers). Use `fieldLabel()`; prefer natural wording and standard acronyms (HTML, TLS, MFA, URL).

## Elevation & Depth

Light only: cards use a single soft shadow (`{components.card.shadow}`). No stacked elevation system, no heavy drop shadows, no glow. Hierarchy comes from surface vs canvas and typography, not z-depth.

## Shapes

| Token | Value | Use |
|---|---|---|
| `{rounded.sm}` | 6px | Small chips |
| `{rounded.md}` | 8px | Buttons, inputs, nav items |
| `{rounded.lg}` | 12px | Cards, dialogs |
| `{rounded.full}` | pill | Status badges |

## Components

- **Primary button** — `{components.button-primary}`; hover `{components.button-primary-hover}`.
- **Ghost / secondary** — `{components.button-ghost}`.
- **Card** — white surface, 1px border, `{rounded.lg}`, light shadow.
- **Sidebar** — white; active item `{components.nav-active}`.
- **Input** — `{components.input}`; focus ring `{colors.focus-ring}`.
- **Badge** — `{components.badge}` for quiet status; danger/warning variants use those color tokens.
- **Table** — header `{components.table-header}`; row separators `{colors.border}`; no zebra required.
- **Form field** — stacked: `{typography.label}` above control (not inline); multi-select checkbox/radio may keep control + short text on one row.
- **Boolean toggle** — scalar on/off fields use `.toggle` (`role="switch"`): track + knob; On = `{colors.accent}`, Off = muted track; always show On/Off text beside the track. Dirty uses `{colors.warning}` like other fields.
- **Record index** — `{components.table}` / `.data-table` in a `.table-scroll`; primary identity is a text cell, not a button row. **Create is its own page** (index → Create action → create page → back / open detail) — never embedded on the index under the table.
- **Parameter field** — `{typography.label}` → `{typography.helper}` description **between label and control** → **control + Save** row (Save at the trailing end). Boolean values (`true`/`false`) render as a toggle in that row. Row uses flex wrap: control `flex: 1 1` with a min width so it shrinks first; Save wraps only after that min is hit.
- **Unsaved field / Save** — dirty control border + light warning wash (`{colors.warning}`); dirty primary Save/Create uses warning fill; dirty secondary Save uses warning border. Not purple/danger.

## Do's and Don'ts

**Do**

- Keep chrome light and airy; let tables breathe inside cards.
- Use accent sparingly for primary actions and navigation selection.
- Show Parameter helper text between the label and the control (never after the control).

**Don't**

- Recreate dense phpList gray utility chrome.
- Ship dark mode in V1.
- Use purple gradients, cream/terracotta “editorial” palettes, or multi-accent rainbow status.
- Put detail entities (List, Member, Campaign, catalog detail, User detail) as primary sidebar destinations.
