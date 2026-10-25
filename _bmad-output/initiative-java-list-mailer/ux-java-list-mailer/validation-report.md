# Validation Report — java-list / List Mailer UX

- **DESIGN.md:** `_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/DESIGN.md`
- **EXPERIENCE.md:** `_bmad-output/initiative-java-list-mailer/ux-java-list-mailer/EXPERIENCE.md`
- **Run at:** 2026-10-25T23:36:00+05:30
- **Lenses:** rubric walker only

## Overall verdict

Clearwater DESIGN is contract-ready for tokens. EXPERIENCE locks core IA (detail-not-nav, Parameters stacked, login/MFA, Compose) but is **thin** as a full R1–R34 UX contract: member HTTPS surfaces, MTS Issues/Drop/Blocks, Notice path, and many per-surface states/flows are in the mock but not promoted into the spine. Adequate for catalogs/campaigns story slicing; Update recommended before treating EXPERIENCE as complete product UX law.

## Category verdicts

- Flow coverage — **thin**
- Token completeness — **adequate**
- Component coverage — **thin**
- State coverage — **thin**
- Visual reference coverage — **adequate**
- Bloat & overspecification — **strong**
- Inheritance discipline — **adequate**
- Shape fit — **adequate**

## Findings by severity

### Critical (1)

**Flow coverage** — Member unsub/consent missing from IA  
HTTPS unsubscribe and consent (R14–R15) exist in `mockups/clickable` but have no EXPERIENCE IA rows or Key Flow.  
Fix: Add member-surface IA + Key Flow with expired/already-unsubscribed failure paths.

### High (7)

**Flow** — Notice path, From-domain Issues, Drop log, Global blocks in mock nav but not EXPERIENCE IA (R6, R30, R32).  
**Flow** — Attention role scoping (R8) has no Key Flow (Admin vs LM vs sender).  
**Flow** — Install / first Admin (R1) has no Key Flow.  
**Components** — DESIGN-only controls lack EXPERIENCE behavior rows (Button, Card, Input, Badge, Table).  
**Components** — EXPERIENCE-only patterns lack DESIGN specs (Compose preview, Test send, Used By, Attention rows, MFA panel).  
**State** — No per-surface empty/error/permission matrix for most IA surfaces.  
**Inheritance** — Audit IA defers to mock instead of committing R33.

### Medium (15) / Low (12)

See `review-rubric.md` (failure paths on Flows 1–2, contrast notes in DESIGN, Inspiration section missing, `.working` duplicate mock link, etc.).

## Reviewer files

- `review-rubric.md`
