ALTER TABLE bounce_mailboxes
  ADD COLUMN last_polled_at TIMESTAMP(3) NULL,
  ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1;

CREATE TABLE bounce_events (
  id CHAR(36) NOT NULL PRIMARY KEY,
  mailbox_id CHAR(36) NOT NULL,
  received_at TIMESTAMP(3) NOT NULL,
  message_id VARCHAR(255) NULL,
  subject VARCHAR(998) NULL,
  from_addr VARCHAR(320) NULL,
  headers_json MEDIUMTEXT NOT NULL,
  raw_bytes LONGBLOB NOT NULL,
  status VARCHAR(32) NOT NULL,
  CONSTRAINT fk_bounce_events_mailbox FOREIGN KEY (mailbox_id) REFERENCES bounce_mailboxes (id)
);

CREATE INDEX idx_bounce_events_status ON bounce_events (status);
CREATE INDEX idx_bounce_events_mailbox ON bounce_events (mailbox_id, received_at);
