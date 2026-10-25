---
id: 1
type: story
title: "Compose Draft queue GreenMail tracer"
parent: epic-campaigns-send
covers: [R1, R4, R8]
after: [epic-lists-members]
hitl: true
risk: high
---

# Compose Draft queue GreenMail tracer

## Description

Compose creates Draft Campaign; Queue → Queued→Sending→Finished for one List through smtp-adapter + GreenMail; first-list Sending path.

## Requirements

Verify: Operator queues a one-list Campaign; GreenMail receives messages; status ends Finished.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
