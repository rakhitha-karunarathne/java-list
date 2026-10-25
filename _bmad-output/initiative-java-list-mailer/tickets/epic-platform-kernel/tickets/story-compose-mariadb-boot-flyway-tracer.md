---
id: 1
type: story
title: "Compose MariaDB Boot Flyway tracer"
parent: epic-platform-kernel
covers: [R2, R4]
after: []
hitl: true
risk: high
---

# Compose MariaDB Boot Flyway tracer

## Description

Maven single-module Spring Boot app starts with Flyway against MariaDB in Docker Compose; ddl-auto validate/none; empty schema migrates.

## Requirements

Verify: docker compose up brings app + MariaDB; Flyway runs; app health responds.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-platform-kernel/epic-platform-kernel.md
