---
id: 4
type: story
title: "TOTP enroll unenroll and login challenge"
parent: epic-users-access
covers: [R1, R3]
after: [1]
hitl: false
risk: high
---

# TOTP enroll unenroll and login challenge

## Description

Operators enroll/unenroll authenticator MFA with backup codes; login challenges TOTP when enrolled.

## Requirements

Verify: Enrolled user must pass TOTP (or one-time backup code) after password; unenroll stops challenge.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-users-access/epic-users-access.md
