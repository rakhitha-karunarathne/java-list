ALTER TABLE lists
  ADD COLUMN paused BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE send_queue
  ADD COLUMN list_id VARCHAR(36) NULL;

INSERT INTO parameters (param_key, param_value, description)
VALUES (
  'send.paused',
  'false',
  'When true, the send-queue processor starts no further campaign messages. Test sends and Notice-path mail still go out. Values: true|false. Default false.'
)
ON DUPLICATE KEY UPDATE param_key = param_key;
