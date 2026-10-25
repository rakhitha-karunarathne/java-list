---
id: 1
type: story
title: "Install Admin and real JWT auth"
parent: epic-users-access
covers: [R1, R3]
after: [epic-platform-kernel]
hitl: true
risk: high
---

# Install Admin and real JWT auth

## Description

Fresh install seeds initial Admin; user store with email+password; JWT Bearer replaces platform stub for /api.

## Requirements

Verify: Fresh compose/install yields Admin; email login returns JWT; stub env-only path retired for operator API.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-users-access/epic-users-access.md
