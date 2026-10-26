---
id: 10
type: story
title: "Campaigns end-to-end suite"
parent: epic-campaigns-send
covers: [R1, R8, R7]
after: [9]
hitl: false
risk: medium
refined: true
---

# Campaigns end-to-end suite

## Description

Automated test-profile suite proves the Campaign send path end-to-end after refactors: multi-list dedupe, queue processor with GreenMail, open pixel reporting, and Stop/Requeue paths from EXPERIENCE Flow 4. Acts as the epic regression gate before downstream unsubscribe and bounce epics rely on tokens and correlation fields.

## Requirements

1. **Compose → queue → send → Finished** (R1, R8)
   **Given** the test profile Compose stack with GreenMail and at least two Lists sharing a duplicate Member
   **When** the suite creates a Campaign, queues it, and waits for processing
   **Then** status ends Finished, duplicate Member receives one message, and sent count matches distinct recipients

2. **Open pixel recorded** (R7)
   **Given** a message captured from GreenMail in the suite
   **When** the suite GETs the HTML pixel URL from that message
   **Then** Campaign reporting shows at least one unique open and total opens ≥ 1
   **And** the test completes without blocking SMTP on pixel ingestion

3. **Stop mid-send → Stopped → Requeue** (R8)
   **Given** a large enough recipient set or throttled processor to observe Sending
   **When** the suite invokes Stop then Requeue
   **Then** status moves Stopped with unsent rows still pending, then Queued → Sending → Finished without duplicate sends to already-sent addresses

4. **Finished Requeue after Add List** (R8, epic C5)
   **Given** a Finished Campaign and an additional List whose Members include one address already sent and one new address
   **When** the suite adds the List and Requeues
   **Then** only the new address receives mail in the second pass
   **And** status returns to Finished with sent count increased by one

5. **Suite gate in CI/test profile** (R1, R7, R8)
   **Given** a clean test database and GreenMail fixture
   **When** the campaigns end-to-end suite target runs (documented Gradle/Maven command in repo test docs)
   **Then** all tests pass
   **And** failure output identifies which epic requirement scenario failed

## Boundaries

- Must not change: production default profile behavior; suite runs under test profile only.
- Must not replace: story-level unit tests; this is integrated GreenMail coverage only.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
