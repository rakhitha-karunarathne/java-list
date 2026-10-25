---
type: epic
title: "Unsubscribe & consent"
parent: initiative-java-list-mailer
covers: [R14, R15]
sources: []
companions: []
after: []
refined: false
risk: medium
---

# Unsubscribe & consent

## Description

HTTPS unsubscribe (one URL per campaign send, List-Unsubscribe-Post) and consent clear-unsub via notice-mail. Opaque tokens; audited.

## Outcome

Members can leave lists safely; clear-unsub requires consent email — initiative Done when #4.

## Done when

1. `/unsubscribe/<token>` removes member from all lists on that campaign; List-Unsubscribe-Post works.
2. Consent `/approve/<token>` clears unsub only via Notice path mail; expiry default 14d; audited.
3. notice-mail slice sends consent/system mail using Notice sending path (bypasses bounce-disable).

## Boundaries

- Campaign compose/send remains epic-campaigns-send.
- Does not implement bounce disable (epic-bounce-processing).

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R14–R15, R6
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-17, AD-18, AD-16 unsubscribe/consent/notice-mail

## Notes

- Waits on epic-campaigns-send because: campaign send and opaque tokens.
