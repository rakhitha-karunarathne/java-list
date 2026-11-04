---
id: 11
type: story
title: "Site title parameter (nav + MFA issuer)"
parent: epic-catalogs-parameters
covers: [R5]
after: [5]
hitl: false
risk: medium
refined: true
---

# Site title parameter (nav + MFA issuer)

## Description

Admins managing several List Mailer deployments need a distinct **website title** so authenticator apps and the operator UI are not all labeled “List Mailer”. Add Parameter `site.title` as the **first** row on System → Parameters (default `List Mailer`). Use it as the TOTP issuer when enrolling MFA, and show it as the sidebar brand (and login heading). Expose a public read of the current title so the login screen can show it without auth.

## Requirements

1. **Given** a fresh install  
   **Then** Parameters lists `site.title` first with value `List Mailer`

2. **Given** Admin sets `site.title` to a non-blank value (e.g. `Acme Lists`)  
   **Then** new MFA enroll otpauth issuer is that value; sidebar brand and login heading show it

3. **Given** `site.title` is missing or blank when read  
   **Then** the UI and MFA issuer fall back to `List Mailer`

4. **Given** Admin PUTs a blank `site.title`  
   **Then** the API rejects the update

## Boundaries

- In: ParameterKeys/Definitions seed + order; MFA setup issuer; Shell brand; LoginCard; public GET for title; user-guide  
- Out: Changing JWT issuer string; renaming the product in docs beyond operator-facing title; nav collapse/reorder (separate story)
