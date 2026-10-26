---
id: 5
type: story
title: "Blob storage and public dl"
parent: epic-campaigns-send
covers: [R9]
after: [2]
hitl: false
risk: high
refined: true
---

# Blob storage and public dl

## Description

Campaign images and similar assets live as database blobs tied to a Campaign. Outbound HTML uses public HTTPS `/dl/<campaignGuid>/…` URLs so recipients can load images without operator session. Deleting a Campaign removes its blobs.

## Requirements

1. **Images stored as DB blobs on save/queue** (R9)
   **Given** Compose references images that should ship with the Campaign
   **When** the operator queues (or saves content that persists blobs per product rules)
   **Then** blob records exist scoped to that Campaign’s guid
   **And** stored bytes match what was uploaded or fetched for send

2. **Public GET `/dl/<campaignGuid>/<file>`** (R9)
   **Given** a Queued or Sending Campaign whose HTML references a rewritten `/dl/…` image URL
   **When** an unauthenticated client GETs that URL over HTTPS
   **Then** the response is `200` with the correct image content-type and body
   **And** operator JWT is not required (public member surface per epic C1)

3. **HTML rewrite uses `/dl` URLs** (R9)
   **Given** Campaign HTML contained internal or upload references before queue
   **When** mail is sent to GreenMail
   **Then** the HTML part’s `img` sources use `/dl/<campaignGuid>/…` under the configured public HTTPS base
   **And** fetching those URLs from outside the admin UI succeeds

4. **Cascade delete with Campaign** (R9)
   **Given** a Campaign with at least one stored blob
   **When** the Campaign is deleted (product-supported delete path)
   **Then** all blobs for that Campaign guid are removed
   **And** subsequent GETs to former `/dl/…` URLs return not found

5. **Wrong guid or path** (R9)
   **Given** a random or other Campaign’s guid in the `/dl/` path
   **When** a client requests the URL
   **Then** the response is not found or forbidden without leaking another Campaign’s bytes

## Boundaries

- Must not change: open-pixel URL shape (`/t/…`), unsubscribe handlers (later epic), or object-store off-V1 scope (epic boundaries).
- Must not change: image-fetch modes, SSRF guards, or payload ceilings (story 7).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Confirm public `/dl/**` remains outside SPA auth filter behavior already reserved in kernel security.
