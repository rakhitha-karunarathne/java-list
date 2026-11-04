-- Durable campaign link for Bounce Messages after send_queue rows are deleted.
ALTER TABLE bounce_events
  ADD COLUMN campaign_id CHAR(36) NULL;

UPDATE bounce_events be
INNER JOIN send_queue sq ON sq.id = be.send_queue_id
SET be.campaign_id = sq.campaign_id
WHERE be.send_queue_id IS NOT NULL
  AND (be.campaign_id IS NULL OR be.campaign_id = '');

CREATE INDEX idx_bounce_events_campaign ON bounce_events (campaign_id);
