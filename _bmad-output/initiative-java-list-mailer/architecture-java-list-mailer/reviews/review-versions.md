# Review: stack versions & tech-claim verification

**Date:** 2026-10-25  
**Spine:** `architecture-java-list-mailer.md`  
**Lens:** Every committed decision should be web-researched or reality-checked — library/framework versions, named tech still exists and fits.  
**Method:** Full read of spine + `.memlog.md`; web spot-check of Stack table (Oct 2026); cross-check of tech-adjacent AD rules against current product/docs.

---

## Verdict

**Conditional pass — stack pins are current and verifiable; AD corpus lacks research provenance.**

The five pinned stack versions in the spine **exist at the stated numbers** and align with upstream release state as of **2026-10-25**. No named framework in the Stack table is dead or fictional. **Architecture decisions AD-1–AD-24 were not web-researched** in any documented form; they trace to coaching elicitation in `.memlog.md`. That is acceptable for product/structure ADs but **fails the stated lens** if “every committed decision” includes all ADs. For **version drift**, the main risks are **unpinned transitive stack** (Hibernate/Jakarta/Spring Security via Boot BOM), **newer LTS alternatives** (Java 25, MariaDB 13.x), and **implement-time Flyway/MariaDB wiring** not spelled out in AD-8.

---

## Stack table verification

| Claim (spine) | Spot-check result | Sources (checked 2026-10-25) |
| --- | --- | --- |
| **Java 21 (LTS; Boot 4.1 requires 17+)** | **Valid.** Java 21 is an Oracle/OpenJDK LTS (Premier support to Sep 2028). Boot **4.1.2** requires **Java 17+**, compatible through **Java 27**. Pinning 21 is conservative and compatible. | [Oracle Java SE Support Roadmap](https://www.oracle.com/java/technologies/java-se-support-roadmap.html); [Spring Boot 4.1 system requirements](https://docs.spring.io/spring-boot/system-requirements.html) |
| **Spring Boot 4.1.2** | **Valid and current patch** on the 4.1 line (published **2026-10-22**). Maven Central and release notes present. | [Maven Central spring-boot 4.1.2](https://central.sonatype.com/artifact/org.springframework.boot/spring-boot/4.1.2); [GitHub release v4.1.2](https://github.com/spring-projects/spring-boot/releases/tag/v4.1.2) |
| **Flyway managed by Boot 4.1.x (12.4.0 at 4.1.0 baseline)** | **Valid for 4.1.2.** Boot **4.1.2** dependency management still lists **flyway-core / flyway-mysql @ 12.4.0**. Note: baseline wording is accurate; no evidence Flyway was bumped in 4.1.1/4.1.2 patch releases. Prefer **`spring-boot-starter-flyway`** (Boot 4 module) over legacy manual `flyway-core` only. | [Spring Boot 4.1 managed coordinates](https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html); [flyway.version property](https://docs.spring.io/spring-boot/4.1/appendix/dependency-versions/properties.html) |
| **React 19.3.1** | **Valid.** npm latest **19.3.1** (published **2026-10-14**). | [npm react](https://www.npmjs.com/package/react); [GitHub react v19.3.1](https://github.com/react/react/releases/tag/v19.3.1) |
| **MariaDB 12.3.3 (Community LTS)** | **Valid GA LTS maintenance release** (**2026-08-24**); series maintained to **June 2029**. Not the newest major line: **MariaDB 13.0.2** GA **2026-09-15** exists — pin is **intentional LTS choice**, not “latest server”. | [MariaDB 12.3.3 release notes](https://mariadb.com/docs/release-notes/community-server/12.3/12.3.3); [All releases](https://mariadb.com/docs/release-notes/community-server/all-releases) |

### Stack provenance in repo

- Pinned versions appear once in spine **Stack** (lines 211–220) and in `.memlog.md` line 95 (“Pinned at Finalize”) **without URLs or check date**.
- **Recommendation:** Add a one-line `stack-verified: 2026-10-25` + link row in spine front matter or a `stack-pins.md` appendix when implementing.

---

## Tech-adjacent AD claims (existence / fit)

| AD / area | Claim | Reality check | Risk |
| --- | --- | --- | --- |
| **AD-5** | In-process `ApplicationEvent` | Still first-class in Spring Framework 7 / Boot 4. | Low — pattern valid for V1. |
| **AD-6** | JWT Bearer operator API | Boot 4.1.x ships OAuth2 Resource Server / JWT fixes (e.g. issuer/JWKS edge cases in 4.1.2). | Low — pick starter + config at implement; no library named in spine. |
| **AD-7** | Docker Compose: app, MariaDB, **phpMyAdmin** | phpMyAdmin remains standard Compose adjunct; MariaDB official images track 12.x tags. | Low — image tag should match **12.3.3** in compose file when written. |
| **AD-8** | Flyway on startup; Hibernate `ddl-auto=validate`/`none` | Boot 4 pulls **Hibernate 7.4.x** (4.1.2 release notes). `ddl-auto=validate` remains valid. Flyway **12.x** documents MariaDB via **`flyway-mysql`** module; verified versions include **12.3.2** (docs updated **2026-10-21**). | **Medium** — spine does not state **`flyway-mysql`** / JDBC driver expectation; MariaDB 12.3.3 is newer than Flyway’s doc “verified” patch but same series. Confirm starter brings `flyway-mysql` on first migration run. |
| **AD-10 / Deferred** | No ShedLock V1; ShedLock named for V2+ | ShedLock project still maintained; deferral is product choice, not stale tech. | None for V1. |
| **AD-19** | `sessionStorage` for JWT | Browser API unchanged; fits SPA + Bearer. | None. |
| **AD-21** | Maven single module | Standard for V1 monolith. | None. |
| **Deferred** | ArchUnit dependency rules | ArchUnit active; “when code exists” is fine. | None until CI added. |
| **Deferred** | Exact opaque-token codec | “Pick at implement” — **no version claim**; acceptable gap. | Crypto choice still needs implement-time review (not spine stale). |

---

## Drift & freshness flags (not wrong, but could age)

1. **Spring Boot 4.1.2 is fresh** (3 days old vs review date). No action unless team standardizes on **4.0.x** LTS cadence instead — 4.0.9 also patched 2026-10-22.
2. **Java 25 LTS** (Sep 2025) is the **current Oracle “latest LTS”**; Boot migration guide encourages latest LTS. **Java 21** remains valid; document **JDK distribution** (Eclipse Temurin, etc.) — **Oracle JDK 21** moves from NFTC to OTN for updates **after Sep 2026** ([Oracle blog](https://blogs.oracle.com/java/jdk-21-approaches-end-of-permissive-license)).
3. **MariaDB 13.x** is GA; staying on **12.3 LTS** is defensible for mailer DB but should be labeled **“chosen LTS, not latest major”** in Stack notes.
4. **Implicit BOM stack unpinned in spine:** Boot 4.1.2 → Spring Framework **7.0.10**, Spring Security **7.1.2**, Hibernate **7.4.13**, Jakarta EE **11** / Servlet **6.1** (Boot 4 baseline). Implementers inherit these without spine mention — **not stale**, but **unverified in prose** until first `pom.xml` / dependency report.
5. **Flyway footnote “12.4.0 at 4.1.0 baseline”** will read stale after Boot bumps Flyway in a future 4.1.x/4.2 — prefer **“see Boot 4.1.x BOM (`flyway.version`)”**.

---

## Research provenance gap (lens failure mode)

| Category | Count | Evidence |
| --- | --- | --- |
| **Stack version pins** | 5 | Memlog finalize note only; **this review** provides first documented external verification. |
| **Structural / product ADs (AD-1–AD-24)** | 24 | `.memlog.md` coaching decisions; **no** web citations, benchmarks, or compatibility studies in spine. |
| **Named third-party in ADs** | ShedLock (deferred), phpMyAdmin, Cloudflare (deploy) | Existence assumed; **not version-pinned** — acceptable at architecture altitude. |

If the initiative requires **audit trail that each AD was reality-checked**, add per-AD `verified:` tags only where external proof matters (crypto, mail protocols, Flyway, JWT libraries) — not for slice naming.

---

## Contradictions unrelated to versions (informational)

- `.memlog.md` line 48: “Campaign images … **CDN-cached at the edge**” vs spine **AD-23**: “CDN caching of `/dl` **not a V1 goal**”. Not a version issue; reconcile in product review (`reconcile-ticket.md` already flags R22 tension).

---

## Recommendations (minimal)

1. **Spine Stack:** Add `verified: 2026-10-25` and optional “MariaDB 12.3 LTS (not 13.x)” gloss; link Boot dependency coordinates for Flyway.
2. **AD-8 implement note:** Explicitly require **`spring-boot-starter-flyway`** + confirm **`org.flywaydb:flyway-mysql`** on classpath for MariaDB 12.3.
3. **JDK policy:** One line — target **Java 21** on **non-Oracle or Oracle OTN-aware** JDK for post-Oct-2026 updates.
4. **Do not re-pin** Boot/React/MariaDB patch levels in AD text; keep single Stack table as sole version source.

---

## Summary table

| Check | Result |
| --- | --- |
| Stack versions exist at stated numbers | **Pass** |
| Stack versions are reasonably current | **Pass** (Boot/React on latest patch; MariaDB on LTS maintenance) |
| Named tech in ADs still exists | **Pass** |
| Every AD web-researched | **Fail** (coaching-only; expected for most ADs) |
| Implement-time hidden deps documented | **Partial** (Flyway MariaDB module) |
