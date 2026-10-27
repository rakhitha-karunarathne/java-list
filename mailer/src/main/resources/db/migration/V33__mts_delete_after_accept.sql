ALTER TABLE mts_inboxes
  ADD COLUMN delete_after_accept BOOLEAN NOT NULL DEFAULT TRUE;
