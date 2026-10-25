---
title: 'TOTP enroll unenroll and login challenge'
type: 'feature'
ticket: '4'
created: '2026-10-25'
status: done
baseline_revision: '2754cbf'
route: 'oneshot'
route_source: 'pinned'
risk: 'high'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick-self', 'bugbot']
review_loop_iteration: 1
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Operators need optional authenticator MFA with backup codes and a login challenge when enrolled.

**Approach:** Store TOTP secret + hashed backup codes on the user; setup/confirm/unenroll under /api/me/mfa; login returns short-lived mfaToken then /api/auth/mfa.

</frozen-after-approval>

## Implementation Notes

MFA-purpose JWTs are rejected by EnabledUserFilter for /api access. Single-use mfaToken via jti store; atomic backup-code mark-used.

## Review Triage Log

- 2026-10-25 bugbot: multi-use mfaToken → jti consume; backup race → conditional update.
- 2026-10-25 smoke: enroll/confirm/challenge/unenroll ok.
