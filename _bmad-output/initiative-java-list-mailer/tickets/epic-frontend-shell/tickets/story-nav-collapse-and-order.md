---
id: 4
type: story
title: "Collapsible nav categories; Campaigns before Lists"
parent: epic-frontend-shell
covers: [R1, R2]
after: [1]
hitl: false
risk: low
refined: true
---

# Collapsible nav categories; Campaigns before Lists

## Description

Operator sidebar categories become collapsible. **Campaigns** appears **before** **Lists & Members**. On first load, **Dashboard** (Attention), **Campaigns**, and **Lists & Members** start expanded; every other category starts collapsed. Clicking a category heading toggles its items. If the current view lives in a collapsed section, that section stays usable (expand it when navigating into it or keep it open while active).

## Requirements

1. **Given** the sidebar  
   **Then** section order is Dashboard → Campaigns → Lists & Members → Mail-To-Send → Bounces → Catalogs → System

2. **Given** a fresh page load with default home view  
   **Then** Dashboard, Campaigns, and Lists & Members are expanded; other categories are collapsed

3. **Given** the operator clicks a category label  
   **Then** that category’s items show or hide

4. **Given** C8 / NavIaC8Test  
   **Then** sidebar still lists indexes/tools only (no detail destinations)

## Boundaries

- In: `navConfig.js` order; `Shell.jsx` collapse UI; light CSS  
- Out: Site title / MFA issuer (separate story); persisting collapse across sessions (optional later)
