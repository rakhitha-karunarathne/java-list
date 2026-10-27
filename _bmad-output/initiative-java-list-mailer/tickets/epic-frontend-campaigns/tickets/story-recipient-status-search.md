---
id: 9
type: story
title: "Search campaign recipients by email and status"
parent: epic-frontend-campaigns
covers: [R24]
after: [8]
hitl: false
risk: medium
refined: true
status: ready
---

# Search campaign recipients by email and status

## Description

The campaign detail recipients table lists every recipient for that campaign. Columns are Email, Status, First Open, and Opens. Status is Pending, Failed-Retry Pending, Sent, Send Failed, Opened, or Bounced. Bounced wins over Opened. First Open and Opens still show. One search box keeps a row when the email contains the text or the status label contains the text, ignoring case. Retry failed shows when any row is Send Failed.

## Requirements

1. **Every recipient**
   **Given** a campaign with pending and sent recipients
   **When** an operator opens its detail
   **Then** both appear in the recipients table

2. **Status column**
   **Given** a recipient who opened and bounced
   **When** the table is shown
   **Then** the status is Bounced
   **And** First Open and Opens still show

3. **Search**
   **Given** a search text
   **When** the operator types it
   **Then** a row stays if its email contains the text or its status label contains the text, ignoring case

4. **Retry failed**
   **Given** at least one Send Failed recipient
   **When** the operator opens the campaign
   **Then** Retry failed is shown
   **And** it is hidden when there are none
