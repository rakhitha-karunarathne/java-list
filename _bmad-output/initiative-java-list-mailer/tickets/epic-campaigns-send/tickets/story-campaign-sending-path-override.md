---
id: 14
type: story
title: "Campaign sending path override"
parent: epic-campaigns-send
covers: [R18]
after: [1]
hitl: false
risk: medium
refined: true
---

# Campaign sending path override

## Description

Compose / Draft edit can optionally select a **Sending Path** for the Campaign. When set, queue and test-send use that path for every recipient and **override** the first selected List’s sending path. When unset, behavior stays: first ordered List’s path (R18 / FR-16).

## Requirements

1. **Optional field**
   **Given** Compose or Draft edit
   **When** the operator picks a Sending Path (or clears it)
   **Then** `campaigns.sending_path_id` stores the choice (nullable)
   **And** campaign detail / API expose `sendingPathId` (null = use first list)

2. **Queue and test-send**
   **Given** a Draft with `sendingPathId` set
   **When** Queue or Test send runs
   **Then** every send-queue row (and test SMTP) uses that path id
   **And** if `sendingPathId` is null, use the first ordered List’s path as today

3. **Catalog Used By**
   **Given** a Campaign references a Sending Path
   **When** Admin views that path’s Used By / tries delete
   **Then** the Campaign appears as a reference and delete stays blocked while referenced

4. **UI**
   **Given** Compose and Campaign detail (read-only display when not Draft)
   **When** paths are listed
   **Then** operator can select “Default (first list)” or a concrete path name
   **And** EXPERIENCE notes that first-list path is the default unless Campaign overrides

## Boundaries

- Must not change multi-list dedupe or list membership rules.
- Does not change Notice sending path.
- Requeue of Stopped/Finished keeps the Campaign’s stored path (edit only in Draft).
- Out: per-recipient path; List path UI changes.

## References

- prd — FR-16 / R18 first-list path (default when override absent)
- parent — `epic-campaigns-send.md`
- related — `story-sending-path-return-path.md` (envelope Return-Path on the chosen path)
- code — `CampaignSendService.queueAndSend` / `testSend`, `CampaignsController`, Compose UI

## Notes

- Decision: 2026-10-27 — User: document first; optional Campaign sending path overrides List path when defined.
