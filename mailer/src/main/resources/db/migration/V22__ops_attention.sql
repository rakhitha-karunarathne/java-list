CREATE TABLE ops_logs (
  id CHAR(36) NOT NULL PRIMARY KEY,
  category VARCHAR(64) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  message VARCHAR(1024) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_ops_logs_created ON ops_logs (created_at);
CREATE INDEX idx_ops_logs_category ON ops_logs (category, created_at);

CREATE TABLE attention_items (
  id CHAR(36) NOT NULL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  detail VARCHAR(1024) NOT NULL,
  kind VARCHAR(32) NOT NULL,
  list_id CHAR(36) NULL,
  open_flag TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_attention_open ON attention_items (open_flag, created_at);
