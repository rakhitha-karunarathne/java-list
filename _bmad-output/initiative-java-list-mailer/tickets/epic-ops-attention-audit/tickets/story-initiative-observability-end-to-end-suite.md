---
id: 6
type: story
title: "Initiative observability end-to-end suite"
parent: epic-ops-attention-audit
covers: [R1, R2, R3]
after: [5]
hitl: false
risk: medium
refined: true
---

# Initiative observability end-to-end suite

## Description

Closing **test-profile suite** for epic-ops-attention-audit and cross-epic observability hooks: prove **ops error → Admin Attention**, **Audit** contains **Campaign status** events plus representative **unsub/bounce** events from upstream epics, and **retention** purge jobs honor **Parameters** (90d ops / 365d audit) in dry-run or shortened-window mode. Acts as regression gate before initiative sign-off on R8, R25, and R33.

## Requirements

1. **Ops failure → Attention** (R2, R1)
   **Given** test profile with GreenMail unavailable or rejecting for one send attempt
   **When** the suite triggers queue or test send failure
   **Then** Ops Logs contains a send failure row
   **And** Admin Attention shows a highlighted ops-error item; List manager session does not

2. **Bounce poll ops row** (R2)
   **Given** bounce poll hooks from epic-bounce-processing
   **When** the suite runs one poll tick against fixture IMAP
   **Then** Ops Logs includes a bounce poll entry
   **And** suite does not assert correlation or disable side effects (C3)

3. **MTS poll ops row** (R2)
   **Given** MTS poll hooks from epic-mail-to-send
   **When** the suite runs one MTS poll tick
   **Then** Ops Logs includes an MTS poll entry

4. **Campaign status Audit trail** (R3)
   **Given** a Campaign driven through Queue → Sending → Stop → Requeue → Finished in test
   **When** the suite queries Audit
   **Then** rows exist for Queue, Stop, Requeue, and Finished in order
   **And** no raw secrets in returned payloads (C1)

5. **Finished Requeue list-add Audit** (R3)
   **Given** a Finished Campaign and an additional List in test fixtures
   **When** the suite adds the List and Requeues
   **Then** Audit contains list added and subsequent Requeue rows

6. **Unsub and bounce Audit samples** (R3)
   **Given** HTTPS unsub and at least one bounce-driven disable from prior epics
   **When** those flows run as part of suite setup
   **Then** Audit contains corresponding rows without token or credential plaintext
   **And** suite does not redefine unsub or bounce rules (C3)

7. **Ops retention dry-run** (R2, C2)
   **Given** Ops Logs rows older than a test-shortened ops retention Parameter
   **When** the ops retention job runs in test mode
   **Then** expired ops rows are purged and in-window rows remain
   **And** default product seed remains 90 days when Parameter reset (C2)

8. **Audit retention dry-run** (R3, C2)
   **Given** Audit rows older than a test-shortened audit retention Parameter
   **When** the audit retention job runs in test mode
   **Then** expired audit rows are purged and in-window rows remain
   **And** default product seed remains 365 days when Parameter reset (C2)

9. **Suite gate** (R1, R2, R3)
   **Given** clean test database and documented Gradle/Maven test target for this suite
   **When** CI or local test profile runs the observability end-to-end suite
   **Then** all scenarios pass
   **And** failure output names which requirement (R1/R2/R3) failed

## Boundaries

- Must not change: production default profile schedules or retention seeds except via existing Parameters UI.
- Must not replace: story-level unit tests; integrated GreenMail/IMAP fixtures only.
- Out: full Clearwater UI automation unless already in repo test harness (IA verified in story 4).

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-ops-attention-audit/epic-ops-attention-audit.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-15, AD-16)

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
