# C8 detail-not-nav verification (epic 9 story 4)

Primary sidebar destinations (Admin shell in `mailer/frontend`):

| Allowed (index / tool) | Not primary sidebar |
| --- | --- |
| Home, Compose, Parameters | Campaign / List / Member **detail** |
| Ops Logs (index → row detail) | Ops log detail as nav item |
| Attention, Audit | User / Sending path / Footer / Bounce mailbox **detail** |
| Inboxes & mappings (MTS index → inbox detail) | MTS inbox detail as nav item |

Walkthrough: open each index; open a row; confirm sidebar labels unchanged (no new detail destination).
List sender: Attention forbidden; no detail entities as primary nav.
