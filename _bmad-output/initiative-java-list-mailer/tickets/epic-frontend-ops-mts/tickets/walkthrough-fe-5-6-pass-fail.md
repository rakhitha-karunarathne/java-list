# FE-5.6 Clearwater browser walkthrough — pass/fail

**Date:** 2026-10-26  
**Target:** `http://localhost:8080/` (docker `java-list-app` rebuilt with current SPA)  
**Actor:** `admin@example.com` / bootstrap password  
**Method:** Cursor IDE browser automation + page DOM flows

## Checklist

| # | Requirement | Result | Notes |
|---|-------------|--------|-------|
| 1 | Shell roles — Admin sees Attention/Users/Parameters | **PASS** | Full admin nav present after login |
| 2 | Catalog — open/create Sending path | **PASS** | Index listed 2 paths; detail opened with Used By; create via UI works when React `input` events fire |
| 3 | Lists/Members — Lists index + Members search | **PASS** | Created list with path/footer; Members empty-state sentence shown |
| 4 | Compose — Save Draft smoke | **PASS** | `Saved Draft …` + status badge **Draft** |
| 5 | MTS — open index | **PASS** | Inboxes & Mappings + Create inbox form |
| 6 | Attention / Ops Logs | **PASS** | Both indexes load without error banner |
| 7 | C8 — detail not in sidebar | **PASS** | Path detail / User detail kept sidebar on index item |
| 8 | Artifact committed | **PASS** | This file |

## Extra surfaces smoked

| Surface | Result |
|---------|--------|
| Campaigns index + status filter + Draft row Stop | PASS |
| Footers create → detail | PASS |
| Notice Sending Path unset copy | PASS |
| Parameters (stacked + descriptions) | PASS |
| Users detail Security panel | PASS |
| Drop Log / Global Blocks / Issues | PASS (indexes) |
| Bounce Mailboxes index | PASS |
| Bounce Log | **STUB** — placeholder copy only (no API) |
| Audit Log | PASS |

## Failures / follow-ups

1. **Bounce Log** remains a stub (no backend list API).
2. MCP `browser_fill` + Create on Sending Paths once failed to submit (password/controlled-input timing); native React value setters + click succeeded — treat as automation flake unless reproduced manually.
3. No List-sender session exercised in this run (Admin only).
4. Queue/test-send against GreenMail not clicked in this pass (Draft save only).

## Verdict

**FE-5.6 walkthrough: PASS** for Admin happy-path IA coverage (with Bounce Log stub called out).
