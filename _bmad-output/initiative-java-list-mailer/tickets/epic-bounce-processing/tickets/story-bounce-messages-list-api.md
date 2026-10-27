---
id: 6
type: story
title: "Bounce messages list API"
parent: epic-bounce-processing
covers: [R1]
after: [3, 4]
hitl: false
risk: medium
refined: true
---

# Bounce messages list API

## Description

Expose ingested bounce events as an operator-facing **Bounce Messages** list API so the SPA (and Attention deep links) can show rows with extracted error, hard/soft class, and correlated vs uncorrelated — not the Bounce Processing Log (poll/process activity). Realizes PRD **FR-25** visibility and **FR-26** “Bounce list UI shows extracted error per row,” forge **Bounce list UI**, and Clearwater mock `page-bounces`.

Today `bounce_events` are persisted by poll/correlate/process but there is **no list endpoint**; the nav **Bounce Log** page is a stub (FE walkthrough FE-5.6).

## Requirements

1. **Index newest first**
   **Given** bounce events exist in `bounce_events`
   **When** an authorized caller requests `GET /api/bounce-events` (or equivalent under `/api/bounces`)
   **Then** rows return newest `received_at` first
   **And** each row includes at least: id, receivedAt, recipient email (when known), campaign id + subject (when correlated), classification (hard/soft/unknown), extracted error text, correlated flag (or status CORRELATED / UNCORRELATED / INGESTED / PROCESSED), member id when known

2. **Extracted error**
   **Given** a correlated or classifiable bounce
   **When** the list (or detail) is read
   **Then** the row shows an **extracted error** string suitable for operators (FR-26 / forge)
   **And** if no durable `error_text` column exists yet, this story adds persistence at correlate (or derive once and store) so the UI does not re-parse raw MIME on every index load

3. **Admin sees all**
   **Given** an Admin
   **When** they call the list API
   **Then** all bounce events in retention are visible

4. **List manager scoped**
   **Given** a List manager
   **When** they call the list API
   **Then** only bounces whose recipient is a Member of a List they manage are returned (FR-25)
   **And** List senders receive 403 (or empty forbidden) — same as other bounce operator surfaces

5. **Optional filters**
   **Given** query params for correlated | uncorrelated | hard | soft (and All)
   **When** the caller filters
   **Then** the result set matches the Clearwater toolbar options
   **And** uncorrelated rows remain listed (never hidden); Attention remains the human-action inbox, not a second store

6. **Detail for Attention**
   **Given** an Attention item for an uncorrelated bounce
   **When** the client opens the bounce by id
   **Then** `GET …/{id}` returns enough fields to show the same columns plus safe diagnostics (no need to dump full raw bytes in the index payload)

## Boundaries

- Must not change correlation ladder, disable streaks, or bounce-process algorithm (stories 3–4).
- Must not replace Bounce Processing Log (`activity_logs` kind BOUNCE).
- Out: TTL purge jobs (FR-26 TTLs may already exist or stay Parameters-only until a retention story); full raw MIME download UI.
- Non-bounce mailbox noise discarded at ingest stays discarded (FR-25).

## References

- prd — `prd-java-list-mailer.md` FR-25, FR-26 (bounce list UI / extracted error); FR-8 Attention links into bounce log
- forge — `forge-java-list-mailer.md` Bounce list UI + Admin / List manager visibility
- ux — Clearwater mock `#page-bounces` columns: When, Email, Campaign, Class, Error, Correlated?
- parent — `epic-bounce-processing.md` R1
- code — `BounceEventEntity`, `BounceCorrelationService`, stub `BounceLogPage`

## Notes

- Decision: 2026-10-27 — User: rename Bounce Log → Bounce Messages and show message list; stories created from FR-25/26 + mock (API first).
- Gap: walkthrough FE-5.6 called Bounce Log a stub with no backend list API.
