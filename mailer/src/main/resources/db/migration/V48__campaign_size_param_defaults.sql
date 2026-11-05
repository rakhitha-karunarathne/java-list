-- Bump previous campaign size Parameter defaults to 2048 KB / 4096 KB.
UPDATE parameters
SET param_value = '2048',
    description = 'Hard upper for Campaign HTML+text payload size in KB before Queue. Integer >= 1. Default 2048.'
WHERE param_key = 'campaign.payload.max.kb'
  AND param_value IN ('2000', '1954');

UPDATE parameters
SET param_value = '4096',
    description = 'Hard upper for a single fetched/stored Campaign image in KB. Integer >= 1. Default 4096.'
WHERE param_key = 'campaign.image.max.kb'
  AND param_value = '500';
