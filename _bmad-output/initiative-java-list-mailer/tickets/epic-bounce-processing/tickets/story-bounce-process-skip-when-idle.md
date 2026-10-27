---
id: 7
type: story
title: "Bounce process skips when no new correlated bounces"
parent: epic-bounce-processing
covers: [R2]
after: [4]
hitl: false
risk: medium
refined: true
---

# Bounce process skips when no new correlated bounces

## Description

**Bounce processing** (daily job and manual Run — not mailbox polling) only does work when the set of **CORRELATED** bounce events has changed since the last process run. Idle ticks (same leftover holdback rows, or no correlated rows) skip: no START/END Processing Log noise, no member walk.

## Requirements

1. **Idle skip**
   **Given** bounce-process runs (schedule or `POST /api/bounce-process/run`)
   **When** there are no CORRELATED bounce events, or the set of CORRELATED event ids is unchanged since the last completed process run
   **Then** the walk does not run
   **And** no Bounce Processing Log START/END is written for that tick
   **And** the manual API returns a clear skipped message (not “already running”)

2. **New work runs**
   **Given** at least one CORRELATED bounce id that was not in the set recorded after the previous process run (new correlation, or first run with correlated rows)
   **When** bounce-process starts
   **Then** it runs the existing hard/soft/holdback walk as today
   **And** after completion it records the current CORRELATED id set for the next idle check

3. **Polling unchanged**
   **Given** bounce mailbox poll / correlate
   **When** this story ships
   **Then** poll and correlate behavior are unchanged (correlate already idles when no INGESTED)

## Boundaries

- In: `BounceProcessService` / controller message; idle check without loading bounce payloads.
- Out: Changing disable/holdback rules; persisting the idle watermark across JVM restart (in-memory is enough; after restart one re-walk of leftover CORRELATED is allowed).

## References

- parent — epic-bounce-processing.md
- related — story-daily-hard-soft-bounce-process.md
- code — `BounceProcessService`, `BounceCorrelationService` idle pattern for INGESTED

## Notes

- Decision: 2026-10-27 — User: process (not poll) only if new bounces since last run; document → implement → review → commit → rebuild.

## Dev Agent Record

### Completion Notes

- Idle when CORRELATED id set is empty or equals the watermark from the last **successful** walk (input ∩ remaining; mid-run arrivals stay unseen).
- Manual `POST /api/bounce-process/run` returns skipped message; no START/END on idle. Poll/correlate unchanged.

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| medium | Watermark in `finally` skipped retry after partial failure | Fixed — stamp only after successful walk |
| medium | Mid-run new CORRELATED ids included in watermark | Fixed — stamp `input ∩ remaining` only |
| medium | Holding `running` during idle → false ALREADY_RUNNING | Fixed — idle check before CAS |
| medium | Weak tests (return 0 without proving skip) | Fixed — activity/findByStatus verifies + new-id + controller |
| low | Limit/holdback param change won’t re-walk leftovers | Deferred — story Out; need new id or restart |
