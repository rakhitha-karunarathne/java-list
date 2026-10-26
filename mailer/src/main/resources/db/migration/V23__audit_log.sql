CREATE TABLE audit_log (
  id CHAR(36) NOT NULL PRIMARY KEY,
  action VARCHAR(64) NOT NULL,
  detail VARCHAR(1024) NOT NULL,
  actor VARCHAR(320) NULL,
  list_id CHAR(36) NULL,
  member_id CHAR(36) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_audit_created ON audit_log (created_at);
CREATE INDEX idx_audit_list ON audit_log (list_id, created_at);
CREATE INDEX idx_audit_member ON audit_log (member_id, created_at);
