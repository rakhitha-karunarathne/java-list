---
id: 2
type: story
title: "phpMyAdmin private and GreenMail test profile"
parent: epic-platform-kernel
covers: [R3]
after: [1]
hitl: false
risk: medium
---

# phpMyAdmin private and GreenMail test profile

## Description

Default Compose includes phpMyAdmin on tools/private only; profile test adds GreenMail; app can point SMTP/IMAP stubs at it.

## Requirements

Verify: phpMyAdmin not on public compose ports; compose --profile test starts GreenMail; app config documents pointing adaptors at it.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-platform-kernel/epic-platform-kernel.md
