CREATE TABLE campaigns (
  id CHAR(36) NOT NULL PRIMARY KEY,
  subject VARCHAR(255) NOT NULL,
  html_body MEDIUMTEXT NOT NULL,
  text_body MEDIUMTEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE campaign_lists (
  campaign_id CHAR(36) NOT NULL,
  list_id CHAR(36) NOT NULL,
  PRIMARY KEY (campaign_id, list_id),
  CONSTRAINT fk_campaign_lists_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
  CONSTRAINT fk_campaign_lists_list FOREIGN KEY (list_id) REFERENCES lists (id)
);

CREATE TABLE send_queue (
  id CHAR(36) NOT NULL PRIMARY KEY,
  campaign_id CHAR(36) NOT NULL,
  member_id CHAR(36) NOT NULL,
  email VARCHAR(320) NOT NULL,
  sending_path_id CHAR(36) NOT NULL,
  status VARCHAR(32) NOT NULL,
  CONSTRAINT fk_send_queue_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
  CONSTRAINT fk_send_queue_member FOREIGN KEY (member_id) REFERENCES members (id),
  CONSTRAINT uq_send_queue_campaign_member UNIQUE (campaign_id, member_id)
);
