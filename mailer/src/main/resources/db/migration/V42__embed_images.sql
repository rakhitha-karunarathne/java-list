ALTER TABLE campaigns
  ADD COLUMN embed_images TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE mts_inboxes
  ADD COLUMN embed_images TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE campaign_attachments
  ADD COLUMN content_id VARCHAR(255) NULL;
