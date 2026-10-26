ALTER TABLE campaign_attachments
  ADD CONSTRAINT uq_campaign_attachments_campaign_filename UNIQUE (campaign_id, filename);
