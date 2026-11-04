---
id: 9
type: story
title: "Delete user and grants"
parent: epic-users-access
covers: [R1, R2]
after: [2, 3]
hitl: false
risk: medium
refined: true
---

# Delete user and grants

## Description

Admin **Users & Grants** detail can **Delete** an operator account. Confirmation is required. Delete removes the user row, list grants, MFA backup codes, and TOTP secret. Delete is refused for your own account, when this is the only Admin account, or when deleting would leave no enabled Admin (same idea as refusing to disable the last Admin).

## Requirements

1. **Given** User detail for a deletable Admin (another enabled Admin exists) or any List Manager / List Sender  
   **When** Delete is confirmed  
   **Then** the user, their list grants, MFA backup codes, and TOTP secret are removed

2. **Given** there is only one Admin account, or this is the last enabled Admin  
   **When** Delete is attempted for that Admin  
   **Then** the API rejects with conflict and the user (and their grants/MFA data) is unchanged

3. **Given** User detail  
   **When** Delete is clicked  
   **Then** a confirmation dialog runs before any DELETE request

4. **Given** Users index  
   **Then** there is no Delete control (detail only)

5. **Given** User detail for the signed-in Admin  
   **When** Delete is attempted  
   **Then** the API rejects (forbidden) and the UI does not offer Delete

## Boundaries

- In: `DELETE /api/users/{id}`; Users detail `confirmDelete`; cascade grants + MFA data  
- Out: Soft-delete; deleting audit/ops log history that mentions the email; self-service account deletion on My Account
