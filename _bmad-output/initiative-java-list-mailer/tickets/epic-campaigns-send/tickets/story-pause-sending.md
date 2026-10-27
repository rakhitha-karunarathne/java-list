---
id: 12
type: story
title: "Pause sending"
parent: epic-campaigns-send
covers: [R21, R35, R36, R37]
after: [11]
hitl: true
risk: high
refined: true
status: ready
---

# Pause sending

## Description

Three holds, each independent of Stop. The send processor checks them before each recipient. A message already handed to SMTP may finish. The next recipient does not start while a hold applies.

- Parameter `send.paused` (default false) stops campaign sends. It does not change campaign statuses. Test sends still go out. Notice-path mail still goes out.
- Pause on a Queued or Sending campaign sets status Paused. Resume sets Queued without Requeue. Admin, List manager, and List sender may do this on a campaign they can open.
- Pause on a List holds rows queued for that List. An address queued under a List that is not paused still sends. Admin may pause any List. A List manager may pause a List they manage. A List sender cannot. Mail-to-send may still create a campaign for a paused List. Those rows wait until the List is resumed.

Pause flags are cached from the database. Setting one to paused clears the cache in this process before the response returns. The cache is re-read on each 60-second wake. Clearing it across multiple instances is V2+.

## Requirements

1. **Global pause**
   **Given** a batch already sending
   **When** an Admin sets `send.paused` true
   **Then** the next recipient is not started
   **And** a test send still goes out
   **And** a Notice-path message still goes out
   **And** campaign statuses stay as they were

2. **Campaign pause**
   **Given** a Queued or Sending campaign
   **When** an Admin, List manager, or List sender pauses it
   **Then** the status is Paused
   **And** Resume sets Queued without Requeue
   **And** Draft, Finished, and Stopped do not show Pause
   **And** the campaigns index status filter includes Paused

3. **List pause**
   **Given** a queued row for List A, and the same address also on paused List B
   **When** List A is not paused
   **Then** that row sends
   **And** a row queued for a paused List waits until that List is resumed
   **And** mail-to-send may still create a campaign for a paused List

4. **List control**
   **Given** a List an Admin or a List manager can manage
   **When** they open that List
   **Then** Pause and Resume are on the List
   **And** a List sender does not see them
