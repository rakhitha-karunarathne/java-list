---
id: 10
type: story
title: "Poll one mailbox now"
parent: epic-frontend-ops-mts
covers: [R4, R29]
after: [9]
hitl: true
risk: medium
refined: true
status: ready
---

# Poll one mailbox now

## Description

There is no Execute Queues page and no send-queue button. Each Bounce mailbox record and each Mail-To-Send inbox record shows last polled time, a Result of OK or Errors, and a Poll button. The index columns are Name, Poll interval, Last polled, and Result. Host is not on the index. Only an Admin sees it. Pressing Poll starts a poll of that one mailbox and returns at once. The screen says the process started. Last polled updates when the poll finishes. The 60-second jobs stay and still poll only enabled mailboxes whose interval has elapsed. Poll is shown only on an enabled mailbox. A disabled mailbox has no Poll button, and the poll endpoint rejects it. Each mailbox has its own poll lock. A second poll of that mailbox does not start while the lock is held, and the screen still says the process started. The mailbox stores Last Polling Update, set at polling start, after each message read, and when polling is done. `mailer.poll.lock-timeout` defaults to 1 hour. A Last Polling Update older than that is treated as released. A different mailbox can still be polled. While the lock is held, detail shows a Polling section with that time. The section is hidden when the mailbox is not polling. Break lock is shown only when Last Polling Update is at least 60 seconds old. Break lock releases the lock and does not stop a poll already talking to the mail server.

The mailbox detail page has a section of that mailbox’s processing-log rows (time, event, message, newest first). Bounce mailboxes use the Bounce Processing Log. Inboxes use the Mail-To-Send Processing Log. When that log parameter is off, the section says logging is off. The full log pages are unchanged.

Poll interval minutes already exists (default 15). Last polled is already stored and is not shown. A log row for a mailbox stores that mailbox’s id. The screen shows the name. Existing Bounce and Mail-To-Send processing-log rows are deleted once. The Queue Sending Log is left as it is.

## Requirements

1. **Index columns**
   **Given** the Bounce mailboxes index or the Mail-To-Send inboxes index
   **When** an Admin views it
   **Then** the columns are Name, Poll interval, Last polled, and Result
   **And** Host is not shown
   **And** Result is OK after a finished poll, Errors after a failed poll, and blank when never polled
   **And** a message that was ingested or rejected does not by itself set Errors

2. **Poll this box**
   **Given** an Admin on that record
   **When** they press Poll
   **Then** the screen says the process started without waiting for the poll
   **And** only that mailbox is polled
   **And** its last polled time updates when the poll finishes
   **And** no other mailbox is polled

3. **Log section**
   **Given** logging is on for that kind
   **When** an Admin opens the mailbox detail
   **Then** a section lists log rows stored for that mailbox id
   **And** the screen shows the mailbox name, or the id if that mailbox is gone
   **And** when logging is off the section says logging is off
   **And** existing Bounce and Mail-To-Send processing-log rows are deleted once

4. **Admin only**
   **Given** a List manager or List sender
   **When** they view the same records
   **Then** there is no Poll button
   **And** the poll endpoint rejects them

5. **Disabled mailbox**
   **Given** a mailbox that is disabled
   **When** an Admin views it
   **Then** there is no Poll button
   **And** the poll endpoint rejects a poll of that mailbox

6. **One poll per mailbox**
   **Given** a poll of that mailbox is in progress and its lock is newer than the timeout
   **When** Poll is pressed again, or the wake reaches that mailbox
   **Then** the screen says the process started
   **And** a second poll of that mailbox does not start
   **And** a Last Polling Update older than 1 hour is treated as released

7. **Polling section**
   **Given** a mailbox whose lock is held
   **When** an Admin opens its detail
   **Then** a Polling section shows Last Polling Update
   **And** the section is hidden when the mailbox is not polling
   **And** Break lock is shown only when Last Polling Update is at least 60 seconds old
   **And** Break lock releases the lock
   **And** a poll already talking to the mail server is not stopped

8. **Polling heartbeat**
   **Given** a poll of that mailbox
   **When** it starts, reads a message, or finishes
   **Then** Last Polling Update on that mailbox is set to that time
