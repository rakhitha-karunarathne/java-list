ALTER TABLE campaigns
  ADD COLUMN sending_path_id CHAR(36) NULL,
  ADD CONSTRAINT fk_campaigns_sending_path
    FOREIGN KEY (sending_path_id) REFERENCES sending_paths (id);
