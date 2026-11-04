# Graph Report - java-list  (2026-11-04)

## Corpus Check
- 2587 files · ~2,213,798 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 185 file(s) not represented in the graph (top: .toml 146, .csv 20, (none) 11)

## Summary
- 2794 nodes · 9439 edges · 128 communities (75 shown, 53 thin omitted)
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 1433 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `99a77624`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- java / any / anyboolean
- ListRepository
- deleteByListId() / delete() / from()
- MtsIngestService
- SendingPathsController
- .verify
- deleteByCampaignId() / ResponseStatus / getStatus()
- org.springframework.web.bind.annotation.GetMapping
- environment
- apiJson
- BlockView / ConnectionTestRequest / ConnectionTestResponse
- CampaignEntity / CampaignEntity() / getCreatedAt()
- findAllByOrderByReceivedAtDesc() / BounceEventsController / 
- UsersPages.jsx
- Attachment / CampaignAttachmentEntity / CampaignAttachmentEn
- org.junit.jupiter.api.Test
- TimeZone.jsx
- textOf() / BounceCorrelationService / classify()
- MtsDropLogEntity
- org.springframework.transaction.annotation.Transactional
- java / authenticator / bytearrayinputstream
- AesGcmOpaqueTokenHelper.java
- BounceMailboxEntity
- mailer
- UserRepository
- UnsubscribeTracerTest.java
- 0 / 4 / 1
- SendQueueEntity
- setTimeZone() / MeAccountController / changePassword()
- mjs / js / jsx
- List Mailer — user guide
- Part / Charset / Attachment
- ActivityLogRepository
- disable() / MemberEntity / getEmail()
- ParametersPort
- Pk / approve() / open()
- NoticeSendingPathEntity
- SecurityConfig.java
- Connection / BaseJavaMigration / Context
- java / jwsheader / jwtencoderparameters
- java / tick() / Scheduled
- findIdsByStatus() / BounceProcessController() / runNow()
- from() / FooterEntity / FooterEntity()
- load() / jsx / ActivityLogPage()
- UsersPage
- deleteSeen() / poll() / HandleResult
- RawMessage / existsByEmail() / existsByInboxIdAndEmail()
- from() / AuditLogEntity / AuditLogEntity()
- CatalogPages.jsx
- App.jsx
- Pk / MappingSummary / allMappings()
- with
- SendingPathEntity
- EnabledUserFilter.java
- SecretStore
- list
- Pk / getCampaignId() / getListId()
- BounceCorrelationServiceTest
- AttentionController / ack() / AttentionController()
- SecretEntity
- Pk / Override / MtsAuthorizedSenderEntity
- ParametersProbeController
- UserRepository
- .currentVersion
- .seedParameters
- jsx / BADGE / formatBytes()
- .testImap
- MfaService
- ParameterEntity
- BouncePollService
- map
- BootstrapAuthProperties / InstallAdminConfig / installAdmin(
- MailSecurity / check() / imap()
- MtsFromDomainIssueEntity / getCreatedAt() / getDetail()
- catalogUi.jsx
- UnsubscribeController / get() / html()
- UserListGrantEntity / getListId() / getUserId()
- SecretsProperties
- Compose
- .newGuid
- .add
- SendingPathsPage
- RemoteImageFetchService
- Override / Pk / equals()
- BounceCorrelationService
- MembersPage
- OpsLogEntity / getCategory() / getCreatedAt()
- AttentionItemRepository
- BounceErrorExtractor / BounceErrorExtractor() / extract()
- ActivityLogEntity
- UserBackupCodeEntity / getCodeHash() / getId()
- host / port / yml
- .delete
- ListsPage
- Authentication / CodeRequest / MfaController
- .newGuid
- CampaignOpenEntity / CampaignOpenEntity() / getCampaignId()
- MtsBlockEntity / getCreatedAt() / getId()
- JavaMailSmtpAdapter
- json / plugins / rules
- SpringBootApplication / EnableScheduling / springapplication
- LoggingOpsHooks
- UserEntity
- BounceMailboxesPage
- Shell.jsx
- Pk
- .read
- MtsGateResult
- Hot-path DB indexes for bounce correlation and joins
- listmailer:mailer
- Totp
- HotPathIndexesMigrationTest.java
- AuditController
- SendQueueProcessor
- .open
- Secrets active key version and 1024 key slots
- Site title parameter (nav + MFA issuer)
- Collapsible nav categories; Campaigns before Lists
- FootersPage
- ParametersPage
- AGENTS.md

## God Nodes (most connected - your core abstractions)
1. `ParametersPort` - 89 edges
2. `apiJson()` - 82 edges
3. `MtsAdminController` - 66 edges
4. `ListRepository` - 64 edges
5. `ListEntity` - 63 edges
6. `OpsHooksPort` - 63 edges
7. `CampaignSendService` - 59 edges
8. `CampaignsController` - 59 edges
9. `ListAccessService` - 58 edges
10. `MemberRepository` - 53 edges

## Surprising Connections (you probably didn't know these)
- `saveNotifyEmails()` --calls--> `apiJson()`  [EXTRACTED]
  mailer/frontend/src/UsersPages.jsx → mailer/frontend/src/api.js
- `saveTimeZone()` --calls--> `apiJson()`  [EXTRACTED]
  mailer/frontend/src/UsersPages.jsx → mailer/frontend/src/api.js
- `requestConsent()` --calls--> `apiJson()`  [EXTRACTED]
  mailer/frontend/src/ListsMembersPages.jsx → mailer/frontend/src/api.js
- `bulkAdd()` --calls--> `apiJson()`  [EXTRACTED]
  mailer/frontend/src/ListsMembersPages.jsx → mailer/frontend/src/api.js
- `BounceCorrelationService` --references--> `SendQueueRepository`  [EXTRACTED]
  mailer/src/main/java/com/listmailer/bounce/BounceCorrelationService.java → mailer/src/main/java/com/listmailer/campaigns/SendQueueRepository.java

## Import Cycles
- None detected.

## Communities (128 total, 53 thin omitted)

### Community 0 - "java / any / anyboolean"
Cohesion: 0.13
Nodes (5): OpenPixelControllerTest, AdminNoticeMailTest, AuditRetentionJobTest, OpsRetentionJobTest, ListUnsubscribePostTest

### Community 1 - "ListRepository"
Cohesion: 0.12
Nodes (32): BounceEventRepository, CampaignAttachmentRepository, CampaignListRepository, CampaignOpenRepository, CampaignRepository, CampaignSendService, SendPauseCache, CatalogReferenceRepository (+24 more)

### Community 2 - "deleteByListId() / delete() / from()"
Cohesion: 0.09
Nodes (7): ListEntity, ListDetail, ListRequest, ListsController, ListSummary, MemberRow, ListAccessServiceTest

### Community 3 - "MtsIngestService"
Cohesion: 0.09
Nodes (3): InboxBody, MtsInboxEntity, MtsIngestService

### Community 4 - "SendingPathsController"
Cohesion: 0.12
Nodes (6): ConnectionTestRequest, PathDetail, PathRequest, PathSummary, SendingPathsController, UsedBy

### Community 6 - "deleteByCampaignId() / ResponseStatus / getStatus()"
Cohesion: 0.13
Nodes (11): AddListBody, AttachmentIn, AttachmentOut, CampaignDetail, CampaignListRef, CampaignsController, CreateBody, MemberOpenStat (+3 more)

### Community 7 - "org.springframework.web.bind.annotation.GetMapping"
Cohesion: 0.15
Nodes (5): BounceProcessController, ApiPingController, ActivityLogsController, EnabledView, SecretsAdminController

### Community 8 - "environment"
Cohesion: 0.05
Nodes (48): build, depends_on, environment, healthcheck, ports, context, mariadb, GREENMAIL_OPTS (+40 more)

### Community 9 - "apiJson"
Cohesion: 0.10
Nodes (42): apiJson(), CampaignsPage(), addList(), load(), pause(), queue(), remove(), requeue() (+34 more)

### Community 10 - "BlockView / ConnectionTestRequest / ConnectionTestResponse"
Cohesion: 0.10
Nodes (12): BlockView, ConnectionTestRequest, ConnectionTestResponse, DropView, GrantRow, GrantsBody, GrantView, InboxDetail (+4 more)

### Community 11 - "CampaignEntity / CampaignEntity() / getCreatedAt()"
Cohesion: 0.08
Nodes (9): CampaignEntity, CampaignSummary, CampaignStatus, DRAFT, FINISHED, PAUSED, QUEUED, SENDING (+1 more)

### Community 12 - "findAllByOrderByReceivedAtDesc() / BounceEventsController / "
Cohesion: 0.18
Nodes (3): BounceEventsController, BounceMessageDetail, BounceMessageView

### Community 13 - "UsersPages.jsx"
Cohesion: 0.13
Nodes (28): ConfirmDialog(), CREATE_ID, isCreatePage(), FieldDesc(), fieldHelp, listTimeZones(), btnDirtyClass(), dirtyClass() (+20 more)

### Community 15 - "Attachment / CampaignAttachmentEntity / CampaignAttachmentEn"
Cohesion: 0.10
Nodes (6): CampaignAttachmentEntity, InlineImagePlanner, Plan, PublicDlController, InlineImagePlannerTest, PublicDlControllerTest

### Community 16 - "org.junit.jupiter.api.Test"
Cohesion: 0.08
Nodes (3): GuidsTest, MtsEmbedImagesCopyTest, TotpTest

### Community 17 - "TimeZone.jsx"
Cohesion: 0.33
Nodes (6): formatWhen(), colombo, utc, pollIndexColumns(), TimeZoneContext, TimeZoneProvider()

### Community 25 - "mailer"
Cohesion: 0.04
Nodes (45): name, email, password, correlate-tick-ms, poll-tick-ms, password, url, username (+37 more)

### Community 26 - "UserRepository"
Cohesion: 0.15
Nodes (8): UserListGrantRepository, CreateUserRequest, GrantRequest, GrantResponse, PatchUserRequest, UserResponse, UsersAdminController, UsersAdminControllerTest

### Community 27 - "UnsubscribeTracerTest.java"
Cohesion: 0.07
Nodes (5): NavIaC8Test, CampaignsE2ETest, MtsIngestTracerTest, ObservabilityE2ETest, UnsubscribeTracerTest

### Community 29 - "0 / 4 / 1"
Cohesion: 0.08
Nodes (27): dependencies, qrcode@^1.5.4, react@^19.3.1, react-dom@^19.3.1, devDependencies, oxlint@^1.88.0, @types/react@^19.3.0, @types/react-dom@^19.3.0 (+19 more)

### Community 31 - "setTimeZone() / MeAccountController / changePassword()"
Cohesion: 0.17
Nodes (5): MeAccountController, NotificationEmailsRequest, PasswordChangeRequest, TimeZoneRequest, MeAccountControllerTest

### Community 32 - "mjs / js / jsx"
Cohesion: 0.11
Nodes (11): decodeJwtPayload(), roleFromToken(), TOKEN_KEY, formatBooleanString(), isBooleanString(), parseBooleanString(), CONSENT_REQUEST_HINT, feature (+3 more)

### Community 33 - "List Mailer — user guide"
Cohesion: 0.05
Nodes (39): Already covered (pre-V45), Database indexes (hot paths), V45 additions (regular correlate / process / join), Campaigns end-to-end suite (story 5.10), Compose shapes, Deploy notes (platform), HTTPS / TLS, Published app image (no database) (+31 more)

### Community 34 - "Part / Charset / Attachment"
Cohesion: 0.17
Nodes (5): Attachment, Collector, MimeBodyParser, Parsed, MimeBodyParserTest

### Community 35 - "ActivityLogRepository"
Cohesion: 0.19
Nodes (4): ActivityLogRepository, ActivityLogRetentionJob, UserBackupCodeRepository, ActivityLogRetentionJobTest

### Community 36 - "disable() / MemberEntity / getEmail()"
Cohesion: 0.15
Nodes (8): MemberEntity, ConsentClearBody, MemberDetail, MembersController, Membership, MemberSummary, PatchBody, ReAddBody

### Community 37 - "ParametersPort"
Cohesion: 0.10
Nodes (25): ConsentService, OpsHooksPort, ParametersPort, OpaqueTokenHelper, BulkAddMembersService, ListMemberRepository, ListMembersController, MemberRepository (+17 more)

### Community 38 - "Pk / approve() / open()"
Cohesion: 0.10
Nodes (12): ApproveController, ApproveOutcome, ALREADY_CLEAR, CLEARED, EXPIRED, INVALID, ConsentTokenCodec, Payload (+4 more)

### Community 39 - "NoticeSendingPathEntity"
Cohesion: 0.39
Nodes (3): NoticeBody, NoticeSendingPathController, NoticeSendingPathEntity

### Community 40 - "SecurityConfig.java"
Cohesion: 0.09
Nodes (4): SecurityConfig, KernelStubConfig, SchedulerHost, PasswordConfig

### Community 43 - "java / tick() / Scheduled"
Cohesion: 0.12
Nodes (7): BounceCorrelationProcessor, BouncePollProcessor, CampaignHtmlRewriter, MtsPollProcessor, AuditRetentionJob, OpsRetentionJob, CampaignHtmlRewriterTest

### Community 44 - "findIdsByStatus() / BounceProcessController() / runNow()"
Cohesion: 0.12
Nodes (7): BounceProcessProcessor, BounceProcessService, StartOutcome, ALREADY_RUNNING, SKIPPED, STARTED, BounceProcessControllerTest

### Community 45 - "from() / FooterEntity / FooterEntity()"
Cohesion: 0.09
Nodes (11): CatalogReferenceEntity, CatalogTargetType, BOUNCE_MAILBOX, FOOTER, SENDING_PATH, FooterEntity, FooterDetail, FooterRequest (+3 more)

### Community 46 - "load() / jsx / ActivityLogPage()"
Cohesion: 0.18
Nodes (20): ActivityLogPage(), AttentionInbox(), ack(), load(), AuditLog(), bounceIdFromAttention(), MainView(), OpsLogs() (+12 more)

### Community 47 - "UsersPage"
Cohesion: 0.22
Nodes (13): passwordChangeProblem(), check(), resetToBlank(), flagsForRole(), grantsSig(), isGranted(), changePassword(), UsersPage() (+5 more)

### Community 48 - "deleteSeen() / poll() / HandleResult"
Cohesion: 0.13
Nodes (9): HandleResult, DELETE, LEAVE, SEEN, MailboxConn, MailboxPort, MessageHandler, ImapMailboxQuery (+1 more)

### Community 49 - "RawMessage / existsByEmail() / existsByInboxIdAndEmail()"
Cohesion: 0.22
Nodes (6): RawMessage, MtsAuthorizedSenderRepository, MtsEmails, Decision, MtsGateService, MtsGateServiceTest

### Community 52 - "App.jsx"
Cohesion: 0.09
Nodes (4): App(), AppInner(), LoginCard(), fetchSiteTitle()

### Community 53 - "Pk / MappingSummary / allMappings()"
Cohesion: 0.15
Nodes (3): MappingSummary, MtsSenderListGrantEntity, Pk

### Community 54 - "with"
Cohesion: 0.07
Nodes (27): jobs, publish, name, on, release, permissions, contents, packages (+19 more)

### Community 56 - "EnabledUserFilter.java"
Cohesion: 0.05
Nodes (11): FilterChainRegistry, ChainContinuation, PluginFilter, EnabledUserFilter, RoleJwtConverter, HttpFilterChainInterceptor, SpaWebFilter, WebMvcKernelConfig (+3 more)

### Community 57 - "SecretStore"
Cohesion: 0.18
Nodes (4): SecretRepository, RewrapResult, SecretStore, SecretStoreTest

### Community 58 - "list"
Cohesion: 0.10
Nodes (8): NoticeSendingPathRepository, SmtpPort, MtsBlockRepository, MtsDropLogRepository, MtsDropService, NoticeMailService, MtsDropServiceTest, NoticeMailServiceTest

### Community 64 - "ParametersProbeController"
Cohesion: 0.17
Nodes (7): ParametersProbeController, ParameterView, ValueBody, Def, ParameterDefinitions, ParametersSiteTitlePutTest, ParametersPort

### Community 65 - "UserRepository"
Cohesion: 0.27
Nodes (3): AuthController, LoginRequest, MfaTokenStore

### Community 67 - ".seedParameters"
Cohesion: 0.31
Nodes (3): ParametersSeedConfig, ParametersSeedConfigTest, ParametersPort

### Community 68 - "jsx / BADGE / formatBytes()"
Cohesion: 0.29
Nodes (11): BADGE, formatBytes(), StatusBadge(), CAMPAIGN_STATUS_LABEL, CAMPAIGN_STATUSES, campaignStatusLabel(), canDeleteCampaign(), canPauseCampaign() (+3 more)

### Community 69 - ".testImap"
Cohesion: 0.16
Nodes (5): ConnectionTestRequest, ConnectionTestResponse, ConnectionTestResponse, Result, MailConnectionProbeTest

### Community 70 - "MfaService"
Cohesion: 0.23
Nodes (3): MfaService, Setup, MfaServiceSiteTitleTest

### Community 71 - "ParameterEntity"
Cohesion: 0.19
Nodes (3): JpaParametersPort, ParameterEntity, ParameterRepository

### Community 72 - "BouncePollService"
Cohesion: 0.19
Nodes (9): BouncePollService, BounceMailboxesController, MailboxDetail, MailboxRequest, MailboxSummary, BounceMailboxRepository, MailboxPollLock, BounceMailboxConnectionTest (+1 more)

### Community 73 - "map"
Cohesion: 0.14
Nodes (4): ParameterKeys, PublicSiteController, SiteTitles, SiteTitlesTest

### Community 74 - "BootstrapAuthProperties / InstallAdminConfig / installAdmin("
Cohesion: 0.31
Nodes (5): BootstrapAuthProperties, JwtProperties, InstallAdminConfig, UserRepository, InstallAdminConfigTest

### Community 78 - "UnsubscribeController / get() / html()"
Cohesion: 0.13
Nodes (7): OpenPixelController, PublicHtmlPage, UnsubscribeController, Outcome, ALREADY, INVALID, UNSUBSCRIBED

### Community 80 - "SecretsProperties"
Cohesion: 0.24
Nodes (4): SecretsConfig, KeyEntry, SecretsProperties, SecretsConfigTest

### Community 81 - "Compose"
Cohesion: 0.29
Nodes (7): authHeaders(), Compose(), bodyPayload(), clearPickedFile(), fileToAttachment(), saveDraft(), useUnsavedGate()

### Community 82 - ".newGuid"
Cohesion: 0.16
Nodes (6): AttentionItemRepository, OpsLogRepository, OpsLogsController, OpsLogView, PersistingOpsHooks, PersistingOpsHooksTest

### Community 84 - "SendingPathsPage"
Cohesion: 0.22
Nodes (10): SendingPathsPage(), create(), load(), pathAccountFields(), pathBase(), remove(), ACRONYMS, fieldLabel() (+2 more)

### Community 85 - "RemoteImageFetchService"
Cohesion: 0.12
Nodes (7): ImageFetchMode, BEST_EFFORT, MANDATORY, OFF, RemoteImageFetchService, SsrfGuard, SsrfGuardTest

### Community 88 - "MembersPage"
Cohesion: 0.25
Nodes (8): emptyHint(), MembersPage(), disable(), enable(), refreshDetail(), requestConsent(), search(), membersSearchPath()

### Community 95 - "host / port / yml"
Cohesion: 0.25
Nodes (7): host, port, mailer, imap, smtp, host, port

### Community 97 - "ListsPage"
Cohesion: 0.43
Nodes (7): ListsPage(), bulkAdd(), confirmDeleteList(), create(), load(), save(), adopt()

### Community 102 - "JavaMailSmtpAdapter"
Cohesion: 0.22
Nodes (3): Attachment, SmtpMessage, JavaMailSmtpAdapter

### Community 103 - "json / plugins / rules"
Cohesion: 0.33
Nodes (5): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema

### Community 106 - "UserEntity"
Cohesion: 0.15
Nodes (6): MfaRequest, UserEntity, UserRole, ADMIN, LIST_MANAGER, LIST_SENDER

### Community 107 - "BounceMailboxesPage"
Cohesion: 0.33
Nodes (7): BounceMailboxesPage(), bounceAccountFields(), bounceBase(), create(), load(), pollFromIndex(), remove()

### Community 108 - "Shell.jsx"
Cohesion: 0.21
Nodes (9): NAV_DEFAULT_OPEN, NAV_SECTIONS, FORBIDDEN_SIDEBAR_LABELS, visibleSections(), sectionIdForView(), Shell(), closeNav(), go() (+1 more)

### Community 111 - "MtsGateResult"
Cohesion: 0.29
Nodes (6): MtsGateResult, ACCEPT, DROP_COUNTED, DROP_SILENT, FROM_DOMAIN_ISSUE, NOTIFY_CROSS_INBOX

### Community 112 - "Hot-path DB indexes for bounce correlation and joins"
Cohesion: 0.40
Nodes (4): Boundaries, Description, Hot-path DB indexes for bounce correlation and joins, Requirements

### Community 119 - "SendQueueProcessor"
Cohesion: 0.20
Nodes (3): SendLimits, SendQueueProcessor, SendLimitsTest

### Community 121 - "Secrets active key version and 1024 key slots"
Cohesion: 0.40
Nodes (4): Boundaries, Description, Requirements, Secrets active key version and 1024 key slots

### Community 122 - "Site title parameter (nav + MFA issuer)"
Cohesion: 0.40
Nodes (4): Boundaries, Description, Requirements, Site title parameter (nav + MFA issuer)

### Community 123 - "Collapsible nav categories; Campaigns before Lists"
Cohesion: 0.40
Nodes (4): Boundaries, Collapsible nav categories; Campaigns before Lists, Description, Requirements

### Community 124 - "FootersPage"
Cohesion: 0.60
Nodes (4): FootersPage(), create(), load(), remove()

### Community 126 - "ParametersPage"
Cohesion: 0.67
Nodes (3): ParametersPage(), loadSecretsStatus(), rewrapSecrets()

## Knowledge Gaps
- **34 isolated node(s):** `ADMIN`, `LIST_MANAGER`, `LIST_SENDER`, `DRAFT`, `FINISHED` (+29 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 520 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **53 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AttentionItemEntity` connect `AttentionController / ack() / AttentionController()` to `.newGuid`, `java / column / enumerated`, `findAllByOrderByReceivedAtDesc() / BounceEventsController / `?**
  _High betweenness centrality (0.030) - this node is a cross-community bridge._
- **What connects `ADMIN`, `LIST_MANAGER`, `LIST_SENDER` to the rest of the system?**
  _34 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `java / any / anyboolean` be split into smaller, more focused modules?**
  _Cohesion score 0.12512768130745658 - nodes in this community are weakly interconnected._
- **Why does `ParametersPort` connect `ParametersPort` to `java / any / anyboolean`, `ListRepository`, `.verify`, `deleteByCampaignId() / ResponseStatus / getStatus()`, `org.springframework.web.bind.annotation.GetMapping`, `BlockView / ConnectionTestRequest / ConnectionTestResponse`, `stringutils`, `org.springframework.transaction.annotation.Transactional`, `ActivityLogRepository`, `SecurityConfig.java`, `java / jwsheader / jwtencoderparameters`, `java / tick() / Scheduled`, `findIdsByStatus() / BounceProcessController() / runNow()`, `EnabledUserFilter.java`, `list`, `UserRepository`, `.seedParameters`, `ParameterEntity`, `RemoteImageFetchService`, `Authentication / CodeRequest / MfaController`, `.newGuid`, `SendQueueProcessor`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Should `ListRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.12202380952380952 - nodes in this community are weakly interconnected._
- **Why does `MtsAdminController` connect `BlockView / ConnectionTestRequest / ConnectionTestResponse` to `ListRepository`, `MtsIngestService`, `ParametersPort`, `org.springframework.web.bind.annotation.GetMapping`, `BouncePollService`, `MtsFromDomainIssueEntity / getCreatedAt() / getDetail()`, `RawMessage / existsByEmail() / existsByInboxIdAndEmail()`, `.opsLog`, `org.springframework.transaction.annotation.Transactional`, `Pk / MappingSummary / allMappings()`, `list`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Should `deleteByListId() / delete() / from()` be split into smaller, more focused modules?**
  _Cohesion score 0.09294199860237597 - nodes in this community are weakly interconnected._