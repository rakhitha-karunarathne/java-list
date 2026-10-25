---
title: 'Compose MariaDB Boot Flyway tracer'
type: 'feature'
ticket: '1'
created: '2026-10-25'
status: done
baseline_revision: '1ef3538c383ab408f4b0ead31c43967886c80f43'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick']
review_loop_iteration: 0
context:
  - _bmad-output/initiative-java-list-mailer/tickets/epic-platform-kernel/epic-platform-kernel.md
  - _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The repo has no runnable app or database stack; epic platform needs a tracer through Maven Boot, Flyway, and Compose MariaDB.

**Approach:** Add a single-module Spring Boot 4.1.2 / Java 21 app under `mailer/` with Flyway + `ddl-auto=validate`, MariaDB as a Compose service, and an app container whose health proves the stack.

</frozen-after-approval>

## Implementation Notes

Oneshot: greenfield scaffold per architecture stack pin table; no React, phpMyAdmin, GreenMail, or JWT (later stories).
Used `spring-boot-starter-flyway` (Boot 4); raw `flyway-core` alone did not auto-migrate.
App image installs `curl` for Compose healthcheck; probe matches top-level `"status":"UP"`.

## Plan Change Log

## Review Triage Log

- 2026-10-25 quick: healthcheck wget/grep UP → **high/patch** fixed (curl + exact status). README status contradiction → **medium/patch** fixed. Empty-entity validate → **false** (valid for tracer). multi-jar COPY → **low** rejected after pinning exact jar name.

## Verification

**Commands:**
- `mvn -f mailer/pom.xml -q -DskipTests package` -- expected: BUILD SUCCESS
- `docker compose up -d --build` -- expected: app + mariadb healthy; curl health OK
