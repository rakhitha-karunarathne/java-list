---
id: 10
type: story
title: "My Account self-service (password and MFA)"
parent: epic-frontend-catalogs
covers: [R6]
after: [9]
hitl: false
risk: medium
refined: true
status: done
---

# My Account self-service (password and MFA)

## Description

Logged-in users manage their own account on **My Account**: change password in one card, MFA in the next. MFA leaves User detail. Users & Grants stays Admin-only. Reach My Account from the header user id and from a nav item pinned at the bottom of the sidebar (every role).

## Requirements

1. **Password section**
   **Given** any signed-in user opens My Account
   **When** the page loads
   **Then** the first card is Password (current, new, confirm)
   **And** a wrong current password or a mismatch is rejected
   **And** success stores a new hash and clears the form

2. **MFA section**
   **Given** My Account
   **When** rendered
   **Then** the MFA enroll / confirm / backup codes / unenroll UI is its own card under Password
   **And** User detail no longer contains that Security card

3. **Reachability**
   **Given** any role
   **When** they use the shell
   **Then** the header user id (email) navigates to My Account
   **And** **My Account** is a nav item at the bottom of the sidebar
   **And** Users & Grants remains Admin-only

## Boundaries

- In: `POST /api/me/password`, My Account page, header link, bottom nav, User detail without MFA, EXPERIENCE / DESIGN.
- Out: Admin resetting another user's password; revoking existing JWTs; changing email or role on My Account.

## References

- EXPERIENCE — My Account IA, MFA panel, page sections
- parent — epic-frontend-catalogs.md

## Notes

- Decision: 2026-10-26 — self-service password + MFA live on My Account, not User detail.

## Dev Agent Record

### Completion Notes

- `POST /api/me/password` checks the current password, rejects a blank or unchanged new password, and saves a new hash. Any authenticated role (not Admin-only).
- My Account = Password card then Security (MFA) card. User detail is Title · Account · List Grants.
- Header email and the sidebar footer item both open My Account.

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| medium | Spring problem JSON puts the reason in `detail`, so the SPA showed a status code | Fixed — `apiJson` reads `detail` when `message` is absent |
| low | Whitespace-only new password | Fixed — server `hasText` and client trim check |
| info | Existing JWT stays valid until expiry after a password change | Out of scope — stateless sessions, no revocation store |
