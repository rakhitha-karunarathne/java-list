CREATE TABLE activity_logs (
  id CHAR(36) NOT NULL PRIMARY KEY,
  kind VARCHAR(16) NOT NULL,
  event VARCHAR(32) NOT NULL,
  message VARCHAR(1024) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_activity_logs_kind_created ON activity_logs (kind, created_at);
