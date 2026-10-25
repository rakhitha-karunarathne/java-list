---
type: epic
title: "Campaigns & send path"
parent: initiative-java-list-mailer
covers: [R18, R19, R20, R21, R22, R23, R24, R35]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md]
after: []
refined: true
risk: high
---

# Campaigns & send path

## Description

Multi-list campaigns: compose (HTML/text, images, attachments, preview, test send), lifecycle (Draft → Queued → Sending → Finished; Stop → Stopped; Requeue from Stopped or from Finished after Add List), blob-storage `/dl`, system send-queue with throttle, open pixel + reporting. Uses smtp-adapter and GreenMail in test.

## Outcome

Admin can send a multi-list campaign end-to-end and see open stats — initiative Done when #1.

## Requirements

R1 (parent R18): Multi-list Campaign; same address → one queue row; uses first selected List’s Sending path.

R2 (parent R19): Text + HTML, images, attachments; compose has HTML/Text preview pane.

R3 (parent R20): Test send from compose to pasted addresses; uses first List’s path; ignores bounce-disable; not campaign sent-stats; failures → Ops Logs / Admin Attention.

R4 (parent R21): System send-queue processor; system-wide throttle only; Lists only enqueue.

R5 (parent R22): Image-fetch Off / Best Effort / Mandatory + SSRF guards; rewritten images CDN TTL after campaign’s most recent Finished (Requeue that finishes again restarts TTL; Stopped does not).

R6 (parent R23): Email payload and stored-image hard uppers; per-List ≤ ceiling; multi-list strictest (TTL longest wins).

R7 (parent R24): Campaign reporting: sent, unique opens (headline), total opens; per-member first open / count; HTML pixel only; signed tokens.

R8 (parent R35): Campaign status is exactly one of Draft · Queued · Sending · Finished · Stopped. Compose until queue → Draft. Queue → Queued; processor starts → Sending; after final recipient of the current pass → Finished. Stop from Draft/Queued/Sending → Stopped (unsent rows held). Stopped may Requeue → Queued (resume unsent + newly added List members not already sent). Finished may Requeue only after at least one additional List is added → Queued; that pass enqueues newly added List addresses only, excluding addresses already sent for this Campaign.

R9 (architecture AD-11, AD-13, AD-20, AD-23): Images as DB blobs under blob-storage; public GET `/dl/<campaignGuid>/…`; cascade delete with campaign; open pixel is async (non-blocking); schedules may run concurrent classes with in-process non-overlap for the same schedule; V1 one app instance.

R10 (architecture AD-17, AD-18): Campaign send issues opaque member tokens and public HTTPS URLs (unsub/pixel shapes) consumed by later epics; correlation fields bounce needs are written on send-queue rows.

## Constraints

C1 (parent C4, C8): Public member links HTTPS-only; Campaign detail is not a primary sidebar destination.

C2 (architecture AD-10, AD-13): No ShedLock; do not raise a global multi-thread scheduler pool without revisiting overlap rules.

C3 (architecture AD-22): tracking + send-queue packages stay extract-marked for V2; V1 keeps them in-process.

C4: Campaign queue never uses the Notice sending path (parent R6 ownership stays catalogs/notice-mail).

C5 (parent R35): Already-sent addresses are never re-sent on Requeue; test sends do not count as sent. Finished Requeue requires a newly added List and targets only that List’s (those Lists’) not-yet-sent addresses.

## Done when

1. Compose + campaigns create multi-list sends with deduped queue rows and first-list sending path.
2. Status Draft → Queued → Sending → Finished; Stop → Stopped; Requeue from Stopped; Requeue from Finished after adding a List (new-list addresses only, exclude already-sent); index/detail show status + Stop/Requeue.
3. Test send ignores bounce-disable; failures land in ops/Attention hooks.
4. Images stored as DB blobs; public `/dl/<campaignGuid>/<file>`; cascade delete with campaign.
5. Send-queue processes with system throttle; pixel async; reporting shows unique/total opens.
6. GreenMail test path can prove enqueue → SMTP → pixel open recorded.

## Boundaries

- Unsubscribe/consent link handlers are epic-unsubscribe-consent (tokens issued here must match AD-17/18).
- Bounce and MTS are later epics; this epic owns send-queue correlation fields bounce will consume.
- Ops/Attention UIs complete in epic-ops-attention-audit; this epic emits the hooks.
- Out: click tracking; object-store blobs; multi-instance queue workers; Finished Requeue without adding a List.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R18–R24, R35, R6, C4, C8
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-11–AD-13, AD-16–AD-18, AD-20, AD-22–AD-23, AD-26
- prd — `_bmad-output/initiative-java-list-mailer/prd-java-list-mailer/prd-java-list-mailer.md`, FR-40

## Notes

- Waits on epic-lists-members because: lists and members.
- Decision: 2026-10-25 — Refine from parent covers + send/blob/pixel ADs; first product demo after this epic (initiative Decision).
- Decision: 2026-10-25 — Adopt R35 campaign states; Queued is distinct from Sending (processor start).
- Decision: 2026-10-25 — Stopped/Finished Requeue rules per updated R35.
- Assumption: Mail-to-send–created Campaigns enter Queued (skip Draft).

- Decision: 2026-10-25 � Stories planned. Tracer: 5.1 Compose Draft queue GreenMail; least-sure next: 5.4 Stop/Requeue. Refactor sweep included. Closing e2e suite after sweep.
