---
id: 7
type: story
title: "MTS sender-level Substitute From and Sending Path"
parent: epic-mail-to-send
covers: [R1, R29]
after: [1, 2]
hitl: false
risk: high
refined: true
---

# MTS sender-level Substitute From and Sending Path

## Description

Move **Substitute From** from per list-grant to **per authorized sender**. Add optional **Sending Path** on the same authorized sender. When the sender has a sending path, MTS-created Campaigns set that path (campaign override). From-domain rules apply between Substitute From and the path used for send.

## Requirements

1. **Sender fields**
   **Given** an authorized sender on an MTS inbox
   **When** Admin opens the sender page
   **Then** they can set optional Substitute From and optional Sending Path (sender-level, not per List)
   **And** List grants keep only Granted + Footer (per List)

2. **Domain match on save**
   **Given** both Substitute From and Sending Path are set on the sender
   **When** Admin saves
   **Then** the domain of Substitute From must equal the From-email domain of that Sending Path (case-insensitive)
   **And** mismatch returns 400

3. **Ingest domain check**
   **Given** Substitute From is set
   **When** authorized mail is accepted
   **Then** if the sender has a Sending Path, validate Substitute From against that path’s From domain
   **Else** validate against each granted List’s Sending Path From domain (all-or-nothing; open From-domain Issue on mismatch; no Campaign)

4. **Campaign path**
   **Given** the sender has a Sending Path
   **When** MTS creates a Campaign
   **Then** `campaigns.sending_path_id` is set to that path (Used By / queue override)
   **And** when unset, Campaign path stays null → first List path as today

5. **Migration**
   **Given** existing grant-level `substitute_from` values
   **When** Flyway runs
   **Then** the first non-empty substitute per sender (by grant position) is copied onto the sender
   **And** grant column `substitute_from` is dropped

## Boundaries

- Must not change footer-per-list grants, auth/spam matrix, or Notice path.
- Out: applying Substitute From as the SMTP From header if not already wired (domain gate + storage only unless already present).
- Out: per-list substitute (removed).

## References

- prd — FR-28 / FR-29 substitute From domain rule; campaign path override (related)
- parent — `epic-mail-to-send.md`
- related — `story-campaign-sending-path-override.md`, `story-sending-path-return-path.md`
- code — `MtsAuthorizedSenderEntity`, `MtsSenderListGrantEntity`, `MtsFromDomainService`, `MtsCampaignFactory`, `MtsAdminController`, OpsMtsPages sender UI

## Notes

- Decision: 2026-10-27 — User: document first; Substitute From and Sending Path per approved sender; path set on resulting campaign; From domains must match.
