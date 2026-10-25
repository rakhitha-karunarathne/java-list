CREATE TABLE lists (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  sending_path_id CHAR(36) NOT NULL,
  footer_id CHAR(36) NULL,
  spam_threshold DECIMAL(6, 3) NULL,
  payload_override VARCHAR(64) NULL,
  image_ttl_override_hours INT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_lists_sending_path FOREIGN KEY (sending_path_id) REFERENCES sending_paths (id),
  CONSTRAINT fk_lists_footer FOREIGN KEY (footer_id) REFERENCES footers (id)
);
