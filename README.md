# java-list

java-list is an email newsletter and mailing-list application (Java / Spring Boot + React), in the same vein as [phpList](https://www.phplist.org/): lists, campaigns, send, unsubscribe, and delivery ops.

App code lives in `mailer/` (backend + SPA). Orchestration is root `compose.yaml`.

## Status

Working V1 mailer on Compose: MariaDB + Flyway, JWT admin UI, catalogs (sending paths, bounce mailboxes, footers), lists/members, campaigns (compose → queue → send → opens), unsubscribe/consent, bounce poll/process, Mail-to-Send (MTS), ops/audit attention, and encrypted reversible secrets (SMTP/IMAP passwords, MFA seed) with versioned keys.

Default login after empty DB install: `admin@example.com` / `changeme` (override with `MAILER_BOOTSTRAP_*`).

Stack pins: **Java 21**, **Spring Boot 4.1.2**, **MariaDB 12.3.3**.

## How to run

Requires **Docker**. For host builds/tests: **JDK 21+** and **Maven 3.9+**.

### App (usual path)

```bash
git clone https://github.com/rakhitha-karunarathne/java-list.git
cd java-list
docker compose up -d --build
```

- UI / API: http://127.0.0.1:8080  
- Health: http://127.0.0.1:8080/actuator/health  

Rebuild/restart after code changes:

```bash
docker compose up -d --build --force-recreate app
```

Logs: `docker compose logs -f app`

### Optional Compose profiles

```bash
# phpMyAdmin on http://127.0.0.1:8081 only (do not publish on a public edge)
docker compose --profile tools up -d

# GreenMail SMTP 3025 / IMAP 3143 for local mail tests
docker compose --profile test up -d
```

Default shape is **app + MariaDB** only (no GreenMail).

### Secrets / env (Compose)

Set via `compose.yaml` or the environment (never as UI Parameters):

| Variable | Role |
| --- | --- |
| `MAILER_SECRETS_KEY_1` | AES key material (≥32 bytes) for secrets table version 1 |
| `MAILER_SECRETS_MIGRATE_ON_STARTUP` | `true` → rewrap all secrets to highest configured key on boot |
| `MAILER_JWT_SECRET` | JWT signing (≥32 bytes) |
| `MAILER_OPAQUE_TOKEN_SECRET` | Unsubscribe/approve tokens (≥32 bytes) |
| `MAILER_BOOTSTRAP_EMAIL` / `MAILER_BOOTSTRAP_PASSWORD` | First Admin seed when `users` is empty |
| `MAILER_PUBLIC_BASE_URL` | Public HTTPS base for links (`https://…`) |

Key rotation: add another `{version, key}` under `mailer.secrets.keys` in `application.yml`, keep older versions until rewrap finishes (Parameters → Migrate, or `migrate-on-startup`).

### Tests

```bash
cd mailer
mvn test
```

Needs Docker for Testcontainers MariaDB (and embedded GreenMail for campaign/MTS tracers).

### Host run (without rebuilding the image)

Start MariaDB (`docker compose up -d mariadb`), then from `mailer/`:

```bash
mvn spring-boot:run
```

Uses `jdbc:mariadb://localhost:3306/mailer` by default.

## Docs

- [docs/user-guide.md](docs/user-guide.md) — how to use the operator UI  
- [docs/deploy.md](docs/deploy.md) — HTTPS, secrets placement, Compose shapes, campaign E2E gate  
- Planning / tickets under `_bmad-output/initiative-java-list-mailer/`
