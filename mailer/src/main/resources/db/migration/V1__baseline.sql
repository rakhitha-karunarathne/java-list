-- Baseline schema marker for Flyway tracer (platform epic).
-- Domain tables land in later stories.
CREATE TABLE flyway_probe (
  id BIGINT NOT NULL PRIMARY KEY,
  note VARCHAR(64) NOT NULL
);

INSERT INTO flyway_probe (id, note) VALUES (1, 'baseline');
