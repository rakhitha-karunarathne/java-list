-- Age default 30: rewrite only the old seed value of 90.
UPDATE parameters SET value = '30'
WHERE `key` IN (
  'bounce.process.log.retention.days',
  'send.queue.log.retention.days',
  'mts.process.log.retention.days'
) AND value = '90';

INSERT INTO parameters (`key`, value, description)
VALUES
  ('bounce.process.log.max.rows', '5000', 'Maximum Bounce Processing Log rows to keep. Integer >= 1. Default 5000.'),
  ('send.queue.log.max.rows', '5000', 'Maximum Queue Sending Log rows to keep. Integer >= 1. Default 5000.'),
  ('mts.process.log.max.rows', '5000', 'Maximum Mail-To-Send Processing Log rows to keep. Integer >= 1. Default 5000.')
ON DUPLICATE KEY UPDATE `key` = `key`;
