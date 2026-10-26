CREATE TABLE mts_drop_log (
  id CHAR(36) NOT NULL PRIMARY KEY,
  inbox_id CHAR(36) NULL,
  sender_email VARCHAR(320) NULL,
  reason VARCHAR(128) NOT NULL,
  counted_for_auto_block TINYINT(1) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  resolved TINYINT(1) NOT NULL DEFAULT 0
);

CREATE INDEX idx_mts_drop_inbox ON mts_drop_log (inbox_id, created_at);
CREATE INDEX idx_mts_drop_sender ON mts_drop_log (sender_email, created_at);

CREATE TABLE mts_blocks (
  id CHAR(36) NOT NULL PRIMARY KEY,
  kind VARCHAR(16) NOT NULL,
  value VARCHAR(320) NOT NULL,
  source VARCHAR(32) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_mts_blocks_kind_value UNIQUE (kind, value)
);
