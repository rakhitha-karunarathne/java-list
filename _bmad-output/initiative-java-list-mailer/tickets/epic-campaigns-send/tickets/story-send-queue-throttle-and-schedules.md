---
id: 6
type: story
title: "Send-queue throttle and schedules"
parent: epic-campaigns-send
covers: [R4, R9]
after: [1]
hitl: false
risk: high
refined: true
---

# Send-queue throttle and schedules

## Description

Lists and Campaign actions only enqueue work; a system send-queue processor delivers mail through smtp-adapter under a single system-wide throttle. Scheduled processors may run concurrent job classes, but the same schedule class must not overlap in-process on V1’s single app instance.

## Requirements

1. **Lists enqueue only; processor sends** (R4)
   **Given** a Campaign in Queued with pending send-queue rows
   **When** the operator does not manually invoke SMTP from List or Campaign UI
   **Then** rows move to sent only via the system send-queue processor
   **And** Campaign status transitions to Sending when processing starts and to Finished after the current pass’s final recipient

2. **System-wide throttle** (R4)
   **Given** a Parameter (or equivalent tunable) sets a low max sends per time window and many queue rows exist
   **When** the processor runs against GreenMail (test profile)
   **Then** observed send timestamps respect the throttle cap (no burst beyond configured rate)
   **And** lowering the cap during Sending slows subsequent sends without duplicating rows

3. **No Notice path on Campaign queue** (R4, epic C4)
   **Given** a Notice sending path exists in catalogs
   **When** a Campaign queue row is processed
   **Then** SMTP uses the Campaign’s List-derived Sending path only
   **And** Notice path is never selected for Campaign queue processing

4. **Concurrent schedule classes allowed** (R9)
   **Given** two different scheduled job types both enqueue or drain work (e.g. send-queue vs another campaign-related schedule stub if present)
   **When** their triggers fire close together on one JVM
   **Then** both may run without global deadlock
   **And** each completes its own responsibility

5. **Same schedule class non-overlap** (R9)
   **Given** a long-running send-queue schedule tick is still in progress
   **When** the next tick for the same schedule class would start
   **Then** the second tick is skipped or deferred (no double-processing of the same rows in parallel)
   **And** no ShedLock or multi-instance worker assumption is introduced (epic C2)

6. **Processor idle behavior** (R4)
   **Given** no pending send-queue rows
   **When** the processor schedule fires
   **Then** it exits cleanly without error spam in Ops Logs
   **And** Campaigns already Finished or Stopped are unchanged

## Boundaries

- Must not change: dedupe rules, Stop holding unsent rows, or Requeue enqueue semantics (story 4).
- Must not change: open-pixel async handling or blob `/dl` serving (stories 5 and 8).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md (for UI stories)
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md (compose/campaign flows)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Under load test, verify unsent rows are not marked sent twice after a simulated slow tick.
