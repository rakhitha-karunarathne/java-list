---
type: epic
title: "Users & access"
parent: initiative-java-list-mailer
covers: [R1, R2]
sources: [_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md]
companions: [_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md]
after: []
refined: true
risk: high
---

# Users & access

## Description

Install creates the initial Admin. Admins create/disable users with local **email** + password (email is the login id); optional TOTP authenticator-app MFA with backup codes; Admin / List manager / List sender roles and per-list manage/send grants enforce scoped access via real JWT. MFA becomes required for all operators when the Parameters flag is on (catalogs epic owns the Parameter; this epic enforces it at login).

## Outcome

Only authorized operators reach operator APIs; list grants take effect immediately when changed; MFA is available without blocking first login unless Parameters require it.

## Requirements

R1 (parent R1): Install yields an initial Admin; Admins create/disable Users with local email + password (email is the login id); optional TOTP authenticator-app MFA (enroll + backup codes); Admin may set a Parameter to make MFA mandatory for all operators; roles Admin / List manager / List sender enforce scoped access.

R2 (parent R2): Per-List manage and/or send grants are edited on User detail; removing a grant takes effect immediately; Admin needs no list grants.

R3 (architecture AD-6, AD-19): Operator API uses JWT Bearer after email + password, and TOTP when the user is enrolled **or** Parameters “MFA mandatory” is on; hashed backup codes; MFA secrets never in Parameters; JWT stored in sessionStorage on the SPA. No SMS/WebAuthn/SSO in V1.

## Constraints

C1 (parent C6): Signing secrets and MFA secrets/backup codes never stored as Parameters plaintext or written raw into Audit.

C2 (architecture AD-6): Member/public links use opaque tokens (later epics), distinct from operator JWT — this epic must not conflate the two mechanisms.

C3 (architecture AD-4): Operator UI talks JSON under `/api`; this epic owns real JWT guards on that surface (replacing the platform stub).

## Done when

1. Fresh install yields a usable Admin.
2. Admin can create/disable users and assign roles.
3. Per-list grants are editable on user detail and apply immediately.
4. JWT auth guards `/api` for real sessions (sessionStorage).
5. Operators can enroll/unenroll TOTP; login challenges TOTP when enrolled or when MFA-mandatory Parameter is on; backup codes work once.

## Boundaries

- Catalogs, lists, and campaigns stay in later epics.
- Parameters Admin UI (including MFA-mandatory flag and its description) is epic-catalogs-parameters; this epic only reads the flag at login.
- Sibling: epic-platform-kernel owns the shell this epic authenticates against.
- Out: SSO, WebAuthn, SMS MFA, multi-tab refresh tokens.

## References

- parent — `_bmad-output/initiative-java-list-mailer/initiative-java-list-mailer.md`, R1–R2, C6
- architecture — `_bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md`, AD-4, AD-6, AD-16 users, AD-19

## Notes

- Waits on epic-platform-kernel because: app shell, MariaDB, JWT stub.
- Decision: 2026-10-25 — Refine from parent R1–R2 + architecture AD-6/AD-19; no epic-local PRD/UX. MFA-mandatory Parameter owned by catalogs; enforcement at login owned here.
- Assumption: Bootstrap Admin from install uses env seed once; thereafter Admin manages users in-app.

- Decision: 2026-10-25 � Stories planned. Tracer: 2.1 Install Admin and real JWT. Refactor sweep included.
