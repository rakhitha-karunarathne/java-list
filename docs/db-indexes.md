# Database indexes (hot paths)

Flyway owns schema indexes. When you add a repository method that filters or joins on a non-PK column in a growing table, add a matching index in the same change (or the next Flyway version).

## Already covered (pre-V45)

| Area | Index |
| --- | --- |
| Bounce pending poll | `bounce_events (status)`, `(mailbox_id, received_at)` |
| Correlation token / pixel / unsub | unique on `send_queue.correlation_token`, `pixel_token`, `unsub_token` |
| Send pacing | `send_queue (status, sent_at)`, `(member_id, sent_at)` |
| Ops / audit / activity / MTS drop | created_at (+ category/list/member) composites |

## V45 additions (regular correlate / process / join)

| Query / join | Index |
| --- | --- |
| Bounce correlate `findByVerpRcpt` | unique `send_queue (verp_rcpt)` |
| Bounce correlate `findBySmtpMessageId` | unique `send_queue (smtp_message_id)` |
| Bounce correlate subject fallback | `send_queue (status, subject)` (CI collation; not `lower()`) |
| Bounce process sends for member | `send_queue (member_id, status)` |
| Bounce by queue row / member cleanup | `bounce_events (send_queue_id)`, `(member_id, status)` |
| Mailbox status counts | `bounce_events (mailbox_id, status)` |
| Bounce messages UI order | `bounce_events (received_at)` |
| Campaigns by status; list reverse FKs | `campaigns (status)`; `campaign_lists` / `list_members` / `user_list_grants` / `mts_sender_list_grants` by `list_id` or `member_id` |
| MTS `existsByEmail` | `mts_authorized_senders (email)` |
| Attention by list | `attention_items (open_flag, list_id, created_at)` |
| Secrets key version counts | `secrets (key_version)` |

Nullable unique columns (`verp_rcpt`, `smtp_message_id`) allow many SQL `NULL`s in MariaDB. Do **not** store empty string — writers normalize blank → `NULL` (only one `''` would be allowed under UNIQUE).
