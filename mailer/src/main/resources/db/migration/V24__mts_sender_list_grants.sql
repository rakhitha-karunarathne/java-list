-- Per authorized sender × list: substitute From + footer (replaces named inbox mappings).

CREATE TABLE mts_sender_list_grants (
  id CHAR(36) NOT NULL,
  inbox_id CHAR(36) NOT NULL,
  sender_email VARCHAR(320) NOT NULL,
  list_id CHAR(36) NOT NULL,
  substitute_from VARCHAR(320) NULL,
  footer_id CHAR(36) NULL,
  position INT NOT NULL DEFAULT 0,
  PRIMARY KEY (inbox_id, sender_email, list_id),
  CONSTRAINT uq_mts_slg_id UNIQUE (id),
  CONSTRAINT fk_mts_slg_sender FOREIGN KEY (inbox_id, sender_email)
    REFERENCES mts_authorized_senders (inbox_id, email) ON DELETE CASCADE,
  CONSTRAINT fk_mts_slg_list FOREIGN KEY (list_id) REFERENCES lists (id),
  CONSTRAINT fk_mts_slg_footer FOREIGN KEY (footer_id) REFERENCES footers (id)
);

INSERT INTO mts_sender_list_grants (id, inbox_id, sender_email, list_id, substitute_from, footer_id, position)
SELECT UUID(), m.inbox_id, s.email, ml.list_id, m.substitute_from, m.footer_id, ml.position
FROM mts_mapping_lists ml
INNER JOIN mts_mappings m ON m.id = ml.mapping_id
INNER JOIN mts_authorized_senders s ON s.inbox_id = m.inbox_id;

DROP TABLE mts_mapping_lists;
DROP TABLE mts_mappings;

-- Orphaned Used By rows from named mappings (source_id was mapping id).
DELETE FROM catalog_references WHERE source_type = 'MTS_MAPPING';
