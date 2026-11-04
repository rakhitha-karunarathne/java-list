# Deploy notes (platform)

## HTTPS / TLS

- Operator UI, unsubscribe, pixel, `/dl`, and List-Unsubscribe links must be **HTTPS only**.
- Cloudflare (or equivalent) origin TLS: **Full** or **Full Strict** only.
- **Flexible is unsupported** — do not terminate HTTPS at the edge while talking HTTP to origin.

Public base URL is a Parameter seed (`public.base.url`), set via `MAILER_PUBLIC_BASE_URL` / `mailer.public-base-url`. Value must start with `https://`.

## Secrets

Store in env or a secret file — **never** as Parameters:

- `MAILER_JWT_SECRET`
- `MAILER_BOOTSTRAP_PASSWORD` (seeds initial Admin once when `users` is empty; login uses the DB hash after that)
- `MAILER_OPAQUE_TOKEN_SECRET` (≥32 bytes; AES-GCM for `/unsubscribe` and `/approve` tokens)
- SMTP/IMAP mailbox credentials (catalog fields later; still not Parameters)

## Compose shapes

| Shape | Command | Services |
| --- | --- | --- |
| Default / prod-shaped | `docker compose up -d --build` | app + MariaDB |
| Tools | `docker compose --profile tools up -d` | + phpMyAdmin on `127.0.0.1:8081` only |
| Test mail | `docker compose --profile test up -d` | + GreenMail (SMTP 3025, IMAP 3143) |

Default Compose **does not** include GreenMail.

## Published app image (no database)

`mailer/Dockerfile` builds **only the mailer app**. Bring your own MariaDB (11+ / 12 recommended). Flyway migrates the schema on startup.

```bash
# From repo root
docker build -t ghcr.io/<owner>/java-list:latest -f mailer/Dockerfile mailer
docker push ghcr.io/<owner>/java-list:latest
```

Required env when running the image:

| Variable | Role |
| --- | --- |
| `SPRING_DATASOURCE_URL` | e.g. `jdbc:mariadb://host:3306/mailer` |
| `SPRING_DATASOURCE_USERNAME` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `MAILER_JWT_SECRET` | ≥32 bytes |
| `MAILER_OPAQUE_TOKEN_SECRET` | ≥32 bytes |
| `MAILER_SECRETS_KEY_1` | ≥32 bytes (secrets table version 1) |
| `MAILER_SECRETS_KEY_2` … `_1024` | Optional key versions (env only; see below) |
| `MAILER_PUBLIC_BASE_URL` | Public `https://…` base for links |

Optional: `MAILER_BOOTSTRAP_EMAIL` / `MAILER_BOOTSTRAP_PASSWORD`, `SERVER_PORT`, `JAVA_OPTS`, `MAILER_SECRETS_ACTIVE_KEY_VERSION`, `MAILER_SECRETS_MIGRATE_ON_STARTUP`.

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://db.example:3306/mailer \
  -e SPRING_DATASOURCE_USERNAME=mailer \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  -e MAILER_JWT_SECRET='…' \
  -e MAILER_OPAQUE_TOKEN_SECRET='…' \
  -e MAILER_SECRETS_KEY_1='…' \
  -e MAILER_PUBLIC_BASE_URL=https://lists.example.com \
  ghcr.io/<owner>/java-list:latest
```

Create an empty MariaDB database (and user with rights on it) before first start. Do not embed MariaDB in the published app image.

### Secret key versions (env / Compose only)

Master keys are **not** listed in `application.yml`. Set numbered env vars:

| Env | Meaning |
| --- | --- |
| `MAILER_SECRETS_KEY_1` | Version 1 material (≥32 bytes) |
| `MAILER_SECRETS_KEY_n` | Version *n* (scanned through **1024**; gaps OK) |
| `MAILER_SECRETS_ACTIVE_KEY_VERSION` | Optional; encrypt + migrate/rewrap target. Default = highest `KEY_n` present |

Keep every `MAILER_SECRETS_KEY_*` that rows still reference until after a successful rewrap onto the active version.

**Rotate in Compose**

1. Add `MAILER_SECRETS_KEY_2: "your-new-key-at-least-32-bytes!!!!"` (keep `_1`).
2. Set `MAILER_SECRETS_ACTIVE_KEY_VERSION: "2"` (optional if `_2` is already the highest key).
3. Set `MAILER_SECRETS_MIGRATE_ON_STARTUP: "true"` (or migrate later in UI: Parameters → Migrate secrets).
4. `docker compose up -d --force-recreate app`.
5. After migrate, you may remove retired keys from Compose **only** when no row still uses that `key_version`.

If no `MAILER_SECRETS_KEY_*` is set, the app logs a warning and uses a built-in version-1 **dev** default (not for production).

## Campaigns end-to-end suite (story 5.10)

Requires Docker (Testcontainers MariaDB) and runs an embedded GreenMail SMTP:

```bash
cd mailer
mvn test -Dtest=CampaignsE2ETest
```

Failures are named by epic requirement (`R1/R8`, `R7`, `R8 Stop…`, `R8 Finished…`). Full `mvn test` also runs this suite. There is no separate CI workflow in-repo yet — treat this Maven target as the gate when Docker is available.
