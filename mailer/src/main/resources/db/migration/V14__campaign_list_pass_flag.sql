ALTER TABLE campaign_lists
  ADD COLUMN included_in_pass TINYINT(1) NOT NULL DEFAULT 0;

-- Finished campaigns already completed a pass; require Add List before Finished requeue.
UPDATE campaign_lists cl
  INNER JOIN campaigns c ON c.id = cl.campaign_id
SET cl.included_in_pass = 1
WHERE c.status = 'FINISHED';
