ALTER TABLE bounce_mailboxes
  ADD COLUMN last_polling_update TIMESTAMP(6) NULL,
  ADD COLUMN last_poll_ok BOOLEAN NULL,
  ADD COLUMN polling BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE mts_inboxes
  ADD COLUMN last_polling_update TIMESTAMP(6) NULL,
  ADD COLUMN last_poll_ok BOOLEAN NULL,
  ADD COLUMN polling BOOLEAN NOT NULL DEFAULT FALSE;

-- Old Bounce/MTS log rows used mailbox name, not id.
DELETE FROM activity_logs WHERE kind IN ('BOUNCE', 'MTS');
