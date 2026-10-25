---
type: epic
title: "Campaigns & send path"
parent: initiative-java-list-mailer
covers: [R18, R19, R20, R21, R22, R23, R24]
sources: []
companions: []
after: []
refined: false
risk: high
---

# Campaigns & send path

## Description

Multi-list campaigns: compose (HTML/text, images, attachments, preview, test send), blob-storage `/dl`, system send-queue with throttle, open pixel + reporting. Uses smtp-adapter and GreenMail in test.

## Outcome

Admin can send a multi-list campaign end-to-end and see open stats — initiative Done when #1.

## Done when

1. Compose + campaigns create multi-list sends with deduped queue rows and first-list sending path.
2. Test send ignores bounce-disable; failures land in ops/Attention hooks.
3. Images stored as DB blobs; public `/dl/<campaignGuid>/<file>`; cascade delete with campaign.
4. Send-queue processes sequentially with system throttle; pixel async; reporting shows unique/total opens.

## Boundaries

- Unsubscribe/consent links are epic-unsubscribe-consent (tokens issued here must match AD-17/18).
- Bounce and MTS are later epics; this epic owns send-queue correlation fields bounce will consume.
- V2 extract marks: tracking + send-queue packages (AD-22).

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R18–R24
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-11–AD-13, AD-16–AD-18, AD-20, AD-22–AD-23, AD-26

## Notes

- Waits on epic-lists-members because: lists and members.
