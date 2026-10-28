---
id: 8
type: story
title: "Encrypted secrets store (versioned keys)"
parent: epic-catalogs-parameters
covers: [R1, R2]
after: [1, 2, 7]
hitl: false
risk: high
refined: true
---

# Encrypted secrets store (versioned keys)

## Description

Reversible credentials (Sending Path / Bounce / MTS passwords, MFA TOTP seed) live in a shared **`secrets`** table as AES-GCM ciphertext. Parents hold `*_secret_id`. Login `password_hash` and MFA backup-code hashes stay as today. Master keys are versioned integers in config; **highest version is current**. Admin can **migrate all secrets to the latest key** from System → Parameters.

## Requirements

1. **Store**
   **Given** a reversible secret is saved
   **When** persisted
   **Then** a `secrets` row holds `key_version` + ciphertext
   **And** the parent stores only the secret id
   **And** deleting the parent deletes the secret row

2. **Keys**
   **Given** `mailer.secrets.keys` is a list of `{version, key}` (≥32 bytes each)
   **When** encrypting
   **Then** the highest version is used
   **And** decrypt uses the row’s `key_version` (must still be configured)

3. **Migrate existing**
   **Given** plaintext password / totp_secret columns from before this story
   **When** Flyway runs
   **Then** values are encrypted into `secrets`, FKs set, and plaintext columns dropped

4. **Admin rewrap**
   **Given** Admin on Parameters (System)
   **When** they run “Migrate secrets to latest key”
   **Then** every secret with `key_version < max` is re-encrypted with the current key
   **And** the API reports migrated / already-current / failed counts

5. **Out of scope**
   User password hashes, backup code hashes, JWT/opaque-token secrets, Parameters values.

## Boundaries

- In: `secrets` table, SecretStore, config keys, path/bounce/MTS/TOTP wiring, Flyway migrate, Admin rewrap UI on Parameters.
- Out: AWS Secrets Manager; envelope encryption; returning secrets in APIs.

## References

- parent — epic-catalogs-parameters.md
- code — SendingPath / BounceMailbox / MtsInbox passwords; User totp_secret; Parameters page

## Notes

- Decision: 2026-10-28 — User: shared encrypted secrets table; config key for now; version integers (max = current); Admin migrate button; own slice; migrate existing; review; commit.

## Dev Agent Record

### Completion Notes

- `secrets` table + `SecretStore` (AES-GCM); `mailer.secrets.keys: [{version, key}]` (max version = current).
- Path / bounce / MTS passwords and user TOTP seed use `*_secret_id`; Flyway V40–V41 migrates plaintext and drops old columns.
- Parameters page: Secrets card + migrate-to-latest; `GET/POST /api/secrets/*` Admin-only.

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| medium | Controllers/tests missing SecretStore wiring | Fixed — injected + mocks updated |
| medium | Plaintext must move in same deploy as column drop | Fixed — V41 Java migration encrypts then drops |
| low | E2E testcontainers need Docker | Pre-existing env; unit suite green |
| info | Key still in compose/env | Accepted — AWS later |
