---
id: 1
type: story
title: "MTS inbox mappings create Queued campaign tracer"
parent: epic-mail-to-send
covers: [R1, R7]
after: [epic-campaigns-send, epic-bounce-processing]
hitl: true
risk: high
refined: true
---

# MTS inbox mappings create Queued campaign tracer

## Description

End-to-end tracer for Mail-to-send: Admin configures an IMAP MTS inbox (via mailbox-query from the bounce epic), authorized senders, and inbox→List mappings. Ingest polls the inbox incrementally (one inbox at a time, low memory). When an authorized sender’s message passes downstream gates (later stories), the system creates exactly one Campaign via campaigns ports in **Campaign Queued** (skip Draft), with mapping Lists, substitute From, and footer override applied.

## Requirements

1. **MTS index and inbox detail (R1, parent C8)**
   **Given** an Admin is signed in
   **When** they open **Inboxes & mappings** from the sidebar
   **Then** they see an index of MTS inboxes and an all-mappings overview
   **And** per-inbox settings, authorized senders, and that inbox’s mappings are reachable only from the index or a mapping row that targets the inbox — not as a primary sidebar destination

2. **IMAP-only inbox configuration (R1)**
   **Given** Admin creates or edits an MTS inbox
   **When** they save IMAP connection settings and a poll interval (default 15 minutes)
   **Then** the inbox is stored as IMAP-only (no POP3 path)
   **And** polling uses the shared mailbox-query utility (epic R5 / AD-12: one inbox at a time, stream/process incrementally)

3. **Authorized senders (R1)**
   **Given** an MTS inbox exists
   **When** Admin adds or removes authorized sender addresses for that inbox
   **Then** only listed senders can follow the authorized accept path for that inbox
   **And** changes are visible on inbox detail

4. **Inbox→List mappings (R1)**
   **Given** an MTS inbox and at least one List
   **When** Admin defines a mapping from that inbox to one or more Lists
   **Then** mapping rows show the target Lists
   **And** a message accepted for that mapping uses those Lists as the Campaign’s List set (with in-Campaign dedupe by address)

5. **Substitute From domain rule (R1)**
   **Given** a mapping with substitute From set for sender+inbox+List
   **When** substitute From is empty
   **Then** the Campaign uses the Sending path From for that List
   **When** substitute From is non-empty
   **Then** its domain matches the domain of that List’s Sending-path From or ingest treats the message as a From-domain Issue (story 2), not a partial Campaign

6. **Footer override (R1)**
   **Given** a mapping may reference a footer, and the List may have its own footer
   **When** an authorized message is accepted into a Campaign
   **Then** outbound content uses mapping footer if set, else List footer, else none

7. **Authorized accept → Campaign Queued (R7)**
   **Given** GreenMail holds a test IMAP MTS inbox, authorized sender, and mapping to one or more Lists with enabled subscribed Members
   **When** a valid authorized message is ingested and passes auth/spam/From-domain gates (fixtures may stub gates for this tracer)
   **Then** exactly one Campaign exists with status **Queued** (not Draft)
   **And** Campaign Lists match the mapping; send-queue rows are created per campaigns epic rules
   **And** Stop/Requeue/Finished list-add behavior is unchanged and owned by campaigns epic

8. **Two inbox copies → two Campaigns (R1)**
   **Given** the same logical message content is present as two distinct messages in the MTS inbox
   **When** both are processed on the authorized path
   **Then** two separate Campaigns in Queued are created

## Boundaries

- Must not re-implement IMAP poll/stream primitives; consume mailbox-query from epic-bounce-processing (R5).
- Must not own Campaign lifecycle transitions beyond create/enqueue via campaigns ports (R7); no Draft step for MTS-created Campaigns.
- Must not implement From-domain Issues, auth/spam/DMARC matrix, Drop log, or global blocks (stories 2–4).
- Out: POP3; external spam service / AI spam scoring.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-mail-to-send/epic-mail-to-send.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (AD-12, AD-16 send-to-mail)
- design — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md
- experience — _bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Tracer scope: prove inbox + mapping + Queued Campaign wiring; gate stories may use minimal fixtures until stories 2–3 land.
