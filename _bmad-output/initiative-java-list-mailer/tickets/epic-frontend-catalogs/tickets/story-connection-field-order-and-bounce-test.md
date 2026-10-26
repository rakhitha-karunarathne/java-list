---
id: 7
type: story
title: "Connection field order and Bounce Test Connection"
parent: epic-frontend-catalogs
covers: [R1, R2]
after: [6]
hitl: false
risk: low
refined: true
status: done
---

# Connection field order and Bounce Test Connection

## Description

Align connection forms so **Port** follows **Host**, and **Password** follows **Username**. Add **Test Connection** on Bounce mail box create and edit (IMAP), matching Sending Path / MTS.

## Requirements

1. **Sending Path create (and edit)**
   **Given** Admin opens Create or Edit Sending Path
   **When** the form renders
   **Then** field order is Name → Host → Port → Username → Password → From Email → From Name → Use TLS

2. **Bounce mail box create and edit**
   **Given** Admin opens Add Mail Box or Bounce detail
   **When** the form renders
   **Then** Port is immediately after Host
   **And** **Test Connection** probes IMAP (blank password on edit uses stored secret)
   **And** success / failure messaging matches other connection tests

3. **MTS Add Mail Box / inbox account**
   **Given** Admin opens Add Mail Box or inbox account fields
   **When** the form renders
   **Then** Port is immediately after Host (before Username / Password)

## Boundaries

- In: SPA field order; `POST /api/bounce-mailboxes/test-connection`.
- Out: Changing validation rules; Bounce e2e against GreenMail in browser.

## References

- EXPERIENCE — Test Connection; Add Mail Box
- related — story-mail-server-connection-test (Bounce was deferred)

## Notes

- Decision: 2026-10-26 — Host then Port; Username then Password; Bounce gets Test Connection.

## Dev Agent Record

### Completion Notes

- Sending Path create/edit: Name → Host → Port → Username → Password → From…
- Bounce create/edit: Host → Port → Username → Password; `POST /api/bounce-mailboxes/test-connection`
- MTS inbox account fields: Host → Port → Username → Password

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| low | Bounce Test Connection was deferred from story #6 | Delivered here |
| low | Edit Sending Path previously put Port after From fields | Fixed to match create |
| info | Three page commits as requested | Sending Path; Bounce; MTS |
