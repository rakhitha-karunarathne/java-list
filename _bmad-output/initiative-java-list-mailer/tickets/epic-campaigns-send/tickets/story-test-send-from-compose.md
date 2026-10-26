---
id: 3
type: story
title: "Test send from compose"
parent: epic-campaigns-send
covers: [R3]
after: [1]
hitl: false
risk: medium
refined: true
---

# Test send from compose

## Description

Operators can validate content and SMTP connectivity from Compose without queueing a Campaign. Test sends use the first selected List’s Sending path, bypass bounce-driven disable for recipients, and never affect Campaign sent or open reporting.

## Requirements

1. **Test send to pasted addresses** (R3)
   **Given** a Draft Campaign on Compose with at least one List selected and valid HTML or text
   **When** the operator pastes one or more recipient addresses and runs Test send
   **Then** each pasted address receives a single message via the first selected List’s Sending path in GreenMail (test profile)
   **And** the Campaign status stays Draft and no send-queue rows are created for the Campaign

2. **Ignores bounce-disable for test** (R3)
   **Given** a pasted address belongs to a Member who is disabled due to bounce processing on a selected List
   **When** the operator runs Test send including that address
   **Then** GreenMail still receives the test message for that address
   **And** the Member’s disabled state is unchanged afterward

3. **Does not affect Campaign sent stats** (R3)
   **Given** a Campaign that was previously Finished with a known sent count on detail
   **When** the operator opens Compose for a new Draft (or the same Draft) and runs Test send
   **Then** that Campaign’s sent count and open reporting are unchanged
   **And** test messages do not create open-pixel events on the Campaign

4. **SMTP failure → Ops Logs and Attention** (R3)
   **Given** the first List’s Sending path points at an unreachable or rejecting SMTP endpoint (or GreenMail is stopped in test)
   **When** the operator runs Test send
   **Then** the operator sees a clear failure on Compose
   **And** an Ops Logs entry exists for the failure and Admin Attention surfaces it (hooks per epic-ops-attention-audit; emission owned here)

5. **Validation failures** (R3)
   **Given** no List is selected, or pasted addresses are empty or malformed
   **When** the operator runs Test send
   **Then** no SMTP attempt is made
   **And** validation messaging explains what to fix

## Boundaries

- Must not change: full Campaign queue dedupe, lifecycle states, or bounce-disable rules on real queue sends.
- Must not change: Campaign reporting, open pixel, or opaque token issuance on test mail.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
