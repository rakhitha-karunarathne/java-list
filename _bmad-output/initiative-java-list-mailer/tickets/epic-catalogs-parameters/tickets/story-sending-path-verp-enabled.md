---
id: 9
type: story
title: "Sending path VERP enabled"
parent: epic-catalogs-parameters
covers: [R1]
after: [7]
hitl: false
risk: medium
refined: true
---

# Sending path VERP enabled

## Description

Move **VERP Enabled** off global Parameters onto each **Sending Path**. Operators turn it on only when that path’s bounce / Return-Path address accepts `bounce+…` local-parts. When on, outbound mail for that path gets a per-recipient VERP envelope (`verp_rcpt`) and bounce correlation tries VERP matching for those sends. Remove the `verp.enabled` Parameter (migrate prior `true` onto all existing paths).

## Requirements

1. **Given** a Sending Path with VERP Enabled on  
   **When** a campaign message is sent on that path  
   **Then** `send_queue.verp_rcpt` is set and SMTP MAIL FROM uses that VERP address

2. **Given** VERP Enabled off on the path  
   **Then** no VERP envelope is applied for sends on that path

3. **Given** a bounce that contains a VERP address / `bounce+` token for a VERP send  
   **When** correlation runs  
   **Then** the event can correlate via the VERP signal (before generic token)

4. **Given** Parameters Admin  
   **Then** `verp.enabled` is no longer listed; path detail shows the VERP toggle

## Boundaries

- In: `sending_paths.verp_enabled`; send + SMTP envelope; bounce correlation; UI/docs; drop Parameter  
- Out: Changing non-VERP correlation ladder steps beyond gating/use of VERP
