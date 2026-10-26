ALTER TABLE campaign_lists
  ADD COLUMN position INT NOT NULL DEFAULT 0;

-- Distinct positions for rows that shared the default after ADD COLUMN.
UPDATE campaign_lists cl
  JOIN (
    SELECT campaign_id, list_id,
           ROW_NUMBER() OVER (PARTITION BY campaign_id ORDER BY list_id) - 1 AS pos
    FROM campaign_lists
  ) ranked ON ranked.campaign_id = cl.campaign_id AND ranked.list_id = cl.list_id
SET cl.position = ranked.pos;

CREATE TABLE campaign_attachments (
  id CHAR(36) NOT NULL PRIMARY KEY,
  campaign_id CHAR(36) NOT NULL,
  filename VARCHAR(255) NOT NULL,
  content_type VARCHAR(127) NOT NULL,
  content LONGBLOB NOT NULL,
  CONSTRAINT fk_campaign_attachments_campaign
    FOREIGN KEY (campaign_id) REFERENCES campaigns (id) ON DELETE CASCADE
);

CREATE INDEX idx_campaign_attachments_campaign ON campaign_attachments (campaign_id);
