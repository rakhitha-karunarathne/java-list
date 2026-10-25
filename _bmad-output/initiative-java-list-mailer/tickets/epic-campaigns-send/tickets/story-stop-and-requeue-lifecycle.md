---
id: 4
type: story
title: "Stop and Requeue lifecycle"
parent: epic-campaigns-send
covers: [R35]
after: [1]
hitl: false
risk: high
---

# Stop and Requeue lifecycle

## Description

Stop from Draft/Queued/Sending → Stopped; Requeue from Stopped; Finished Requeue only after Add List; new-list addresses excluding already-sent.

## Requirements

Verify: Stop mid-flight leaves Stopped; Requeue from Stopped resumes; Finished Requeue gated on Add List.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/epic-campaigns-send.md
