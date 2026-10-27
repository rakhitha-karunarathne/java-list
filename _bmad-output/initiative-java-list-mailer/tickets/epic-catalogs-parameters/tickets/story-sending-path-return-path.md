---
id: 7
type: story
title: "Sending path return-path email"
parent: epic-catalogs-parameters
covers: [R1]
after: [1]
hitl: false
risk: medium
refined: true
---

# Sending path return-path email

## Description

Admin can set an optional **Return-Path email** on a Sending Path. When present, outbound SMTP uses it as the envelope reverse-path (MAIL FROM). Header **From** stays `from_email` / `from_name`. When absent, envelope behavior stays as today (From address).

## Requirements

1. **Catalog field**
   **Given** Admin creates or edits a Sending Path
   **When** they enter Return-Path email (optional)
   **Then** it persists on `sending_paths` and appears on create/edit forms and detail API

2. **Domain match**
   **Given** Return-Path is non-empty
   **When** the path is saved
   **Then** the domain of Return-Path **must equal** the domain of From email (case-insensitive)
   **And** mismatch returns 400 with a clear message (From must match the domain in the return-path / envelope sender)

3. **Send uses Return-Path**
   **Given** a path with Return-Path set
   **When** any mail is sent on that path (campaign queue, test send, notice mail)
   **Then** SMTP envelope MAIL FROM is the Return-Path address
   **And** the visible From header remains the path From name/email

4. **VERP domain**
   **Given** VERP is enabled and Return-Path is set
   **When** a queue row builds `verp_rcpt`
   **Then** the domain part comes from Return-Path (not From), so bounce correlation aligns with the envelope sender

## Boundaries

- Must not change bounce mailbox ingest or correlation ladder beyond VERP domain source.
- Empty / null Return-Path = legacy behavior (no envelope override).
- Out: per-message Return-Path overrides; UI outside Sending Paths form.

## References

- parent — `epic-catalogs-parameters.md` R1 (sending paths)
- code — `SendingPathEntity`, `SendingPathsController`, `JavaMailSmtpAdapter`, `CampaignSendService` VERP block
- related — campaign sending-path override story (epic-campaigns-send)

## Notes

- Decision: 2026-10-27 — User: document first; Return-Path on sending path; use when present; From domain must match Return-Path domain when set.
