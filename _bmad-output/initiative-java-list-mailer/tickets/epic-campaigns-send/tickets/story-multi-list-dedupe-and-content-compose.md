---
id: 2
type: story
title: "Multi-list dedupe and content compose"
parent: epic-campaigns-send
covers: [R1, R2]
after: [1]
hitl: false
risk: high
refined: true
---

# Multi-list dedupe and content compose

## Description

Extends the tracer compose flow so operators can target multiple Lists in one Campaign, with full HTML/text content, embedded assets, and a live preview. Enqueue dedupes recipients by address and keeps the first selected List’s Sending path for the whole Campaign.

## Requirements

1. **Multi-list selection on compose** (R1)
   **Given** the operator has send access to at least two Lists with overlapping Members
   **When** they select those Lists on Compose and save or queue the Campaign
   **Then** the Campaign detail shows every selected List
   **And** the Campaign remains in Draft until an explicit Queue action (same as story 1)

2. **Same address → one queue row** (R1)
   **Given** the same email address is an enabled, subscribed Member on two or more selected Lists
   **When** the operator queues the Campaign
   **Then** exactly one send-queue row exists for that address for this Campaign
   **And** GreenMail (test profile) receives only one message for that address when the pass completes

3. **First List Sending path** (R1)
   **Given** selected Lists use different Sending paths and the operator ordered List A before List B
   **When** the Campaign sends
   **Then** SMTP uses List A’s Sending path (not List B’s)
   **And** reordering Lists before queue changes which path applies on the next queue from Draft

4. **HTML, text, images, and attachments** (R2)
   **Given** Compose is open on a Draft Campaign
   **When** the operator enters HTML body, plain-text body, at least one inline or referenced image, and at least one attachment
   **Then** saved content is retained on reload
   **And** queued mail includes both HTML and text parts plus attachments visible in GreenMail

5. **HTML/Text preview pane** (R2)
   **Given** HTML and text editors both have content
   **When** the operator switches between HTML and Text preview tabs
   **Then** each tab reflects the current editor content without a full page reload
   **And** editing one body updates the matching preview tab before queue

6. **Queue blocked without Lists or bodies** (R1, R2)
   **Given** a Draft Campaign with no Lists selected, or with empty HTML and empty text
   **When** the operator attempts Queue
   **Then** queue does not proceed and a clear validation message is shown
   **And** no send-queue rows are created

## Boundaries

- Must not change: Draft → Queued → Sending → Finished lifecycle, Stop/Requeue rules, test-send behavior, or Notice sending path (epic C4).
- Must not change: per-Member enable/disable and unsubscribe exclusions already enforced at enqueue (lists/members epic).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: With three Lists and pairwise duplicate addresses, spot-check queue row count equals distinct addresses only.
