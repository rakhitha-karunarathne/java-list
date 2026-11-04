---
id: 8
type: story
title: "MFA enroll TOTP QR code"
parent: epic-users-access
covers: [R1, R3]
after: [4]
hitl: false
risk: low
refined: true
---

# MFA enroll TOTP QR code

## Description

When an operator starts MFA enrollment on **My Account**, show a **QR code** for the `otpauth://` URI so they can scan it into an authenticator app. Keep a manual-entry fallback (secret / URI text). Generate the QR **in the browser** from the existing setup response — do not send the secret to a third-party image service.

## Requirements

1. **Given** Enroll MFA returns `otpAuthUrl`  
   **When** the setup step is shown  
   **Then** a scannable QR image is displayed for that URI

2. **Given** the operator cannot scan  
   **Then** they can still use the shown secret (or URI) for manual entry

3. **Given** confirm or dismiss setup  
   **Then** the QR (and secret) are cleared from the UI

## Boundaries

- In: My Account MFA enroll panel; client-side QR from `otpAuthUrl`  
- Out: Changing TOTP algorithm, server-side QR PNG, third-party QR APIs
