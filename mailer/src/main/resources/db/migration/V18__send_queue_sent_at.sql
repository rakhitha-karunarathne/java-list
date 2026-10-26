ALTER TABLE send_queue
  ADD COLUMN sent_at TIMESTAMP(3) NULL;

CREATE INDEX idx_send_queue_member_sent ON send_queue (member_id, sent_at);
