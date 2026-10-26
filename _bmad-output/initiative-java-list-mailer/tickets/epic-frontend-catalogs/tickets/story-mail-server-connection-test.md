---
id: 6
type: story
title: "Mail server connection test for Sending Paths and MTS Inboxes"
parent: epic-frontend-catalogs
covers: [R1]
after: [1]
hitl: false
risk: medium
refined: true
status: done
---

# Mail server connection test for Sending Paths and MTS Inboxes

## Description

Operators enter SMTP (Sending Path) or IMAP (Mail-to-Send inbox) credentials that already exist on a mail server. Before saving, they need a **Test Connection** action that attempts a real connect and reports success or a clear error. Does not send mail and does not provision the mailbox.

## Requirements

1. **Sending Path — Test Connection**
   **Given** Admin is on Create or Edit Sending Path with host/port/username/password (and Use TLS)
   **When** they press **Test Connection**
   **Then** the app attempts an SMTP connect with those values
   **And** on success shows a clear success message
   **And** on failure shows that it failed plus the error detail from the mail stack
   **And** if Edit leaves password blank, the stored password for that path is used for the test

2. **MTS Inbox — Test Connection**
   **Given** Admin is on **Add Mail Box** (or editing inbox connection fields) with host/port/username/password (and Use SSL)
   **When** they press **Test Connection**
   **Then** the app attempts an IMAP connect
   **And** success / failure messaging matches Sending Path (failure includes error detail)

3. **Safety / UX**
   **Given** a test is in progress
   **When** the button is pressed
   **Then** the control is disabled until the attempt finishes (no double-submit)
   **And** connection attempts use short timeouts so the UI does not hang indefinitely
   **And** Test Connection does not persist the form and does not send a message

## Boundaries

- In: `POST /api/sending-paths/test-connection`, `POST /api/mts/inboxes/test-connection`; SPA buttons on Sending Path create/edit and MTS Add Mail Box.
- Out: Bounce mailbox test (same IMAP pattern later); Greenmail-only e2e in browser; changing save validation.

## API sketch

Request (SMTP): `{ host, port, username, password?, useTls, pathId? }`  
Request (IMAP): `{ host, port, username, password?, useSsl, inboxId? }`  
Response `200`: `{ "ok": true }` or `{ "ok": false, "message": "…" }`

## References

- EXPERIENCE — Test Connection pattern
- parent — epic-frontend-catalogs.md
- related — MTS Add Mail Box (polling registration only)

## Notes

- Decision: 2026-10-26 — connection probe is connect-only; label **Test Connection**.

## Dev Agent Record

### Completion Notes

- `MailConnectionProbe` SMTP/IMAP connect-only with 10s timeouts; error messages capped at 500 chars.
- `POST /api/sending-paths/test-connection` and `POST /api/mts/inboxes/test-connection` return `{ ok, message }`.
- SPA: Test Connection on Sending Path create/edit and MTS Add Mail Box; blank password on path edit uses `pathId` stored secret.
- Create Sending Path gained Use TLS; Add Mail Box gained Port + Use SSL for accurate probes.

### File List

- mailer/src/main/java/com/listmailer/mailbox/MailConnectionProbe.java
- mailer/src/test/java/com/listmailer/mailbox/MailConnectionProbeTest.java
- mailer/src/main/java/com/listmailer/catalogs/SendingPathsController.java
- mailer/src/main/java/com/listmailer/mts/MtsAdminController.java
- mailer/src/test/java/com/listmailer/mts/MtsCatalogReferenceTest.java
- mailer/frontend/src/ConnectionTest.jsx
- mailer/frontend/src/CatalogPages.jsx
- mailer/frontend/src/OpsMtsPages.jsx
- EXPERIENCE.md / .memlog.md / tickets.toml

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| medium | Bounce mailbox has same IMAP creds but no Test button | Out of story boundaries — follow-up |
| low | Error text may still expose server internals | Accepted per AC; message truncated to 500 |
| low | MTS inbox detail cannot re-test without Add form | Acceptable for V1; detail is senders/mappings focused |
| info | `mail.imaps.ssl.trust=*` mirrors existing poller | Same as `ImapMailboxQuery` — no change |
| fixed | Record factory named `ok()` collided with component `ok` | Renamed to `success()` / `failure()` |
