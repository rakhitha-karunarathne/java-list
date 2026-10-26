---
id: 2
type: story
title: "From-domain Issues hold or notify"
parent: epic-mail-to-send
covers: [R2]
after: [1]
hitl: false
risk: high
refined: true
---

# From-domain Issues hold or notify

## Description

When a substitute From domain does not match the List’s Sending-path From domain, or when a Sending-path From/domain change invalidates existing mappings, Mail-to-send opens **From-domain Issues** — a separate operator surface from the **Drop log**. Processing is all-or-nothing: no partial Campaign. Operators resolve via IMAP hold folder per sender per inbox (preferred) or delete-and-notify when hold cannot be created.

## Requirements

1. **Issues surface ≠ Drop log (R2)**
   **Given** a message fails substitute From / Sending-path domain validation
   **When** the operator investigates the outcome
   **Then** a From-domain Issue is visible in the Issues UI
   **And** the event is not recorded only in the Drop log and does not increment auto-block counters (R4)

2. **All-or-nothing on multi-List mapping (R2)**
   **Given** a mapping targets multiple Lists and at least one List fails From-domain validation for the message
   **When** ingest evaluates the message
   **Then** no Campaign is created (no partial List subset)
   **And** a From-domain Issue covers the whole message

3. **Re-validate on Sending-path From/domain change (R2)**
   **Given** a Sending path’s From or domain is changed in catalog
   **When** save completes
   **Then** affected MTS mappings are re-validated
   **And** new or updated From-domain Issues appear for mappings that no longer satisfy the substitute From domain rule

4. **Hold path (preferred) (R2)**
   **Given** IMAP CREATE for a hold folder succeeds for the inbox
   **When** a From-domain Issue is opened for a sender on that inbox
   **Then** the message is moved to a hold folder scoped per sender per inbox
   **And** the authorized sender is notified (Notice sending path)
   **And** no Campaign exists until the Issue is cleared and ingest re-processes

5. **Clear Issue → process (R2)**
   **Given** a held From-domain Issue is cleared by an operator (mapping or path fixed)
   **When** re-processing runs
   **Then** the message follows the normal authorized path (including auth/spam gates)
   **And** the hold folder for that sender on that inbox is removed when empty

6. **Delete-and-notify fallback (R2)**
   **Given** IMAP CREATE for the hold folder fails
   **When** a From-domain Issue would otherwise hold the message
   **Then** the message is deleted from the inbox
   **And** the sender is notified that the message was deleted and must be resent
   **And** operator notices clearly distinguish **held** vs **deleted** wording

7. **No Campaign while Issue open (R2)**
   **Given** an open From-domain Issue for a message
   **When** any ingest pass runs
   **Then** Campaign Queued is not created for that message
   **And** resolving the Issue is the only path to authorized Campaign creation

## Boundaries

- Must not route From-domain outcomes through Drop log rows or global auto-block counters (R4).
- Must not split a single message into Campaigns for a subset of mapped Lists.
- Hold/delete-and-notify notices use Notice sending path; Campaign send path unchanged.
- Auth/spam/DMARC matrix (R3) runs before From-domain Issue creation when both apply; this story owns Issue hold/delete semantics only.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-mail-to-send/epic-mail-to-send.md
- prd — _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/prd-java-list-mailer.md (FR-29)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check: confirm Issue list filters and copy never label a From-domain row as a “drop” reason.
