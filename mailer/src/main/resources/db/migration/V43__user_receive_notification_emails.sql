-- Per-user opt-in for Admin notice mail (Notice Sending Path).
ALTER TABLE users
  ADD COLUMN receive_notification_emails BOOLEAN NOT NULL DEFAULT TRUE;

-- Bootstrap default Admin uses a dummy address — do not email it.
UPDATE users
SET receive_notification_emails = FALSE
WHERE LOWER(email) = 'admin@example.com';
