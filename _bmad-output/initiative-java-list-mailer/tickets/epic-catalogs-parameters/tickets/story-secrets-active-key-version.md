---
id: 10
type: story
title: "Secrets active key version and 1024 key slots"
parent: epic-catalogs-parameters
covers: [R1]
after: [8]
hitl: false
risk: medium
refined: true
---

# Secrets active key version and 1024 key slots

## Description

Support long-lived deployments: scan `MAILER_SECRETS_KEY_1` … `MAILER_SECRETS_KEY_1024`. New encryption (and migrate/rewrap) uses an explicit **active** key version (`MAILER_SECRETS_ACTIVE_KEY_VERSION` / `mailer.secrets.active-key-version`). If unset, the **highest present** key index remains active (prior behavior). Migrate-on-startup and Admin rewrap move every secret **not** on the active version onto that version (older or newer indices, as long as those keys are still configured). Rows whose `key_version` is above the highest configured key still fail migrate until that key is restored.

## Requirements

1. **Given** keys 1…N in env and no active version set  
   **Then** encrypt/migrate uses max(N)

2. **Given** `MAILER_SECRETS_ACTIVE_KEY_VERSION=k` and `MAILER_SECRETS_KEY_k` is set  
   **Then** new secrets and rewrap target version *k* (even if higher keys exist for decrypt-only)

3. **Given** active version set but that key env is missing  
   **Then** startup fails with a clear error

4. **Given** migrate-on-startup / rewrap  
   **Then** rows with any other configured version move to the active version

## Boundaries

- In: `SecretsKeyEnv` max 1024; active version env/property; crypto current; SecretStore rewrap/outdated  
- Out: Storing master keys in Parameters UI
