ALTER TABLE mts_authorized_senders
  ADD COLUMN substitute_from VARCHAR(320) NULL,
  ADD COLUMN sending_path_id CHAR(36) NULL,
  ADD CONSTRAINT fk_mts_auth_sending_path
    FOREIGN KEY (sending_path_id) REFERENCES sending_paths (id);

-- First non-empty grant substitute (by position) becomes the sender-level value.
UPDATE mts_authorized_senders s
SET s.substitute_from = (
  SELECT g.substitute_from
  FROM mts_sender_list_grants g
  WHERE g.inbox_id = s.inbox_id
    AND g.sender_email = s.email
    AND g.substitute_from IS NOT NULL
    AND TRIM(g.substitute_from) <> ''
  ORDER BY g.position ASC
  LIMIT 1
);

ALTER TABLE mts_sender_list_grants
  DROP COLUMN substitute_from;
