---
type: epic
title: "Platform & kernel"
parent: initiative-java-list-mailer
covers: [R34]
sources: []
companions: []
after: []
refined: false
risk: high
---

# Platform & kernel

## Description

Stands up the V1 deployable baseline: Maven single module, Spring Boot + Flyway, React-in-jar shell, Docker Compose (app, MariaDB, phpMyAdmin; GreenMail on test profile), and the thin kernel (ports, JWT stub, opaque-token helper scaffold, GUID conventions).

## Outcome

Operators (and builders) can run the app stack locally and hit an authenticated shell over HTTPS-ready config, with public base URL wired as a Parameter seed.

## Done when

1. `docker compose up` runs app + MariaDB; phpMyAdmin is private-only.
2. Compose profile `test` brings GreenMail; app can be pointed at it.
3. Flyway migrates on startup; React SPA loads from the jar.
4. JWT sessionStorage login stub works against the kernel security path.
5. Production-shaped Compose (no GreenMail) is documented.

## Boundaries

- Does not implement catalog CRUD, campaigns, or full RBAC grants (epic-users-access / catalogs).
- Touch point: architecture spine AD-7, AD-8, AD-9, AD-21, AD-27 — owner: this epic
- Must not change: MariaDB-only (C1); secrets not in Parameters (C6)

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, Requirements R34, Constraints C1–C4, C6
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-7–AD-9, AD-19, AD-21, AD-27

## Notes

- Decision: 2026-10-25 — Opening platform-baseline epic (initiative slice).
