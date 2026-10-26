CREATE TABLE mts_from_domain_issues (
  id CHAR(36) NOT NULL PRIMARY KEY,
  inbox_id CHAR(36) NOT NULL,
  mapping_id CHAR(36) NULL,
  sender_email VARCHAR(320) NOT NULL,
  subject VARCHAR(998) NULL,
  status VARCHAR(32) NOT NULL,
  resolution VARCHAR(32) NULL,
  detail VARCHAR(512) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_mts_fdi_inbox FOREIGN KEY (inbox_id) REFERENCES mts_inboxes (id) ON DELETE CASCADE
);

CREATE INDEX idx_mts_fdi_status ON mts_from_domain_issues (status);
