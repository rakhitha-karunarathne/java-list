-- Per-path VERP (was global Parameter verp.enabled).
ALTER TABLE sending_paths
  ADD COLUMN verp_enabled BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE sending_paths
SET verp_enabled = TRUE
WHERE EXISTS (
  SELECT 1
  FROM parameters
  WHERE param_key = 'verp.enabled'
    AND LOWER(param_value) = 'true'
);

DELETE FROM parameters WHERE param_key = 'verp.enabled';
