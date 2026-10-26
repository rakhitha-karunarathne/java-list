DELETE FROM parameters WHERE param_key = 'send.queue.throttle.per.minute';

CREATE INDEX idx_send_queue_status_sent ON send_queue (status, sent_at);
