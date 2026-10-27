---
id: 11
type: story
title: "Bounce Messages index UI"
parent: epic-frontend-ops-mts
covers: [R4]
after: [10]
hitl: false
risk: medium
refined: true
---

# Bounce Messages index UI

## Description

Rename sidebar **Bounce Log** → **Bounce Messages** and replace the stub page with a Clearwater index of bounce messages backed by the Bounce messages list API. Distinct from **Bounce Processing Log** (poll/process activity) and **Bounce Mailboxes** (IMAP catalog).

## Requirements

1. **Rename**
   **Given** the BOUNCES nav section
   **When** an Admin or List manager views the sidebar
   **Then** the item label is **Bounce Messages** (not Bounce Log)
   **And** page H2 / crumb match
   **And** Bounce Processing Log and Bounce Mailboxes labels stay unchanged

2. **Message list**
   **Given** bounce events from the list API
   **When** the operator opens Bounce Messages
   **Then** a newest-first table shows columns aligned to the mock: **When**, **Email**, **Campaign**, **Class**, **Error**, **Correlated?**
   **And** Email / Campaign link to Member / Campaign detail when ids exist
   **And** uncorrelated rows show email/campaign as — when unknown, Correlated? = No (Attention remains the action inbox)

3. **Filters**
   **Given** the toolbar
   **When** the operator chooses All | Correlated | Uncorrelated | Hard | Soft
   **Then** the table reflects that filter via the list API

4. **Roles**
   **Given** role rules from FR-25 / mock
   **When** List sender uses the app
   **Then** Bounce Messages is hidden (same as current Bounce Log hideForSender)
   **And** List manager sees only in-scope rows from the API

5. **Run process entry**
   **Given** Admin needs bounce-process
   **When** on Bounce Messages
   **Then** keep a clear path to run bounce processing (existing button or link) without confusing this page with Bounce Processing Log

6. **Attention deep link**
   **Given** Attention “Open Bounce”
   **When** the operator follows the link
   **Then** Bounce Messages opens focused on that bounce id (filter or detail) — Attention is not a second store (FR-8)

## Boundaries

- No correlation or disable logic in the UI — display API outcomes only.
- Must not merge this surface into Bounce Processing Log.
- Out: editing raw MIME; mailbox CRUD (Bounce Mailboxes).

## References

- prd — FR-25 visibility; FR-26 bounce list UI / extracted error; FR-8 Attention → bounce log
- forge — Bounce list UI
- ux — `mockups/clickable/index.html` `#page-bounces`; EXPERIENCE Bounce Processing Log (sibling)
- depends — epic-bounce-processing story **Bounce messages list API**
- parent — `epic-frontend-ops-mts.md`
- code — `navConfig.js` (`bounces`), stub `BounceLogPage` in `OpsMtsPages.jsx`

## Notes

- Decision: 2026-10-27 — User rename Bounce Log → Bounce Messages; list messages on that page; stories from FR-25/26 + Clearwater mock.
- FE-5.6 walkthrough explicitly flagged Bounce Log as stub.
