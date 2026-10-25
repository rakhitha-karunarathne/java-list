CREATE TABLE bounce_mailboxes (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  host VARCHAR(255) NOT NULL,
  port INT NOT NULL,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  use_ssl TINYINT(1) NOT NULL,
  poll_interval_minutes INT NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_bounce_mailboxes_name UNIQUE (name)
);

CREATE TABLE footers (
  id CHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  html_body MEDIUMTEXT NOT NULL,
  text_body MEDIUMTEXT NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_footers_name UNIQUE (name)
);

CREATE TABLE notice_sending_path (
  id TINYINT NOT NULL PRIMARY KEY,
  sending_path_id CHAR(36) NULL,
  CONSTRAINT fk_notice_sending_path FOREIGN KEY (sending_path_id) REFERENCES sending_paths (id)
);

INSERT INTO notice_sending_path (id, sending_path_id) VALUES (1, NULL);
