---
id: 8
type: story
title: "Open pixel reporting and opaque send tokens"
parent: epic-campaigns-send
covers: [R7, R10]
after: [6]
hitl: false
risk: high
refined: true
---

# Open pixel reporting and opaque send tokens

## Description

HTML campaigns embed a tracking pixel served asynchronously so SMTP is not blocked on open recording. Campaign detail and reporting expose sent volume and open metrics; each send issues opaque member tokens and public HTTPS URLs later epics consume, plus send-queue correlation fields for bounce processing.

## Requirements

1. **HTML pixel only in outbound mail** (R7)
   **Given** a Campaign with an HTML body queued and sent via GreenMail
   **When** the received message is inspected
   **Then** the HTML part contains a tracking pixel reference and the plain-text part does not rely on pixel opens for counts
   **And** pixel URL uses HTTPS and the public base parameter shape

2. **Async open recording** (R7, R9)
   **Given** a sent message with a valid pixel URL
   **When** a client GETs the pixel (simulating a mail client load)
   **Then** the HTTP response returns promptly (GIF or documented pixel body) without waiting for heavy DB work on the request thread
   **And** an open event is persisted for the Member/Campaign pair shortly after

3. **Reporting: sent, unique opens, total opens** (R7)
   **Given** a Finished Campaign where some recipients opened once and one recipient opened twice
   **When** the operator views Campaign detail reporting
   **Then** sent count matches successful queue sends, unique opens headline matches distinct openers, and total opens counts all pixel hits
   **And** per-member rows show first-open timestamp and open count

4. **Signed or opaque pixel tokens** (R7)
   **Given** two Members on the same Campaign
   **When** comparing pixel URLs from their respective messages
   **Then** tokens differ and are not guessable sequential ids
   **And** altering the token in the URL does not attribute opens to another Member

5. **Opaque send tokens and URL shapes for later epics** (R10)
   **Given** a successful Campaign send to a Member
   **When** inspecting the message or persisted send metadata
   **Then** unsubscribe (and related) links use opaque tokens and documented public HTTPS path prefixes for epic-unsubscribe-consent
   **And** tokens are issued at send time, not reused across unrelated Campaigns

6. **Send-queue correlation fields** (R10)
   **Given** a row processed to sent on the send-queue
   **When** an operator or test inspects queue row data exposed for ops/debug (or test fixtures)
   **Then** bounce-correlation fields required by architecture AD-18 are populated
   **And** fields remain stable for the life of the row

## Boundaries

- Must not change: unsubscribe/consent handler behavior (later epic); this story only issues matching token/url shapes.
- Must not change: test-send exclusion from Campaign stats (story 3) or click tracking (out of scope).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Load pixel twice for one recipient and confirm unique vs total open counters move as specified.
