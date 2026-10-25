---
id: 5
type: story
title: "MFA mandatory Parameter enforcement"
parent: epic-users-access
covers: [R1, R3]
after: [4]
hitl: false
risk: medium
---

# MFA mandatory Parameter enforcement

## Description

Login reads Parameters MFA-mandatory (seed default off; Admin UI in catalogs epic). When on, require TOTP or force enroll; MFA secrets never in Parameters.

## Requirements

Verify: Flipping the Parameter seed/value on forces enroll/challenge; secrets not in Parameters store.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-users-access/epic-users-access.md
