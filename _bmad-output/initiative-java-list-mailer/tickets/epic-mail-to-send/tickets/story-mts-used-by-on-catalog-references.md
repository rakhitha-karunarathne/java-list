---
id: 5
type: story
title: "MTS Used By on catalog references"
parent: epic-mail-to-send
covers: [R6]
after: [1]
hitl: false
risk: medium
refined: true
---

# MTS Used By on catalog references

## Description

Catalog **Used By** for Sending paths and Footers includes Mail-to-send **mappings** as referencers. Admins cannot delete catalog rows while an MTS mapping still references them; Used By links navigate to the inbox/mapping context.

## Requirements

1. **Sending path Used By includes mappings (R6)**
   **Given** an MTS mapping derives or references a Sending path (via List or explicit path association per product model)
   **When** an Admin opens that Sending path detail
   **Then** Used By lists the referencing MTS mapping(s) with navigable links to inbox/mapping detail
   **And** delete is blocked while any mapping references the path

2. **Footer Used By includes mappings (R6)**
   **Given** an MTS mapping sets a footer override
   **When** an Admin opens that Footer catalog detail
   **Then** Used By lists referencing MTS mapping(s)
   **And** delete is blocked while referenced

3. **Delete blocked with clear Used By (R6)**
   **Given** a Sending path or Footer is referenced by at least one MTS mapping
   **When** Admin attempts delete
   **Then** delete fails with Used By pointing at inbox/mapping rows
   **And** no orphan mapping rows remain if delete were forced (delete cannot succeed)

4. **Unmapped catalog delete still allowed (R6)**
   **Given** a Sending path or Footer is not referenced by any List, Notice path, or MTS mapping
   **When** Admin deletes it
   **Then** delete succeeds per catalogs epic rules

5. **Registry integration (R6)**
   **Given** send-to-mail stores catalog foreign keys on mapping rows
   **When** catalogs slice queries referencers via kernel ports
   **Then** MTS mappings participate in the same R9 delete-guard contract as Lists and other referencers

## Boundaries

- Does not change List-level Used By or Notice sending path rules (catalogs epic).
- Does not alter mapping CRUD UI beyond surfacing delete-block consequences on catalog side.
- From-domain re-validation on path change remains story 2.

## References

- parent — _bmad-output/initiative-java-list-mailer/tickets/epic-mail-to-send/epic-mail-to-send.md
- architecture — _bmad-output/initiative-java-list-mailer/architecture-java-list-mailer/architecture-java-list-mailer.md (R9 / Used By)
- catalogs — _bmad-output/initiative-java-list-mailer/tickets/epic-catalogs-parameters/epic-catalogs-parameters.md

## Notes

- Decision: 2026-10-26 — Refined with full criteria on user request (epics 5–9).
- Align ReferenceQueryPort (or equivalent) so MTS registers footers + sending paths + Lists on mappings.
