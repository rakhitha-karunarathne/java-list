---
id: 4
type: story
title: "Daily hard soft bounce process"
parent: epic-bounce-processing
covers: [R2, R5]
after: [3]
hitl: false
risk: high
refined: true
---

# Daily hard soft bounce process

## Description

Scheduled **daily bounce-process** (default 02:00 UTC per Parameters) walks each email with unprocessed correlated bounce activity: separate **hard** and **soft** consecutive counts, **holdback** on the newest sends, and disable when either class limit is reached. Sets Member global **Disabled** with **disable_reason** (hard vs soft limit and latest error); **bounce disable ≠ list unsubscribe**. Soft bounce limit is independent after initial seed (2× hard); changing hard does not auto-change soft. When VERP is on, correlation for process input already prefers VERP paths from ingest (R5).

## Requirements

1. **Daily schedule runs bounce-process** (R2)
   **Given** Parameters define bounce-process schedule (default daily 02:00 UTC in test can be shortened)
   **When** the job fires with pending correlated bounces
   **Then** bounce-process executes system-wide (not per-mailbox poll)
   **And** poll schedule continues independently (ingest vs process separation)

2. **Separate hard and soft walks** (R2)
   **Given** a member email with alternating hard and soft correlated bounces on different sends ordered by send date
   **When** bounce-process walks sends outer-joined to bounces oldest → newest
   **Then** hard consecutive count and soft consecutive count are computed independently
   **And** reaching hard limit disables even if soft count is below soft limit, and vice versa

3. **Send with no correlated bounce resets that class streak** (R2)
   **Given** consecutive hard bounces on older sends and a newer send with no correlated bounce at process time (holdback excepted)
   **When** the walk reaches that send
   **Then** hard consecutive count resets to zero for subsequent sends
   **And** soft streak is unaffected unless soft bounce pattern dictates

4. **Holdback protects recent sends** (R2)
   **Given** holdback multiplier × class limit newest messages (default multiplier 2)
   **When** limit is not yet hit
   **Then** those newest bounces/sends are not marked processed in a way that hides recent activity prematurely
   **And** holdback applies per class (hard vs soft)

5. **Hard limit disables Member** (R2)
   **Given** hard bounce limit default 3 (Parameter) and three consecutive hard correlated bounces on sends for one member
   **When** bounce-process completes
   **Then** Member is globally Disabled with disable_reason indicating hard bounce limit
   **And** Campaign send-queue skips that Member on future passes

6. **Soft limit disables Member** (R2)
   **Given** soft bounce limit default 6 and six consecutive soft correlated bounces
   **When** bounce-process completes
   **Then** Member is globally Disabled with disable_reason indicating soft bounce limit
   **And** disable is independent of hard limit state (either limit suffices)

7. **Disable marks bounces processed** (R2)
   **Given** disable triggered by hitting a class limit
   **When** the job finishes for that email
   **Then** all bounces for that email are marked processed per addendum algorithm
   **And** Ops Logs reflect disable outcome

8. **Below limit marks processable rows** (R2)
   **Given** correlated bounces below both limits
   **When** bounce-process runs
   **Then** processable bounce rows are marked processed respecting holdback
   **And** Member stays Enabled

9. **Uncorrelated never affects this walk** (R2, R4)
   **Given** uncorrelated bounce records present in the system
   **When** bounce-process runs
   **Then** they are excluded from streak counting and disable decisions
   **And** they remain handled per story 3 (log + Attention only)

10. **Bounce disable ≠ unsubscribe** (R2, epic C3)
    **Given** a Member disabled by bounce-process
    **When** Member detail is viewed
    **Then** disable_reason shows bounce limit, not list unsubscribe
    **And** list membership/unsubscribe state is unchanged solely due to bounce disable

11. **Soft limit independent after init** (R2)
    **Given** soft limit was initially seeded at 2× hard (e.g. 6 when hard is 3)
    **When** an operator later lowers hard limit via Parameters
    **Then** soft limit does not auto-sync to the new hard value
    **And** soft walk continues using the configured soft limit until explicitly changed

## Boundaries

- Must not disable on uncorrelated bounces or use them in streak math (C7; story 3).
- Must not reset streaks on SMTP “SEND OK” alone (addendum; only absence of correlated bounce at process time with holdback rules).
- Must not implement: MTS spam/auth rules or Notice sending path behavior.
- Out: payload oversize incrementing auto-block (initiative addendum rejected path).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-bounce-processing/epic-bounce-processing.md
- addendum — _bmad-output/initiative-java-list-mailer/prd-java-list-mailer/addendum.md (bounce-process algorithm, defaults)
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-25)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Extra check beyond criteria: Run process twice same day idempotently — no double-disable or duplicate Attention storms.
