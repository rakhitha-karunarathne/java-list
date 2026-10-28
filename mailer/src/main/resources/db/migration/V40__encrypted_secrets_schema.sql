CREATE TABLE secrets (
  id CHAR(36) NOT NULL PRIMARY KEY,
  key_version INT NOT NULL,
  ciphertext VARBINARY(2048) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

ALTER TABLE sending_paths
  ADD COLUMN password_secret_id CHAR(36) NULL;

ALTER TABLE bounce_mailboxes
  ADD COLUMN password_secret_id CHAR(36) NULL;

ALTER TABLE mts_inboxes
  ADD COLUMN password_secret_id CHAR(36) NULL;

ALTER TABLE users
  ADD COLUMN totp_secret_id CHAR(36) NULL;
