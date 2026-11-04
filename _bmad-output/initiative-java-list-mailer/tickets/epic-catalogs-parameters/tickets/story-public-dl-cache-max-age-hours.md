---
id: 12
type: story
title: "Public /dl cache max-age hours parameter"
parent: epic-catalogs-parameters
covers: [R1]
after: [5]
hitl: false
risk: low
refined: true
---

# Public /dl cache max-age hours parameter

## Description

Public campaign image downloads at `/dl/{campaignId}/{filename}` send `Cache-Control: public, max-age=…`. The max-age must be configurable under **System → Parameters** as **hours** (not seconds), default **24** hours (86400 seconds). Invalid or missing values fall back to 24.

## Requirements

1. **Given** Parameter `public.dl.cache.max.age.hours` is unset (seed default 24)  
   **When** GET `/dl/…` serves an image  
   **Then** `Cache-Control` is `public, max-age=86400`

2. **Given** an Admin sets the Parameter to `N` (integer >= 1)  
   **When** GET `/dl/…` serves an image  
   **Then** `Cache-Control` is `public, max-age=` (`N` × 3600)

3. **Given** the stored value is blank, non-integer, or `< 1`  
   **When** `/dl` builds Cache-Control  
   **Then** it uses 24 hours

4. **Given** Parameters Admin UI  
   **Then** the key is listed with a description that states hours and the default

## Boundaries

- In: ParameterKeys/Definitions seed; Parameters PUT validation (>= 1); `PublicDlController` Cache-Control; user-guide  
- Out: CDN/proxy config; ETag/Last-Modified; changing which content `/dl` serves
