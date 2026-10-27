---
id: 11
type: story
title: "User display time zone (My Account + client timestamps)"
parent: epic-frontend-catalogs
covers: [R6]
after: [10]
hitl: false
risk: medium
refined: true
---

# User display time zone (My Account + client timestamps)

## Description

Each user picks a **display time zone** on My Account. The SPA shows every timestamp (including log Time columns) in that zone. Values stay **UTC in storage and APIs**; conversion is client-only.

## Requirements

1. **Preference storage**
   **Given** a signed-in user
   **When** they save a time zone on My Account
   **Then** it is stored on their user row as an IANA zone id (e.g. `Asia/Colombo`)
   **And** invalid ids are rejected (400)
   **And** existing users default to `UTC`

2. **My Account UI**
   **Given** My Account
   **When** rendered
   **Then** a Time Zone card (dropdown of IANA zones) sits between Password and MFA
   **And** saving updates the preference and the live display zone for the session

3. **Client display**
   **Given** the user’s time zone
   **When** any UI shows a timestamp (campaign times, bounce received, mailbox poll times, processing/activity/ops log Time fields, etc.)
   **Then** it is formatted in that zone
   **And** blank/null still shows `—`
   **And** APIs and DB remain UTC / Instant with no server-side zone conversion for display

## Boundaries

- In: `users.time_zone`, `GET /api/me`, `PUT /api/me/time-zone`, My Account Time Zone card, shared SPA formatter used by all timestamp displays.
- Out: Server formatting; per-list or per-campaign zones; changing how send schedules interpret wall-clock (still UTC/Instant as today).

## References

- parent — epic-frontend-catalogs.md
- related — story-my-account-self-service.md

## Notes

- Decision: 2026-10-27 — User: account dropdown; all client times in that zone; store UTC only; convert client-side; document → implement → review → commit → rebuild.

## Dev Agent Record

### Completion Notes

- `users.time_zone` (V39, default UTC); `GET /api/me`, `PUT /api/me/time-zone` (IANA-only via `ZoneId.getAvailableZoneIds`).
- My Account Time Zone card between Password and MFA; `TimeZoneProvider` + `formatWhen` on campaign/bounce/mailbox/ops/activity/processing timestamps.

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| medium | PUT accepted offset/`ZoneId` short ids, not only IANA | Fixed — `requireIanaZone` + corrupt GET → UTC |
| medium | Stale `/api/me` after token change | Fixed — cancelled flag on provider effect |
| medium | Async load could clobber in-progress draft | Fixed — `zoneTouched` ref |
| medium | `pollIndexColumns` fallback was raw String | Fixed — `formatWhen(..., 'UTC')` |
| medium | Missing tests: `me` stored zone, blank PUT | Fixed — extended `MeAccountControllerTest` |
| low | Full IANA list without search; UTC flash before load | Deferred — story asked dropdown; brief flash OK |
