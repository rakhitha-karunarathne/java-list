ALTER TABLE users
  ADD COLUMN totp_secret VARCHAR(64) NULL,
  ADD COLUMN totp_enabled TINYINT(1) NOT NULL DEFAULT 0;

CREATE TABLE user_backup_codes (
  id CHAR(36) NOT NULL PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  code_hash VARCHAR(100) NOT NULL,
  used_at TIMESTAMP(3) NULL,
  CONSTRAINT fk_user_backup_codes_user FOREIGN KEY (user_id) REFERENCES users (id)
);
