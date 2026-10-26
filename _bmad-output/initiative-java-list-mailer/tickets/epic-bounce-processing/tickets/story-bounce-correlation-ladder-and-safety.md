---
id: 3
type: story
title: "Bounce correlation ladder and safety"
parent: epic-bounce-processing
covers: [R1, R4]
after: [2]
hitl: false
risk: high
refined: true
---

# Bounce correlation ladder and safety

## Description

After poll ingest, match each bounce to a send-queue row using the **correlation ladder**: VERP/Return-Path when Parameters VERP is on, then opaque **token** (`X-JavaList-Token`), then **Message-ID** and other useful standard headers, then **subject+email** fallback. Join correlated sends by **member GUID**, not email alone. Classify **hard** vs **soft** bounce. **Uncorrelated** bounces are logged and raise **Attention**; they must **never** auto-disable a Member and must **never** reset or increment disable **streaks** (C7, architecture AD-25).

## Requirements

1. **VERP first when Parameter on** (R1)
   **Given** Parameters VERP toggle is on and a sent queue row has VERP/Return-Path correlation stored at send
   **When** a bounce DSN references that Return-Path address
   **Then** the bounce correlates to that queue row and member GUID
   **And** lower ladder rungs are not required for the match

2. **Token correlation** (R1)
   **Given** VERP is off or absent on the bounce and the queue row has `X-JavaList-Token` (or equivalent header) from send-queue
   **When** the bounce carries the same token in headers or body
   **Then** correlation succeeds to that queue row
   **And** member GUID is taken from the queue row

3. **Message-ID and headers** (R1)
   **Given** token match fails but Message-ID (or other configured standard headers) on the bounce matches values stored on a queue row at send
   **When** correlation runs
   **Then** the bounce links to that send
   **And** the match is recorded with which signal succeeded

4. **Subject+email fallback** (R1)
   **Given** higher rungs fail but subject and recipient email match a recent send’s stored fallback fields
   **When** correlation runs
   **Then** the bounce links to that send if uniquely identifiable
   **And** ambiguous subject+email matches are treated as uncorrelated (no guess-disable)

5. **Uncorrelated → log + Attention, no disable, no streak** (R1, R4)
   **Given** a bounce that matches no queue row at any ladder rung
   **When** ingest/correlation completes
   **Then** the bounce is stored or flagged as uncorrelated with diagnostic detail in Ops Logs
   **And** an Attention item is created or updated for operator review
   **And** the Member remains Enabled (no global Disabled)
   **And** consecutive hard/soft counts for any email are unchanged by this bounce

6. **Correlated bounce does not disable immediately** (R1, R4)
   **Given** a newly correlated hard or soft bounce
   **When** correlation completes before daily bounce-process
   **Then** Member is not auto-disabled solely from this ingest step
   **And** disable only occurs when daily process limits are hit (story 4)

7. **Hard vs soft classification** (R1)
   **Given** SMTP/DSN status codes or equivalent signals in the bounce payload
   **When** a correlated bounce is persisted
   **Then** it is labeled hard or soft per product rules
   **And** the label is available to daily bounce-process walks

8. **Safety enforced in process logic** (R4)
   **Given** a code path that updates Member disable state or streak counters
   **When** reviewed or tested with injected uncorrelated bounce records
   **Then** no branch applies disable or streak mutation unless correlation to a member GUID succeeded
   **And** this holds even if Attention logging fails (fail-safe toward no disable)

## Boundaries

- Must not implement: daily consecutive walks, holdback marking, or global disable (story 4).
- Must not implement: VERP Parameter toggle UI (catalogs epic); read toggle via Parameters port only.
- Must not mark list **unsubscribe** on bounce disable paths (epic C3; disable is story 4).
- Must not change: send-queue field population at SMTP send (epic-campaigns-send); consume contract only.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-bounce-processing/epic-bounce-processing.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-25, AD-26)
- addendum — _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/addendum.md (correlation signals)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Seed two queue rows with same email but different member GUIDs; confirm ladder never disables the wrong member.
