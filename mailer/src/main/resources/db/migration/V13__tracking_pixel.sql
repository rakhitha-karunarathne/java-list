ALTER TABLE send_queue
  ADD COLUMN pixel_token CHAR(36) NULL,
  ADD COLUMN unsub_token CHAR(36) NULL,
  ADD COLUMN smtp_message_id VARCHAR(255) NULL;

CREATE UNIQUE INDEX uq_send_queue_pixel_token ON send_queue (pixel_token);
CREATE UNIQUE INDEX uq_send_queue_unsub_token ON send_queue (unsub_token);

CREATE TABLE campaign_opens (
  id CHAR(36) NOT NULL PRIMARY KEY,
  campaign_id CHAR(36) NOT NULL,
  member_id CHAR(36) NOT NULL,
  send_queue_id CHAR(36) NOT NULL,
  opened_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_campaign_opens_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id) ON DELETE CASCADE
);

CREATE INDEX idx_campaign_opens_campaign ON campaign_opens (campaign_id);
CREATE INDEX idx_campaign_opens_member ON campaign_opens (campaign_id, member_id);
