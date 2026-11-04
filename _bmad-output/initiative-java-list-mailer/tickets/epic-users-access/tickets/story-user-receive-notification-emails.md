---
id: 7
type: story
title: "User receive notification emails preference"
parent: epic-users-access
covers: [R1, R2]
after: [2]
hitl: false
risk: low
refined: true
---

# User receive notification emails preference

## Description

Each operator account has **Receive Notification Emails** (boolean). Admin ops notices (`AdminNoticeMail` / Notice Sending Path) go only to enabled Admins with this flag on.

The auto-seeded bootstrap Admin (`MAILER_BOOTSTRAP_EMAIL`, default `admin@example.com`) is created with the flag **off** (dummy mailbox). Other new users default **on**. Operators can change their own flag on My Account; Admins can change it on Users & Grants.

## Requirements

1. **Given** an enabled Admin with Receive Notification Emails on  
   **When** an admin notice is sent  
   **Then** that Admin is included

2. **Given** Receive Notification Emails off (or account disabled)  
   **Then** that user is not emailed for admin notices

3. **Given** empty DB install seeds the bootstrap Admin  
   **Then** that user has Receive Notification Emails **off**

4. **Given** My Account or Users detail  
   **When** the operator saves the toggle  
   **Then** the preference persists

## Boundaries

- In: `users.receive_notification_emails`; AdminNoticeMail filter; Me + Users API/UI; bootstrap seed  
- Out: Changing consent/MTS notices to members/senders (those are not user-account notices)
