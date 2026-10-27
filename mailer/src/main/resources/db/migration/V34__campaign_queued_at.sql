ALTER TABLE campaigns
  ADD COLUMN queued_at TIMESTAMP(3) NULL;

-- Existing non-draft campaigns were queued at some point; created_at is the best known stamp.
UPDATE campaigns
SET queued_at = created_at
WHERE status <> 'DRAFT'
  AND queued_at IS NULL;
