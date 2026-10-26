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
- SMTP/IMAP mailbox credentials (catalog fields later; still not Parameters)
- Opaque-token encryption keys (later)

## Compose shapes

| Shape | Command | Services |
| --- | --- | --- |
| Default / prod-shaped | `docker compose up -d --build` | app + MariaDB |
| Tools | `docker compose --profile tools up -d` | + phpMyAdmin on `127.0.0.1:8081` only |
| Test mail | `docker compose --profile test up -d` | + GreenMail (SMTP 3025, IMAP 3143) |

Default Compose **does not** include GreenMail.

## Campaigns end-to-end suite (story 5.10)

Requires Docker (Testcontainers MariaDB) and runs an embedded GreenMail SMTP:

```bash
cd mailer
mvn test -Dtest=CampaignsE2ETest
```

Failures are named by epic requirement (`R1/R8`, `R7`, `R8 Stop…`, `R8 Finished…`). Full `mvn test` also runs this suite. There is no separate CI workflow in-repo yet — treat this Maven target as the gate when Docker is available.
