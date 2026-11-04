---
id: 10
type: story
title: "Password change requires MFA when enrolled"
parent: epic-users-access
covers: [R1, R3]
after: [4]
hitl: false
risk: medium
refined: true
---

# Password change requires MFA when enrolled

## Description

If the operator has MFA enabled, changing password on My Account must include a valid TOTP or backup code. Without MFA enrolled, password change stays current-password-only. Backup codes remain the recovery path for a lost authenticator.

## Requirements

1. **Given** MFA is enabled  
   **When** POST `/api/me/password` without a valid MFA code  
   **Then** the password is not changed (unauthorized)

2. **Given** MFA is enabled  
   **When** POST `/api/me/password` with current password and a valid TOTP or backup code  
   **Then** the password is changed

3. **Given** MFA is not enabled  
   **When** POST `/api/me/password` with current and new password (no MFA code)  
   **Then** the password is changed as today

4. **Given** My Account and MFA enabled  
   **Then** the password form shows an MFA code field

## Boundaries

- In: `POST /api/me/password` + My Account password UI; `/api/me` exposes whether MFA is enabled  
- Out: Multi-device MFA; Admin MFA reset; requiring MFA for time-zone or other My Account fields
