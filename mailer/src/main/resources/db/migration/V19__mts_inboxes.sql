CREATE TABLE mts_inboxes (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  host VARCHAR(255) NOT NULL,
  port INT NOT NULL,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  use_ssl TINYINT(1) NOT NULL,
  poll_interval_minutes INT NOT NULL,
  last_polled_at TIMESTAMP(3) NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_mts_inboxes_name UNIQUE (name)
);

CREATE TABLE mts_authorized_senders (
  inbox_id CHAR(36) NOT NULL,
  email VARCHAR(320) NOT NULL,
  PRIMARY KEY (inbox_id, email),
  CONSTRAINT fk_mts_auth_inbox FOREIGN KEY (inbox_id) REFERENCES mts_inboxes (id) ON DELETE CASCADE
);

CREATE TABLE mts_mappings (
  id CHAR(36) NOT NULL PRIMARY KEY,
  inbox_id CHAR(36) NOT NULL,
  name VARCHAR(120) NOT NULL,
  substitute_from VARCHAR(320) NULL,
  footer_id CHAR(36) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_mts_map_inbox FOREIGN KEY (inbox_id) REFERENCES mts_inboxes (id) ON DELETE CASCADE,
  CONSTRAINT fk_mts_map_footer FOREIGN KEY (footer_id) REFERENCES footers (id)
);

CREATE TABLE mts_mapping_lists (
  mapping_id CHAR(36) NOT NULL,
  list_id CHAR(36) NOT NULL,
  position INT NOT NULL DEFAULT 0,
  PRIMARY KEY (mapping_id, list_id),
  CONSTRAINT fk_mts_ml_map FOREIGN KEY (mapping_id) REFERENCES mts_mappings (id) ON DELETE CASCADE,
  CONSTRAINT fk_mts_ml_list FOREIGN KEY (list_id) REFERENCES lists (id)
);
