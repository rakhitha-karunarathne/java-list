ALTER TABLE send_queue
  ADD COLUMN correlation_token CHAR(36) NULL,
  ADD COLUMN verp_rcpt VARCHAR(320) NULL,
  ADD COLUMN subject VARCHAR(255) NULL;

CREATE UNIQUE INDEX uq_send_queue_correlation_token ON send_queue (correlation_token);

ALTER TABLE bounce_events
  ADD COLUMN member_id CHAR(36) NULL,
  ADD COLUMN send_queue_id CHAR(36) NULL,
  ADD COLUMN classification VARCHAR(16) NULL,
  ADD COLUMN correlation_signal VARCHAR(32) NULL;
