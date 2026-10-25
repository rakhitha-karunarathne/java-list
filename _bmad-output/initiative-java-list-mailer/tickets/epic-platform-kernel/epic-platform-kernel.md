---
type: epic
title: "Platform & kernel"
parent: initiative-java-list-mailer
covers: [R34]
sources:
  - _bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md
companions:
  - _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md
after: []
refined: true
risk: high
---

# Platform & kernel

## Description

Stands up the V1 deployable baseline: Maven single module, Spring Boot + Flyway, React-in-jar shell, Docker Compose (app, MariaDB, phpMyAdmin; GreenMail on test profile), and the thin kernel (ports, JWT stub, opaque-token helper scaffold, GUID conventions).

## Outcome

Operators (and builders) can run the app stack locally and hit an authenticated shell over HTTPS-ready config, with public base URL wired as a Parameter seed.

## Requirements

R1 (parent R34): Public and operator surfaces are configured for HTTPS-only use; Public base URL is available as a Parameters seed; Cloudflare Flexible is documented as unsupported (Full / Full Strict only).

R2 (parent C1, C2; architecture AD-7): Relational store is MariaDB as a Compose service; the app image does not embed MariaDB.

R3 (parent C2; architecture AD-7, AD-27): Default Compose runs app + MariaDB + phpMyAdmin (phpMyAdmin bound private / profile `tools`, not on public Cloudflare path). Compose profile `test` adds GreenMail; the app can point SMTP/IMAP adaptors at it.

R4 (parent C3; architecture AD-8, AD-21): Maven single-module Spring Boot app; Flyway versioned SQL runs on startup; Hibernate `ddl-auto` is `validate` or `none`.

R5 (parent C3; architecture AD-9): React SPA is packaged into Boot static resources; JSON API under `/api`; non-API routes fall back to `index.html`.

R6 (architecture AD-2, AD-6, AD-18, AD-19, AD-24): Thin kernel scaffold is present — security/JWT Bearer path, opaque-token helper stub, GUID id convention, Parameters port stub, mailbox and SMTP port stubs, scheduler host stub. Operator JWT is stored in sessionStorage on the SPA stub.

R7 (architecture AD-15 seed): Parameters port exposes at least Public base URL for later Admin UI; signing secrets and mailbox credentials are never stored as Parameters (env/secret file only).

## Constraints

C1 (parent C1): MariaDB, not MySQL.

C2 (parent C2; architecture AD-7): Single app container for app + schedulers; MariaDB and phpMyAdmin are Compose services as above.

C3 (parent C3): Java Spring Boot + schedules; React frontend.

C4 (parent C4): Flexible TLS unsupported; member/operator links HTTPS-only.

C5 (parent C6): Signing secrets and path/mailbox credentials never as Parameters plaintext or raw in Audit (Audit UI is later; secret placement is owned here).

C6 (architecture AD-10, AD-13): V1 assumes one app instance; no ShedLock/file/DB schedule locks; do not raise a global multi-thread scheduler pool without revisiting overlap rules.

## Done when

1. `docker compose up` runs app + MariaDB; phpMyAdmin is private-only.
2. Compose profile `test` brings GreenMail; app can be pointed at it.
3. Flyway migrates on startup; React SPA loads from the jar.
4. JWT sessionStorage login stub works against the kernel security path.
5. Production-shaped Compose (no GreenMail) is documented.
6. A builder can run a smoke check: open SPA, authenticate with bootstrap stub credentials from env, hit a protected `/api` ping.

## Boundaries

- Does not implement catalog CRUD, campaigns, full RBAC grants, or Parameters Admin UI beyond the port seed (epic-users-access / epic-catalogs-parameters).
- Does not implement real SMTP/IMAP business flows — only port stubs and GreenMail wiring for later epics.
- Touch point: architecture spine AD-7, AD-8, AD-9, AD-21, AD-27 — owner: this epic.
- Must not change: MariaDB-only (C1); secrets not in Parameters (parent C6).
- Out: CI/CD pipeline beyond what a story needs to verify Compose locally; multi-module Maven; production Cloudflare tunnel automation.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, Requirements R34, Constraints C1–C4, C6
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-2, AD-6–AD-10, AD-13, AD-15, AD-18–AD-19, AD-21, AD-24, AD-27

## Notes

- Decision: 2026-10-25 — Opening platform-baseline epic (initiative slice).
- Decision: 2026-10-25 — Refine: parent R34 + architecture companions are sufficient; no epic-local PRD/UX. Bootstrap JWT stub uses env credentials for smoke only; full user store is epic-users-access. CI deferred unless a story needs it.
- Assumption: Stack versions follow the architecture spine pin table until a story bumps them.
