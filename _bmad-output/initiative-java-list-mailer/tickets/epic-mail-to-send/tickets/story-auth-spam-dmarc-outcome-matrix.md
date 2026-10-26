---
id: 3
type: story
title: "Auth spam DMARC outcome matrix"
parent: epic-mail-to-send
covers: [R3]
after: [1]
hitl: false
risk: high
refined: true
---

# Auth spam DMARC outcome matrix

## Description

Mail-to-send evaluates authorized and stranger mail in a fixed order: SPF/DKIM/DMARC first (DMARC fail always gets reject treatment regardless of published policy), then spam **header** scoring only when the score header is present (absent header skips spam, not fail), then From-domain / Campaign creation. Outcomes follow the silent / notify / Drop log / auto-block rules below; strangers never receive courtesy rejects.

## Requirements

1. **Evaluation order (R3)**
   **Given** a message arrives in an MTS inbox
   **When** ingest evaluates it
   **Then** auth (SPF/DKIM/DMARC) runs before spam header checks
   **And** spam header checks run only if the score header is present
   **And** From-domain validation and Campaign Queued creation run only after auth and spam steps pass for that path

2. **DMARC fail = reject treatment (R3)**
   **Given** DMARC fails for the message
   **When** auth is evaluated
   **Then** the message is treated as reject-path failure regardless of the domain’s published DMARC policy
   **And** downstream matrix rows apply as for auth/spam fail (not as auth pass)

3. **Spam header absent → skip (R3)**
   **Given** the message has no spam score header
   **When** spam evaluation runs
   **Then** spam is skipped (not treated as fail)
   **And** evaluation continues to the next gate

4. **Strictest spam threshold wins (R3)**
   **Given** global, per-mailbox, per-authorized-sender, and per-List thresholds are configured
   **When** a spam score header is present and mapped to multiple Lists
   **Then** the strictest applicable threshold is used
   **And** failing on any mapped List rejects the entire message (all-or-nothing with story 2)

5. **Auth/spam fail, From authorized on this inbox (R3)**
   **Given** the sender is authorized on the inbox being polled
   **When** auth or spam fails
   **Then** the message is silently deleted from the inbox
   **And** a Drop log row is written with reason and **does not** count toward auto-block (R4)

6. **Auth pass, sender not approved on any inbox (R3)**
   **Given** auth (and spam if header present) pass
   **When** the sender is not an authorized sender on any MTS inbox
   **Then** the message is silently deleted
   **And** a Drop log row is written that **counts** toward sender auto-block (R4)
   **And** no courtesy reject is sent to the stranger

7. **Auth pass, approved elsewhere but not this inbox (R3)**
   **Given** auth pass and the sender is authorized on inbox B but not on inbox A where the message arrived
   **When** ingest completes on inbox A
   **Then** the message is rejected with sender notify (cross-inbox authorized notice exception)
   **And** a Drop log row is written without auto-block count
   **And** no Campaign is created on inbox A

8. **Stranger on auth/spam fail paths (R3)**
   **Given** the sender is not authorized on the current inbox
   **When** auth or spam fails
   **Then** silent delete + Drop log as applicable
   **And** no courtesy reject confirming the address exists

9. **From-domain Issue path unchanged (R3)**
   **Given** auth and spam pass for an authorized mapping
   **When** substitute From domain validation fails
   **Then** story 2 From-domain Issues apply (hold or delete-and-notify)
   **And** auth/spam outcomes do not write Drop log instead of Issues for that case

10. **Fixture matrix verification (R3)**
    **Given** GreenMail (or test) fixtures for auth fail, spam header fail, DMARC fail, and cross-inbox authorized cases
    **When** each fixture is ingested
    **Then** silent vs notify vs Drop log vs auto-block-count flags match the rows above

## Boundaries

- V1 reads spam score from header only when present; no external spam service or AI scoring.
- Drop log UI, global blocks, and N/day auto-block enforcement are story 4; this story writes Drop log rows and count flags correctly.
- From-domain Issues (story 2) remain separate from auth/spam Drop reasons.
- Blocked sender/domain checks after auth (story 4) short-circuit before Campaign creation.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-mail-to-send/epic-mail-to-send.md
- prd — _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/prd-java-list-mailer.md (FR-30)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Matrix tests should table-drive reason + counted-for-auto-block + notify flags per fixture.
