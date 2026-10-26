---
id: 4
type: story
title: "Ops Attention Audit Clearwater polish"
parent: epic-frontend-ops-mts
covers: [R4]
after: [epic-frontend-shell]
hitl: true
risk: medium
refined: true
---

# Ops Attention Audit Clearwater polish

## Description

Replace tracer Ops/Attention/Audit views with Clearwater tables: Ops index → detail; Attention with ops-error highlight; Audit read-only. Role rules: Admin all; LM scoped; List sender no Attention/Audit.

## Requirements

1. **Ops Logs**
   **Given** Admin opens Ops Logs
   **When** they filter category and open a row
   **Then** detail opens without ops-log detail as sidebar item

2. **Attention highlight**
   **Given** OPS_ERROR Attention items
   **When** Admin views Attention
   **Then** warning highlight treatment applies
   **And** ack does not delete Ops Logs history

3. **LM / sender**
   **Given** List manager vs List sender
   **When** each opens Attention/Audit
   **Then** LM is scoped; sender is hidden/forbidden

4. **Deep links**
   **Given** Attention row with list/campaign reference when API provides it
   **When** operator follows link
   **Then** target detail opens; sidebar stays Attention or index context

## Boundaries

- No new ops categories.
- Out: push notifications.

## References

- parent — epic-frontend-ops-mts.md
- tickets — epic-ops-attention-audit

## Notes

- Decision: 2026-10-26 — Frontend ops/MTS epic; HITL for Attention chrome.
