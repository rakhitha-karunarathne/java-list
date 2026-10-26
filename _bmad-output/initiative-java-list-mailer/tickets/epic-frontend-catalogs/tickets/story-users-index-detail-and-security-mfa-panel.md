---
id: 4
type: story
title: "Users index detail and Security MFA panel"
parent: epic-frontend-catalogs
covers: [R6]
after: [3]
hitl: true
risk: high
refined: true
---

# Users index detail and Security MFA panel

## Description

Admin **Users** index → User detail (email, role, enabled, list grants). **Security** panel on detail only: MFA enroll QR, verify, backup codes once, unenroll — never a sidebar MFA destination.

## Requirements

1. **Users index (R6)**
   **Given** Admin opens Users
   **When** index loads
   **Then** operators are listed with role/enabled
   **And** create user uses existing Admin API

2. **User detail (R6, C8)**
   **Given** Admin opens a user row
   **When** detail loads
   **Then** role/enabled/grants are editable per API rules
   **And** detail is not a primary sidebar item

3. **Security MFA panel (R6)**
   **Given** User detail Security section
   **When** Admin (or self per API) enrolls MFA
   **Then** QR + verify + backup codes display once
   **And** unenroll works when API allows
   **And** no separate MFA sidebar category exists

4. **Last Admin guard (R6)**
   **Given** API rejects demoting/disabling last Admin
   **When** UI attempts that action
   **Then** error is shown clearly without corrupting local state

## Boundaries

- Consume `/api/users/**` and MFA endpoints only.
- Out: List manager self-service profile beyond what API allows.

## References

- parent — epic-frontend-catalogs.md
- ux — EXPERIENCE MFA Security panel; Flow 2

## Notes

- Decision: 2026-10-26 — Frontend catalogs epic; HITL for MFA UX.
