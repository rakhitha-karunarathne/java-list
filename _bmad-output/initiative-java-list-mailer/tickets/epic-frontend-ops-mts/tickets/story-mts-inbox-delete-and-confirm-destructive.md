---
id: 9
type: story
title: "Delete MTS inbox (cascade) and confirm all destructive deletes"
parent: epic-frontend-ops-mts
covers: [R1]
after: [8]
hitl: false
risk: medium
refined: true
status: done
---

# Delete MTS inbox (cascade) and confirm all destructive deletes

## Description

Admins can remove an MTS inbox. Delete removes that inbox and all connected MTS data (authorized senders, list grants, catalog Used By rows for those grants, From-domain Issues, Drop log rows for the inbox). Every destructive delete in the SPA asks for confirmation first — deleted data is gone.

## Requirements

1. **Delete inbox**
   **Given** Admin is on an inbox detail
   **When** they confirm **Delete Inbox**
   **Then** `DELETE /api/mts/inboxes/{id}` removes the inbox
   **And** authorized senders, sender list grants, grant catalog references, From-domain Issues for that inbox, and Drop log rows for that inbox are removed
   **And** the SPA returns to the Inboxes index

2. **Confirm before delete**
   **Given** any SPA action that permanently deletes operator data (catalog Delete, bounce mailbox Delete, inbox Delete, remove authorized sender, Unblock, MFA unenroll)
   **When** the control is activated
   **Then** a confirm dialog states the action cannot be undone
   **And** Cancel leaves data unchanged; OK proceeds with the existing API call

## Boundaries

- In: MTS inbox DELETE + cascade cleanup; shared `confirmDelete` on existing SPA delete paths.
- Out: New modal component library; soft-delete; deleting campaigns/lists if no SPA delete UI yet; global blocks unrelated to the inbox.

## API sketch

`DELETE /api/mts/inboxes/{id}` → `200` `{ "status": "deleted" }` or `404`

## References

- EXPERIENCE — destructive confirm dialog pattern
- related — story-mts-inboxes-index-no-mappings-overview

## Notes

- Decision: 2026-10-26 — hard delete with cascade; confirm on every destructive SPA delete.

## Dev Agent Record

### Completion Notes

- `DELETE /api/mts/inboxes/{id}` clears grant catalog refs, deletes drop-log rows, deletes inbox (DB cascades senders → grants and from-domain issues).
- SPA: Delete Inbox on detail; `confirmDelete()` on path/footer/bounce Delete, remove sender, Unblock, MFA unenroll.
- EXPERIENCE documents destructive-delete confirm.

### Code Review (adversarial)

| Severity | Finding | Disposition |
|---|---|---|
| medium | Dirty inbox form triggered second leave-confirm after Delete | Fixed — reset form dirty before close |
| low | Drop log has no FK; must delete explicitly | Fixed in service |
| low | confirmDelete test assumed browser `window` | Fixed — `globalThis.confirm` |
| info | Campaigns already created from MTS are not deleted with inbox | Out of scope — campaigns are independent |
| info | Lists/campaigns have no SPA Delete yet | Out of scope until those UIs exist |
