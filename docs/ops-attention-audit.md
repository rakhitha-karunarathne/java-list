# Ops Logs, Attention, Audit

| Surface | API | Retention Parameter |
| --- | --- | --- |
| Ops Logs | `/api/ops-logs` (ADMIN) | `ops.log.retention.days` (90) |
| Attention | `/api/attention` | — (ack closes item; Ops history remains) |
| Audit | `/api/audit` | `audit.retention.days` (365) |

Roles: Admin sees all Attention/Audit; List manager scoped by managed Lists; List sender has neither Attention nor Audit.
