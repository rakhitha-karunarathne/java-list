---
title: 'phpMyAdmin private and GreenMail test profile'
type: 'feature'
ticket: '2'
created: '2026-10-25'
status: done
baseline_revision: 'c7a1ad85ae45f37ec97db47a2b6662e3448d4baa'
route: 'oneshot'
route_source: 'pinned'
risk: 'medium'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self']
review_loop_iteration: 0
context:
  - _bmad-output/initiative-java-list-mailer/tickets/epic-platform-kernel/epic-platform-kernel.md
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Compose lacks private phpMyAdmin and a test-profile GreenMail for SMTP/IMAP adaptors.

**Approach:** Add phpMyAdmin under Compose profile `tools` bound to localhost only; add GreenMail under profile `test`; document app SMTP/IMAP test settings that point at GreenMail.

</frozen-after-approval>

## Implementation Notes

phpMyAdmin: profile `tools`, bind `127.0.0.1:8081`. GreenMail: profile `test`, image 2.1.14, ports 3025/3143. `application-test.yml` holds mailer.smtp/imap host ports for later adaptors.

## Plan Change Log

## Review Triage Log

- 2026-10-25 quick-self: verified compose ps shows 127.0.0.1:8081 for phpMyAdmin; GreenMail up on test profile; default `compose config` omits both. No high findings.

## Verification

**Commands:**
- `docker compose --profile tools config` -- phpMyAdmin ports are 127.0.0.1 only
- `docker compose --profile test up -d greenmail` -- GreenMail starts
