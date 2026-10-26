---
id: 2
type: story
title: "Sign-in and MFA step-up UI"
parent: epic-frontend-shell
covers: [R3]
after: [1]
hitl: false
risk: medium
refined: true
---

# Sign-in and MFA step-up UI

## Description

Operator sign-in card: email + password; when API requires MFA, challenge for authenticator or backup code on the same flow; persist JWT in sessionStorage; logout clears session.

## Requirements

1. **Email + password (R3)**
   **Given** an unauthenticated visitor
   **When** they submit valid email and password for a non-MFA user
   **Then** they receive a session token stored in sessionStorage
   **And** they land in the authenticated shell

2. **MFA step-up (R3)**
   **Given** login returns MFA required
   **When** the operator enters a valid TOTP (or backup code path if API supports)
   **Then** the full session token is stored and shell loads
   **And** copy says “Authenticator code” / “Backup code” (not SMS OTP)

3. **Login failure (R3)**
   **Given** invalid credentials
   **When** submit completes
   **Then** a field-linked or form error shows without leaking whether email exists beyond API behavior
   **And** no token is stored

4. **Logout (R3)**
   **Given** a signed-in session
   **When** the operator signs out
   **Then** sessionStorage token is removed
   **And** the sign-in surface is shown

5. **MFA mandatory unenrolled (R3)**
   **Given** Parameters MFA mandatory and user not enrolled
   **When** password succeeds
   **Then** UI follows API enrollmentRequired path (block or force enroll messaging)
   **And** Security enroll remains on User detail (no separate MFA sidebar item)

## Boundaries

- Consume existing `/api/auth/login` and `/api/auth/mfa` only.
- User Security enroll UI may be stubbed until Users frontend epic if API already supports enroll elsewhere.
- Out: OAuth/social login; SMS OTP.

## References

- parent — epic-frontend-shell.md
- ux — EXPERIENCE Flow 2; Voice and Tone

## Notes

- Decision: 2026-10-26 — Created with frontend epic slice.
