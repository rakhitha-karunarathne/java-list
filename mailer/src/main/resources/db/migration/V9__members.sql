CREATE TABLE members (
  id CHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(320) NOT NULL,
  enabled TINYINT(1) NOT NULL,
  disable_reason VARCHAR(255) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uq_members_email UNIQUE (email)
);

CREATE TABLE list_members (
  list_id CHAR(36) NOT NULL,
  member_id CHAR(36) NOT NULL,
  unsubscribed TINYINT(1) NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (list_id, member_id),
  CONSTRAINT fk_list_members_list FOREIGN KEY (list_id) REFERENCES lists (id),
  CONSTRAINT fk_list_members_member FOREIGN KEY (member_id) REFERENCES members (id)
);
