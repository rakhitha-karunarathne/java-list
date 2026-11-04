-- Rename campaign size Parameters from bytes to KB; convert stored values.
INSERT INTO parameters (param_key, param_value, description)
SELECT
  'campaign.payload.max.kb',
  CAST(GREATEST(1, CEILING(CAST(param_value AS DECIMAL(20, 0)) / 1024)) AS CHAR),
  'Hard upper for Campaign HTML+text payload size in KB before queue. Integer >= 1. Default 2000.'
FROM parameters
WHERE param_key = 'campaign.payload.max.bytes'
  AND param_value REGEXP '^[0-9]+$'
ON DUPLICATE KEY UPDATE
  param_value = VALUES(param_value),
  description = VALUES(description);

INSERT INTO parameters (param_key, param_value, description)
SELECT
  'campaign.image.max.kb',
  CAST(GREATEST(1, CEILING(CAST(param_value AS DECIMAL(20, 0)) / 1024)) AS CHAR),
  'Hard upper for a single fetched/stored Campaign image in KB. Integer >= 1. Default 500.'
FROM parameters
WHERE param_key = 'campaign.image.max.bytes'
  AND param_value REGEXP '^[0-9]+$'
ON DUPLICATE KEY UPDATE
  param_value = VALUES(param_value),
  description = VALUES(description);

DELETE FROM parameters
WHERE param_key IN ('campaign.payload.max.bytes', 'campaign.image.max.bytes');

-- List payload_override matched the old byte Parameter; convert large values to KB.
UPDATE lists
SET payload_override =
  CAST(GREATEST(1, CEILING(CAST(payload_override AS DECIMAL(20, 0)) / 1024)) AS CHAR)
WHERE payload_override REGEXP '^[0-9]+$'
  AND CAST(payload_override AS UNSIGNED) >= 1024;
