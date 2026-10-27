ALTER TABLE send_queue
  ADD COLUMN retry_attempts INT NOT NULL DEFAULT 0,
  ADD COLUMN next_attempt_at TIMESTAMP(3) NULL;

INSERT INTO parameters (param_key, param_value, description)
VALUES (
  'send.retry.count',
  '3',
  'Further SMTP tries after the first failure for a recipient. Integer >= 0. Default 3.'
)
ON DUPLICATE KEY UPDATE param_key = param_key;

INSERT INTO parameters (param_key, param_value, description)
VALUES (
  'send.retry.delay.minutes',
  '5',
  'Minutes to wait before the next try of a Failed-Retry Pending row. Integer >= 0. Default 5.'
)
ON DUPLICATE KEY UPDATE param_key = param_key;
