---
id: 7
type: story
title: "Image-fetch modes SSRF and payload ceilings"
parent: epic-campaigns-send
covers: [R5, R6]
after: [5]
hitl: false
risk: high
refined: true
---

# Image-fetch modes SSRF and payload ceilings

## Description

Campaign compose can pull remote images under Off, Best Effort, or Mandatory fetch modes with SSRF protections. Email payload size and stored-image size obey hard ceilings; multi-list Campaigns apply the strictest per-List limits, including CDN/blob TTL rules tied to Finished state.

## Requirements

1. **Image-fetch Off** (R5)
   **Given** fetch mode Off and HTML contains external `http(s)` image URLs
   **When** the operator queues the Campaign
   **Then** remote images are not fetched into blob storage
   **And** outbound HTML keeps original URLs or documented non-fetch behavior without SSRF egress to private targets from the app

2. **Best Effort vs Mandatory** (R5)
   **Given** HTML references a reachable public image and a failing URL
   **When** mode is Best Effort and the operator queues
   **Then** queue succeeds, reachable images become blobs where applicable, and failing URLs do not block send
   **When** mode is Mandatory and any required fetch fails
   **Then** queue is rejected or Campaign cannot enter Queued with a clear error
   **And** no partial send starts for that queue attempt

3. **SSRF guards** (R5)
   **Given** HTML references URLs pointing at loopback, RFC1918, link-local, or other blocked targets per architecture
   **When** fetch runs in Best Effort or Mandatory
   **Then** those targets are not retrieved
   **And** Mandatory failure messaging identifies blocked or unreachable fetch (without echoing secrets)

4. **Payload and stored-image hard ceilings** (R6)
   **Given** a List with a configured per-List ceiling and content exceeding the email payload or single-image stored size limit
   **When** the operator attempts queue (or save that ingests blobs)
   **Then** the action is blocked with a ceiling-specific message
   **And** no send-queue rows are created for that attempt

5. **Multi-list strictest caps** (R6)
   **Given** two selected Lists with different ceiling or TTL parameters
   **When** the operator queues a multi-list Campaign
   **Then** the effective limit is the strictest combination (smallest payload/image cap; longest CDN/blob TTL rule per R5/R6 epic wording)
   **And** a List over its own ceiling cannot be compensated by a looser sibling List

6. **TTL restarts on Finished after Requeue** (R5)
   **Given** a Campaign reached Finished and later Requeue (from Stopped or Finished-after-Add-List per R8) completes another Finished
   **When** comparing blob CDN/TTL expiry behavior documented for operators or tests
   **Then** TTL restarts from the most recent Finished
   **And** Stop alone without a completing Finished does not restart TTL

## Boundaries

- Must not change: public `/dl` routing contract (story 5) or pixel token shapes (story 8).
- Must not change: dedupe or first-list Sending path (story 2).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Attempt fetch of `http://127.0.0.1/` and metadata IP ranges in Mandatory mode and confirm zero bytes stored.
