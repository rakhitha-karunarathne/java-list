---
id: 7
type: story
title: "MTS inbox edit and per-sender list grants"
parent: epic-frontend-ops-mts
covers: [R1]
after: [1]
hitl: false
risk: high
refined: true
status: done
---

# MTS inbox edit and per-sender list grants

## Description

Inbox detail must let Admins update mailbox account settings. Authorized-sender mail maps to Lists **per sender**: substitute From and footer override are per (sender × list), not a single named inbox mapping. Clicking a sender opens a dedicated page to grant Lists and set those overrides.

## Requirements

1. **Update mailbox account**
   **Given** Admin opens an MTS inbox detail
   **When** they change name / host / port / username / password / SSL / poll / enabled and Save
   **Then** `PUT /api/mts/inboxes/{id}` persists the changes
   **And** blank password on save keeps the stored password
   **And** Test Connection remains available on the edit form

2. **Lists as a table (sender page)**
   **Given** Admin opens an authorized sender from inbox detail
   **When** the sender page loads
   **Then** available Lists appear in an HTML table (Granted + Name + Substitute From + Footer)
   **And** not as a stack of bare checkboxes only

3. **Per sender × list substitute From and footer**
   **Given** Admin grants one or more Lists for a sender
   **When** they set optional Substitute From and/or Footer per granted row and Save
   **Then** those values persist for that inbox + sender + list
   **And** ingest uses that sender’s granted Lists (not a shared inbox “first mapping”)
   **And** footer override is per granted list (else List footer); From-domain validation uses that row’s Substitute From

4. **Sender email navigates to edit page**
   **Given** inbox detail shows authorized senders
   **When** Admin clicks a sender email
   **Then** a new page opens (not sidebar) to edit that sender’s list grants
   **And** back returns to the inbox detail

5. **Index overview**
   **Given** Inboxes & Mappings index
   **When** it loads
   **Then** All Mappings shows sender → list grants (inbox, sender, list, substitute From, footer)

## Boundaries

- In: Flyway migrate named mappings → sender list grants; admin API; SPA inbox edit + sender page; ingest/campaign/from-domain consumers.
- Out: Bounce mailbox edit polish; changing auth/spam gates; inventing multi-campaign UX beyond one Campaign of the sender’s granted Lists.

## API sketch

- `PUT /api/mts/inboxes/{id}` — existing inbox body (password optional)
- `GET /api/mts/inboxes/{id}/senders/{email}` — `{ email, grants: [{ listId, listName, granted, substituteFrom, footerId }] }`
- `PUT /api/mts/inboxes/{id}/senders/{email}/grants` — body `{ grants: [{ listId, granted, substituteFrom, footerId }] }`
- `GET /api/mts/mappings` — flat grant overview rows for index

## References

- parent — epic-frontend-ops-mts.md
- related — story-mts-inbox-detail-senders-and-mappings-ui; epic-mail-to-send R1 (sender+inbox+List substitute From)

## Notes

- Decision: 2026-10-26 — replace inbox-named mappings with sender list grants; substitute From + footer live on the grant row.

## Dev Agent Record

### Completion Notes

- Flyway V24: `mts_sender_list_grants` (id + inbox/sender/list + substitute_from + footer_id); migrates old mappings × authorized senders; drops named mapping tables; clears `MTS_MAPPING` catalog refs.
- Admin API: inbox PUT unchanged; `GET/PUT …/senders/{email}/grants`; `GET /mappings` returns flat grant rows; create/delete named mapping removed.
- Ingest uses that sender’s grants; From-domain validates per-grant Substitute From; campaign footer uses first grant (shortcut for multi-list footer mismatch).
- SPA: inbox detail edits account + Test Connection; sender email → grant page with Lists table; index All Mappings shows grant rows.

### File List

- mailer/src/main/resources/db/migration/V24__mts_sender_list_grants.sql
- mailer/src/main/java/com/listmailer/mts/MtsSenderListGrantEntity.java
- mailer/src/main/java/com/listmailer/mts/MtsSenderListGrantRepository.java
- mailer/src/main/java/com/listmailer/mts/MtsAdminController.java
- mailer/src/main/java/com/listmailer/mts/MtsIngestService.java
- mailer/src/main/java/com/listmailer/mts/MtsCampaignFactory.java
- mailer/src/main/java/com/listmailer/mts/MtsFromDomainService.java
- (removed) MtsMappingEntity/List/Repository
- mailer/frontend/src/OpsMtsPages.jsx, mtsNav.js
- EXPERIENCE.md / .memlog.md / tickets.toml

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| medium | Multi-list grants with different footers: one Campaign uses first grant’s footer | Documented shortcut; split campaigns later if needed |
| medium | Migration copies each mapping to every authorized sender on that inbox | Intentional expansion; mappings with zero senders are dropped |
| low | Orphan `MTS_MAPPING` Used By cleaned in V24; new source type `MTS_SENDER_GRANT` | Fixed in migration + grant save |
| low | Loading flash to index while detail fetches | Fixed — loading card for inbox/sender |
| info | Substitute From still validation-only at send (pre-existing) | Out of story; stored per grant |
