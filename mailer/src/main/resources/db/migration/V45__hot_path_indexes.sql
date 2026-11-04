-- Hot-path indexes for bounce correlation, send queue, and reverse-FK joins.
-- Keep in sync with repository findBy*/deleteBy* patterns (see docs/db-indexes.md).

-- Normalize blanks so UNIQUE nullable columns do not collide on ''
UPDATE send_queue SET verp_rcpt = NULL WHERE verp_rcpt = '';
UPDATE send_queue SET smtp_message_id = NULL WHERE smtp_message_id = '';

-- Bounce correlation ladder (VERP / Message-ID / subject+email fallback)
CREATE UNIQUE INDEX uq_send_queue_verp_rcpt ON send_queue (verp_rcpt);
CREATE UNIQUE INDEX uq_send_queue_smtp_message_id ON send_queue (smtp_message_id);
CREATE INDEX idx_send_queue_status_subject ON send_queue (status, subject);
CREATE INDEX idx_send_queue_member_status ON send_queue (member_id, status);

-- Bounce process + campaign/member cleanup joins
CREATE INDEX idx_bounce_events_send_queue ON bounce_events (send_queue_id);
CREATE INDEX idx_bounce_events_member_status ON bounce_events (member_id, status);
CREATE INDEX idx_bounce_events_mailbox_status ON bounce_events (mailbox_id, status);
CREATE INDEX idx_bounce_events_received ON bounce_events (received_at);

-- Campaign / list reverse lookups (PK leads with the other column)
CREATE INDEX idx_campaigns_status ON campaigns (status);
CREATE INDEX idx_campaign_lists_list ON campaign_lists (list_id);
CREATE INDEX idx_list_members_member ON list_members (member_id);
CREATE INDEX idx_user_list_grants_list ON user_list_grants (list_id);
CREATE INDEX idx_mts_sender_list_grants_list ON mts_sender_list_grants (list_id);
CREATE INDEX idx_mts_authorized_senders_email ON mts_authorized_senders (email);

-- Attention LM inbox by list; secrets rewrap/outdated counts
CREATE INDEX idx_attention_open_list ON attention_items (open_flag, list_id, created_at);
CREATE INDEX idx_secrets_key_version ON secrets (key_version);
