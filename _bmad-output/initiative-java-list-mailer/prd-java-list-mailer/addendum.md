---
title: Java list mailer — PRD addendum
created: 2026-10-25
updated: 2026-10-25
source: forge-java-list-mailer + party-mode
---

# Addendum (not PRD body)

Technical-how, rejected options, and sizing defaults that belong downstream (architecture / ops) or were locked in forge but are not capability statements.

## Stated stack (architecture input)

- Backend: Java, Spring Boot (latest), Spring schedules for periodic work
- Frontend: React
- Database: MySQL
- Delivery: single Docker container
- Plugin architecture: required later; hook list deferred to architecture
- No integrations beyond what the PRD lists for V1

## Origin TLS (ops)

- Cloudflare **Full** (HTTPS to origin; self-signed OK) or **Full (strict)** (Cloudflare Origin CA / trusted cert on container)
- **Flexible** rejected as intended mode
- Open: how origin cert is supplied in the container for Full vs Full Strict (docs/runbook)

## Bounce-process algorithm (implementation spec)

Per email with unprocessed bounce activity:

1. Sends outer-joined to bounce records; order by send date oldest → newest.
2. Walk oldest → newest; count consecutive bounced sends (hard and soft separately).
3. A send with no correlated bounce resets that class’s consecutive count (not “SEND OK”; it is “no bounce linked when the daily job runs,” with holdback protecting recent sends).
4. If count reaches the class limit → disable and mark bounces processed as below.
5. If limit not hit → mark what can be marked processed; holdback: do not mark the newest (holdback_multiplier × class limit) messages.
6. When disabled by hitting the limit: mark all bounces for that email processed.

Correlation signals stored on queue row at send: VERP/Return-Path (when enabled), `X-JavaList-Token`, Message-ID (and other useful standard headers), subject+email fallback.

Open for architecture: which extra standard headers beyond Message-ID to parse in DSNs.

## Default Parameters (product-locked numbers)

| Setting | Default |
|--------|---------|
| Bounce mailbox / mail-to-send poll | 15 minutes (per mailbox) |
| Bounce-process | 02:00 daily, timezone UTC |
| Send queue interval | 15 minutes |
| Message interval | 1 second |
| Messages per run | 900 |
| Message rate | 3600 / hour |
| Hard bounce limit | 3 |
| Soft bounce limit | 6 initially (2× hard); then independent |
| Holdback multiplier | 2 |
| Image fetch timeout | 30s per image |
| Email payload hard upper | 5 MB |
| Stored-image hard upper | 5 MB |
| Campaign TTL default / hard max | 180 / 365 days |
| Bounce log processed / unprocessed TTL | 180 / 365 days |
| Audit retention | 365 days |
| Consent invite expiry | 14 days |
| Auto-block | 10 counted rejects / day (sender only) |
| VERP | off |
| Image-fetch mode | Best Effort |

## Rejected / deferred alternatives

- Cloudflare Flexible
- Soft limit auto-sync when hard changes (rejected after grill; soft independent after init)
- Uncorrelated bounces contributing to disable or streak reset
- mailto: List-Unsubscribe
- POP3
- Courtesy reject to unapproved strangers (backscatter)
- Auto domain block from auto-block counter
- Automatic re-enable from opens
- Unilateral clear unsubscribe

## Competitive landscape (Discovery research digest)

Self-hosted newsletter/list space (2025–2026): **Listmonk** (Go + Postgres, high throughput, modern UI), **Keila** (Elixir, Docker, SMTP + ESP connectors), **Mautic** (full marketing automation), **phpList** (mature PHP/MySQL, slower relative health vs Listmonk). Java list mailer’s wedge is **phpList-like operator behaviors** (mail-to-send, bounce disable with holdback, role-scoped lists, Attention, consent clear) on a **non-PHP** stack — not matching Listmonk’s single-binary positioning or Mautic’s automation breadth.

## Deferred: List public / private (post-V1)

Confirmed **out of V1**. Product intent when built:

| Visibility | Who can add Members | Unsubscribe | Public subscribe page |
|------------|---------------------|-------------|------------------------|
| **Private** | Admins only (not List managers) | Members can still unsubscribe | No |
| **Public** | (operator rules TBD with feature) | Members can unsubscribe | Yes |

V1 ships with no visibility flag; Admin + List manager add (incl. bulk paste) as in FR-9 / FR-35.

## Party / forge provenance

Canonical decisions draft: `_bmad-output/forge-java-list-mailer/forge-java-list-mailer.md` (updated 2026-10-23). PRD Create Fast path ingested that draft as primary input on 2026-10-25.
