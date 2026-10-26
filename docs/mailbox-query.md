# mailbox-query (IMAP utility)

Shared by **bounce** and **mail-to-send**. Package: `com.listmailer.mailbox` implementing `MailboxPort`.

## Contract

```text
MailboxPort.poll(MailboxConn, MessageHandler)
```

- Opens **one** IMAP folder, walks messages **one at a time** (no full-mailbox body list).
- Skips already `SEEN` / `DELETED` messages.
- Handler returns `true` → mark `SEEN`; `false` → leave for re-read.
- Failures throw `MailboxQueryException` (auth, host, timeout) — caller owns Ops/Attention.
- **Callers** serialize inbox polls (one active IMAP poll per poller type on one JVM). Bounce and MTS each enforce their own non-overlap.

`MailboxConn`: host, port, username, password, useSsl, folder (default `INBOX`).

`RawMessage`: messageId, subject, from, recipients, headers map, raw bytes.

No bounce/campaign/MTS domain types in this package.

Stable for epic-mail-to-send: `MailboxPort.poll(MailboxConn, MessageHandler)` lifecycle unchanged after bounce epic refactor sweep (story 7.5).
