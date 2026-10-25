CREATE TABLE sending_paths (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  host VARCHAR(255) NOT NULL,
  port INT NOT NULL,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  from_email VARCHAR(320) NOT NULL,
  from_name VARCHAR(120) NOT NULL,
  use_tls TINYINT(1) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_sending_paths_name UNIQUE (name)
);

CREATE TABLE catalog_references (
  id CHAR(36) NOT NULL PRIMARY KEY,
  target_type VARCHAR(32) NOT NULL,
  target_id CHAR(36) NOT NULL,
  source_type VARCHAR(32) NOT NULL,
  source_id CHAR(36) NOT NULL,
  label VARCHAR(255) NOT NULL,
  CONSTRAINT uq_catalog_references UNIQUE (target_type, target_id, source_type, source_id)
);

CREATE INDEX idx_catalog_references_target ON catalog_references (target_type, target_id);
