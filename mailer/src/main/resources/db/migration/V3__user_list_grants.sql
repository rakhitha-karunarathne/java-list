CREATE TABLE user_list_grants (
  user_id CHAR(36) NOT NULL,
  list_id CHAR(36) NOT NULL,
  can_manage TINYINT(1) NOT NULL,
  can_send TINYINT(1) NOT NULL,
  PRIMARY KEY (user_id, list_id),
  CONSTRAINT fk_user_list_grants_user FOREIGN KEY (user_id) REFERENCES users (id)
);
