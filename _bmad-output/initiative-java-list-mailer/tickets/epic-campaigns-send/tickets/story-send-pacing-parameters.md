# Story: Send pacing parameters

Status: done

Replaces `send.queue.throttle.per.minute`. Four Parameters on the Parameters page:

- `send.delay.per.email.ms` default 1000. Pause before each send after the first in a run. Integer >= 0.
- `send.batch.size` default 600. Maximum SMTP sends in one schedule run. Integer >= 1.
- `send.batch.duration.minutes` default 10. The run stops before the next send once this time is up, even if the batch is not full. Integer >= 1.
- `send.messages.per.hour` default 3600. Count of SENT rows in the last 60 minutes. A run sends only the remaining room, and never more than the batch size. When the hour is full, the run sends nothing and writes no Queue Sending Log row. Integer >= 1.

Each message commits in its own transaction. The delay is slept outside that transaction and is cut short so the run does not sleep past the deadline.

V26 deletes the old parameter row. The app seeds the four new keys on startup.
