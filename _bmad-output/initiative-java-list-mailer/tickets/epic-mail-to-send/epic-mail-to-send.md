---
type: epic
title: "Mail-to-send"
parent: initiative-java-list-mailer
covers: [R29, R30, R31, R32]
sources: []
companions: []
after: []
refined: false
risk: high
---

# Mail-to-send

## Description

IMAP mail-to-send inboxes, authorized senders, mappings, From-domain issues, auth/spam/DMARC outcome matrix, drop log and global blocks. Authorized path creates campaigns; strangers stay silent/logged.

## Outcome

Ingested mail becomes campaigns for authorized senders without courtesy rejects to strangers — initiative Done when #2.

## Done when

1. MTS index + inbox detail; mappings; substitute From domain; footer override.
2. From-domain Issues hold or delete-and-notify (all-or-nothing).
3. Auth then spam-header; DMARC fail = reject treatment; outcome matrix honored.
4. Drop log + global blocks; auto-block sender-only after N/day.

## Boundaries

- Reuses mailbox-query from bounce epic; does not re-own bounce correlation.
- Campaign entity remains campaigns slice; MTS enqueues via ports.
- Catalog Used By must include MTS mappings (R9).

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R29–R32
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-12, AD-16 send-to-mail

## Notes

- Waits on epic-campaigns-send because: campaigns and smtp-adapter.
- Waits on epic-bounce-processing because: mailbox-query utility.
