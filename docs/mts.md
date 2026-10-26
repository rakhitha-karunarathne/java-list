# Mail-to-send (MTS)

Admin API under `/api/mts/**` (ADMIN role). Poll uses shared `MailboxPort` (mailbox-query).

| Path | Purpose |
| --- | --- |
| `/api/mts/inboxes` | Index + CRUD |
| `/api/mts/mappings` | Flat sender×list grant rows (inbox detail / tooling; not an index overview) |
| `/api/mts/from-domain-issues` | Issues ≠ Drop log |
| `/api/mts/drop-log` | Drop log (+ `?inboxId=`) |
| `/api/mts/blocks` | Global sender/domain blocks |

Authorized accept → Campaign then `queueAndSend` (Queued). Strangers: silent drop + Drop log; no courtesy reject.
