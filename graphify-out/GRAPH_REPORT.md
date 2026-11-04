# Graph Report - mailer  (2026-11-04)

## Corpus Check
- 352 files · ~74,138 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 4 file(s) not represented in the graph (top: (none) 3, .css 1)

## Summary
- 2535 nodes · 9551 edges · 114 communities (69 shown, 45 thin omitted)
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 1539 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- java / any / anyboolean
- java / deleteByMemberId() / deleteByCampaignId()
- deleteByListId() / delete() / from()
- getId() / attentionAdmin() / opsLog()
- usedBy() / from() / findByTargetTypeAndTargetId()
- findByCampaignId() / TransactionCallback / countBySendQueueI
- deleteByCampaignId() / ResponseStatus / getStatus()
- java / authentication / bigdecimal
- password / poll-tick-ms / secret
- load() / js / apiJson()
- BlockView / ConnectionTestRequest / ConnectionTestResponse
- CampaignEntity / CampaignEntity() / getCreatedAt()
- findAllByOrderByReceivedAtDesc() / BounceEventsController / 
- js / jsx / mjs
- java / unblock() / dataintegrityviolationexception
- Attachment / CampaignAttachmentEntity / CampaignAttachmentEn
- newGuid() / OpsLogRepository / deleteOlderThan()
- jsx / js / ConfirmDialog()
- textOf() / BounceCorrelationService / classify()
- blockDomain() / java / resolveDrop()
- java / arraylist / atomicboolean
- java / authenticator / bytearrayinputstream
- java / base64 / bytebuffer
- from() / due() / mailboxRef()
- notifyBouncesReceived() / deliver() / injectTracking()
- java / delete() / completablefuture
- PatchMapping / getRole() / isEnabled()
- java / props() / autowired
- 0 / 4 / 1
- testConnection() / ConnectionTestResponse / BouncePollServic
- setTimeZone() / MeAccountController / changePassword()
- mjs / js / jsx
- jsx / App() / AppInner()
- Part / Charset / Attachment
- java / Modifying / Query
- disable() / MemberEntity / getEmail()
- java / ApproveController() / ConsentService
- Pk / approve() / open()
- java / send() / NoticeBody
- java / bearertokenauthenticationfilter / httpmethod
- Connection / BaseJavaMigration / Context
- java / jwsheader / jwtencoderparameters
- java / tick() / Scheduled
- findIdsByStatus() / BounceProcessController() / runNow()
- from() / FooterEntity / FooterEntity()
- load() / jsx / ActivityLogPage()
- create() / load() / save()
- deleteSeen() / poll() / HandleResult
- RawMessage / existsByEmail() / existsByInboxIdAndEmail()
- from() / AuditLogEntity / AuditLogEntity()
- SecretsConfig / keysFromProperties() / secretsCrypto()
- java / assertdoesnotthrow / inetaddress
- Pk / MappingSummary / allMappings()
- BouncePollService / breakLock() / finish()
- MfaController() / MfaService / confirmSetup()
- Override / java / doFilterInternal()
- DisplayName / getId() / countByCampaignId()
- java / clearIssue() / MtsAdminController()
- Pk / getCampaignId() / getListId()
- correlatePending() / getCorrelationSignal() / getMemberId()
- AttentionController / ack() / AttentionController()
- SecretEntity / getCiphertext() / getCreatedAt()
- Pk / Override / MtsAuthorizedSenderEntity
- put() / clearGlobal() / ParametersProbeController
- ParametersProbeController() / Override / JpaParametersPort
- Override / MailConnectionProbe / rootMessage()
- java / bcryptpasswordencoder / Bean
- jsx / BADGE / formatBytes()
- invoke() / invokeAt() / register()
- countByKeyVersionLessThan() / status() / migrateSecretsOnSta
- collection / grantedauthority / jwtauthenticationtoken
- java / SendLimits / allowed()
- expiryDays() / requestClearUnsub() / resolveUnsubLists()
- BootstrapAuthProperties / InstallAdminConfig / installAdmin(
- MailSecurity / check() / imap()
- MtsFromDomainIssueEntity / getCreatedAt() / getDetail()
- SendingPathsPage() / create() / load()
- UnsubscribeController / get() / html()
- UserListGrantEntity / getListId() / getUserId()
- java / arrays / ApplicationRunner
- authHeaders() / Compose() / bodyPayload()
- js / formatBooleanString() / isBooleanString()
- MembersPage() / disable() / enable()
- Override / Pk / equals()
- ActivityLogEntity / ActivityLogEntity() / getCreatedAt()
- Override / LoggingOpsHooks / attentionAdmin()
- OpsLogEntity / getCategory() / getCreatedAt()
- Override / Pk / equals()
- BounceErrorExtractor / BounceErrorExtractor() / extract()
- CatalogReferenceEntity / CatalogReferenceEntity() / getId()
- UserBackupCodeEntity / getCodeHash() / getId()
- host / port / yml
- js / mtsSenderDetailId() / parseMtsDetailId()
- Multipart / UnsubscribeTracerTest / extractHtml()
- Authentication / CodeRequest / MfaController
- InterceptorRegistry / WebMvcConfigurer / java
- CampaignOpenEntity / CampaignOpenEntity() / getCampaignId()
- MtsBlockEntity / getCreatedAt() / getId()
- java / MtsGateResult / ACCEPT
- json / plugins / rules
- SpringBootApplication / EnableScheduling / springapplication
- findByCampaignIdAndFilename() / download() / PublicDlControl
- notifyQueueProcessed() / send() / findByRoleAndEnabledTrueAn
- java / ImageFetchMode / BEST_EFFORT
- AuditController / AuditController() / index()
- CampaignHtmlRewriter / rewrite() / CampaignHtmlRewriterTest
- ApproveOutcome / ALREADY_CLEAR / CLEARED
- shouldForward() / SpaWebFilterTest / doesNotForwardApiOrAsse
- Override / ParametersPort / get()
- listmailer:mailer

## God Nodes (most connected - your core abstractions)
1. `apiJson()` - 103 edges
2. `ParametersPort` - 92 edges
3. `OpsHooksPort` - 68 edges
4. `MtsAdminController` - 67 edges
5. `ListRepository` - 64 edges
6. `ListEntity` - 63 edges
7. `CampaignSendService` - 60 edges
8. `CampaignsController` - 60 edges
9. `SendQueueRepository` - 60 edges
10. `ListAccessService` - 58 edges

## Surprising Connections (you probably didn't know these)
- `save()` --calls--> `apiJson()`  [EXTRACTED]
  frontend/src/CatalogPages.jsx → frontend/src/api.js
- `save()` --calls--> `apiJson()`  [EXTRACTED]
  frontend/src/CatalogPages.jsx → frontend/src/api.js
- `bulkAdd()` --calls--> `apiJson()`  [EXTRACTED]
  frontend/src/ListsMembersPages.jsx → frontend/src/api.js
- `requestConsent()` --calls--> `apiJson()`  [EXTRACTED]
  frontend/src/ListsMembersPages.jsx → frontend/src/api.js
- `ActivityLogPage()` --calls--> `apiJson()`  [EXTRACTED]
  frontend/src/ActivityLogPage.jsx → frontend/src/api.js

## Import Cycles
- None detected.

## Communities (114 total, 45 thin omitted)

### Community 0 - "java / any / anyboolean"
Cohesion: 0.06
Nodes (21): ParametersSeedConfig, OpsHooksPort, ParameterKeys, ParametersPort, ActivityLogService, BounceProcessAsyncTest, BounceProcessServiceTest, SendLimitsTest (+13 more)

### Community 1 - "java / deleteByMemberId() / deleteByCampaignId()"
Cohesion: 0.11
Nodes (28): BounceEventRepository, CampaignAttachmentRepository, CampaignListRepository, CampaignOpenRepository, CampaignRepository, CampaignSendService, OpenPixelController, PublicDlController (+20 more)

### Community 2 - "deleteByListId() / delete() / from()"
Cohesion: 0.07
Nodes (7): ListEntity, ListDetail, ListRequest, ListsController, ListSummary, MemberRow, ListAccessServiceTest

### Community 4 - "usedBy() / from() / findByTargetTypeAndTargetId()"
Cohesion: 0.08
Nodes (9): SendingPathEntity, ConnectionTestRequest, PathDetail, PathRequest, PathSummary, SendingPathsController, UsedBy, Authenticator (+1 more)

### Community 6 - "deleteByCampaignId() / ResponseStatus / getStatus()"
Cohesion: 0.12
Nodes (11): AddListBody, AttachmentIn, AttachmentOut, CampaignDetail, CampaignListRef, CampaignsController, CreateBody, MemberOpenStat (+3 more)

### Community 7 - "java / authentication / bigdecimal"
Cohesion: 0.18
Nodes (3): BounceProcessController, ApiPingController, SecretsAdminController

### Community 8 - "password / poll-tick-ms / secret"
Cohesion: 0.04
Nodes (45): name, email, password, correlate-tick-ms, poll-tick-ms, password, url, username (+37 more)

### Community 9 - "load() / js / apiJson()"
Cohesion: 0.09
Nodes (42): apiJson(), CampaignsPage(), addList(), load(), pause(), queue(), remove(), requeue() (+34 more)

### Community 10 - "BlockView / ConnectionTestRequest / ConnectionTestResponse"
Cohesion: 0.10
Nodes (13): BlockView, ConnectionTestRequest, ConnectionTestResponse, DropView, GrantRow, GrantsBody, GrantView, InboxDetail (+5 more)

### Community 11 - "CampaignEntity / CampaignEntity() / getCreatedAt()"
Cohesion: 0.08
Nodes (10): CampaignEntity, CampaignSummary, CampaignStatus, DRAFT, FINISHED, PAUSED, QUEUED, SENDING (+2 more)

### Community 12 - "findAllByOrderByReceivedAtDesc() / BounceEventsController / "
Cohesion: 0.15
Nodes (8): BounceEventsController, BounceMessageDetail, BounceMessageView, AttentionItemRepository, AttentionService, ListAccessService, BounceEventsControllerTest, AttentionServiceTest

### Community 13 - "js / jsx / mjs"
Cohesion: 0.14
Nodes (29): bounceAccountFields(), NoticePathPage(), pathAccountFields(), ConnectionTestBanner(), FieldDesc(), ACRONYMS, fieldLabel(), PHRASES (+21 more)

### Community 15 - "Attachment / CampaignAttachmentEntity / CampaignAttachmentEn"
Cohesion: 0.10
Nodes (4): CampaignAttachmentEntity, InlineImagePlanner, Plan, InlineImagePlannerTest

### Community 16 - "newGuid() / OpsLogRepository / deleteOlderThan()"
Cohesion: 0.11
Nodes (8): OpsLogRepository, OpsLogsController, OpsLogView, OpsRetentionJob, PersistingOpsHooks, RetentionDays, ObservabilityE2ETest, PersistingOpsHooksTest

### Community 17 - "jsx / js / ConfirmDialog()"
Cohesion: 0.18
Nodes (17): ConfirmDialog(), CREATE_ID, isCreatePage(), emptyHint(), fieldHelp, formatWhen(), MailboxPollPanel(), MailboxProcessingLog() (+9 more)

### Community 18 - "textOf() / BounceCorrelationService / classify()"
Cohesion: 0.15
Nodes (3): BounceCorrelationService, Match, BounceEventEntity

### Community 19 - "blockDomain() / java / resolveDrop()"
Cohesion: 0.09
Nodes (4): MtsBlockRepository, MtsDropLogEntity, MtsDropLogRepository, MtsDropService

### Community 20 - "java / arraylist / atomicboolean"
Cohesion: 0.14
Nodes (3): MailboxQueryException, AdminNoticeMail, NoticeMailService

### Community 22 - "java / base64 / bytebuffer"
Cohesion: 0.10
Nodes (4): AesGcmOpaqueTokenHelper, Totp, AesGcmOpaqueTokenHelperTest, TotpTest

### Community 25 - "java / delete() / completablefuture"
Cohesion: 0.11
Nodes (6): CatalogTargetType, BOUNCE_MAILBOX, FOOTER, SENDING_PATH, ApproveController, PublicHtmlPage

### Community 26 - "PatchMapping / getRole() / isEnabled()"
Cohesion: 0.12
Nodes (10): UserRole, ADMIN, LIST_MANAGER, LIST_SENDER, CreateUserRequest, GrantRequest, PatchUserRequest, UserResponse (+2 more)

### Community 29 - "0 / 4 / 1"
Cohesion: 0.09
Nodes (27): dependencies, qrcode@^1.5.4, react@^19.3.1, react-dom@^19.3.1, devDependencies, oxlint@^1.88.0, @types/react@^19.3.0, @types/react-dom@^19.3.0 (+19 more)

### Community 30 - "testConnection() / ConnectionTestResponse / BouncePollServic"
Cohesion: 0.15
Nodes (11): BounceMailboxesController, ConnectionTestRequest, ConnectionTestResponse, MailboxDetail, MailboxRequest, BounceMailboxRepository, ConnectionTestResponse, MailboxPollLock (+3 more)

### Community 31 - "setTimeZone() / MeAccountController / changePassword()"
Cohesion: 0.15
Nodes (5): MeAccountController, NotificationEmailsRequest, PasswordChangeRequest, TimeZoneRequest, MeAccountControllerTest

### Community 32 - "mjs / js / jsx"
Cohesion: 0.11
Nodes (11): CONSENT_REQUEST_HINT, feature, root, colombo, utc, MfaQr(), otpAuthQrDataUrl(), NAV_SECTIONS (+3 more)

### Community 33 - "jsx / App() / AppInner()"
Cohesion: 0.13
Nodes (12): App(), AppInner(), acceptToken(), decodeJwtPayload(), roleFromToken(), TOKEN_KEY, LoginCard(), Shell() (+4 more)

### Community 34 - "Part / Charset / Attachment"
Cohesion: 0.16
Nodes (5): Attachment, Collector, MimeBodyParser, Parsed, MimeBodyParserTest

### Community 35 - "java / Modifying / Query"
Cohesion: 0.15
Nodes (5): ActivityLogRepository, ActivityLogRetentionJob, ActivityLogsController, ActivityLogView, EnabledView

### Community 36 - "disable() / MemberEntity / getEmail()"
Cohesion: 0.18
Nodes (8): MemberEntity, ConsentClearBody, MemberDetail, MembersController, Membership, MemberSummary, PatchBody, ReAddBody

### Community 37 - "java / ApproveController() / ConsentService"
Cohesion: 0.19
Nodes (12): ConsentService, OpaqueTokenHelper, BulkAddMembersService, Result, ListMemberRepository, BulkBody, ListMembersController, MemberRepository (+4 more)

### Community 38 - "Pk / approve() / open()"
Cohesion: 0.21
Nodes (4): ListMemberEntity, Payload, UnsubTokenCodec, UnsubscribeServiceTest

### Community 39 - "java / send() / NoticeBody"
Cohesion: 0.14
Nodes (8): NoticeBody, NoticeSendingPathController, NoticeSendingPathEntity, NoticeSendingPathRepository, Attachment, SmtpMessage, SmtpPort, NoticeMailServiceTest

### Community 41 - "Connection / BaseJavaMigration / Context"
Cohesion: 0.17
Nodes (3): SecretsCrypto, V41__MigratePlaintextSecrets, SecretsCryptoTest

### Community 42 - "java / jwsheader / jwtencoderparameters"
Cohesion: 0.15
Nodes (5): AccessTokenService, AuthController, LoginRequest, MfaRequest, MfaTokenStore

### Community 43 - "java / tick() / Scheduled"
Cohesion: 0.15
Nodes (6): BounceCorrelationProcessor, BouncePollProcessor, BounceProcessProcessor, FilterChainRegistry, HttpFilterChainInterceptor, MtsPollProcessor

### Community 44 - "findIdsByStatus() / BounceProcessController() / runNow()"
Cohesion: 0.17
Nodes (6): BounceProcessService, StartOutcome, ALREADY_RUNNING, SKIPPED, STARTED, BounceProcessControllerTest

### Community 45 - "from() / FooterEntity / FooterEntity()"
Cohesion: 0.21
Nodes (6): FooterEntity, FooterDetail, FooterRequest, FootersController, FooterSummary, UsedBy

### Community 46 - "load() / jsx / ActivityLogPage()"
Cohesion: 0.18
Nodes (19): ActivityLogPage(), AttentionInbox(), ack(), load(), AuditLog(), bounceIdFromAttention(), MainView(), OpsLogs() (+11 more)

### Community 47 - "create() / load() / save()"
Cohesion: 0.16
Nodes (22): BounceMailboxesPage(), bounceBase(), create(), load(), pollFromIndex(), remove(), runBounceImapTest(), save() (+14 more)

### Community 48 - "deleteSeen() / poll() / HandleResult"
Cohesion: 0.16
Nodes (9): HandleResult, DELETE, LEAVE, SEEN, MailboxConn, MailboxPort, MessageHandler, ImapMailboxQuery (+1 more)

### Community 49 - "RawMessage / existsByEmail() / existsByInboxIdAndEmail()"
Cohesion: 0.23
Nodes (5): RawMessage, MtsEmails, Decision, MtsGateService, MtsGateServiceTest

### Community 50 - "from() / AuditLogEntity / AuditLogEntity()"
Cohesion: 0.16
Nodes (3): AuditLogEntity, AuditLogRepository, AuditRetentionJob

### Community 51 - "SecretsConfig / keysFromProperties() / secretsCrypto()"
Cohesion: 0.32
Nodes (4): SecretsConfig, KeyEntry, SecretsProperties, SecretsConfigTest

### Community 52 - "java / assertdoesnotthrow / inetaddress"
Cohesion: 0.16
Nodes (3): RemoteImageFetchService, SsrfGuard, SsrfGuardTest

### Community 53 - "Pk / MappingSummary / allMappings()"
Cohesion: 0.15
Nodes (3): MappingSummary, MtsSenderListGrantEntity, Pk

### Community 55 - "MfaController() / MfaService / confirmSetup()"
Cohesion: 0.24
Nodes (4): MfaService, Setup, UserBackupCodeRepository, UserEntity

### Community 58 - "java / clearIssue() / MtsAdminController()"
Cohesion: 0.20
Nodes (7): MtsAuthorizedSenderRepository, MtsFromDomainService, MtsInboxRepository, MtsIngestService, MtsSenderListGrantRepository, MtsCatalogReferenceTest, MtsDeleteInboxTest

### Community 61 - "AttentionController / ack() / AttentionController()"
Cohesion: 0.18
Nodes (3): AttentionController, AttentionView, AttentionItemEntity

### Community 62 - "SecretEntity / getCiphertext() / getCreatedAt()"
Cohesion: 0.16
Nodes (3): SecretEntity, SecretRepository, SecretStoreTest

### Community 64 - "put() / clearGlobal() / ParametersProbeController"
Cohesion: 0.24
Nodes (5): ParametersProbeController, ParameterView, ValueBody, Def, ParameterDefinitions

### Community 65 - "ParametersProbeController() / Override / JpaParametersPort"
Cohesion: 0.17
Nodes (3): JpaParametersPort, ParameterEntity, ParameterRepository

### Community 66 - "Override / MailConnectionProbe / rootMessage()"
Cohesion: 0.21
Nodes (4): MailConnectionProbe, Authenticator, Result, MailConnectionProbeTest

### Community 67 - "java / bcryptpasswordencoder / Bean"
Cohesion: 0.22
Nodes (3): KernelStubConfig, SchedulerHost, PasswordConfig

### Community 68 - "jsx / BADGE / formatBytes()"
Cohesion: 0.29
Nodes (11): BADGE, formatBytes(), StatusBadge(), CAMPAIGN_STATUS_LABEL, CAMPAIGN_STATUSES, campaignStatusLabel(), canDeleteCampaign(), canPauseCampaign() (+3 more)

### Community 69 - "invoke() / invokeAt() / register()"
Cohesion: 0.22
Nodes (3): ChainContinuation, PluginFilter, FilterChainRegistryTest

### Community 72 - "java / SendLimits / allowed()"
Cohesion: 0.23
Nodes (3): SendLimits, SendQueueProcessor, SendQueueProcessorTest

### Community 74 - "BootstrapAuthProperties / InstallAdminConfig / installAdmin("
Cohesion: 0.36
Nodes (4): BootstrapAuthProperties, InstallAdminConfig, UserRepository, InstallAdminConfigTest

### Community 77 - "SendingPathsPage() / create() / load()"
Cohesion: 0.27
Nodes (10): SendingPathsPage(), create(), load(), pathBase(), remove(), runSmtpTest(), save(), DeleteBlocked() (+2 more)

### Community 78 - "UnsubscribeController / get() / html()"
Cohesion: 0.27
Nodes (5): UnsubscribeController, Outcome, ALREADY, INVALID, UNSUBSCRIBED

### Community 81 - "authHeaders() / Compose() / bodyPayload()"
Cohesion: 0.29
Nodes (7): authHeaders(), Compose(), bodyPayload(), clearPickedFile(), fileToAttachment(), removeKeptAttachment(), saveDraft()

### Community 83 - "js / formatBooleanString() / isBooleanString()"
Cohesion: 0.39
Nodes (7): formatBooleanString(), isBooleanString(), parseBooleanString(), ParametersPage(), loadSecretsStatus(), rewrapSecrets(), save()

### Community 84 - "MembersPage() / disable() / enable()"
Cohesion: 0.33
Nodes (7): MembersPage(), disable(), enable(), refreshDetail(), requestConsent(), search(), membersSearchPath()

### Community 95 - "host / port / yml"
Cohesion: 0.25
Nodes (7): host, port, mailer, imap, smtp, host, port

### Community 96 - "js / mtsSenderDetailId() / parseMtsDetailId()"
Cohesion: 0.38
Nodes (4): mtsSenderDetailId(), parseMtsDetailId(), parsed, sid

### Community 102 - "java / MtsGateResult / ACCEPT"
Cohesion: 0.29
Nodes (6): MtsGateResult, ACCEPT, DROP_COUNTED, DROP_SILENT, FROM_DOMAIN_ISSUE, NOTIFY_CROSS_INBOX

### Community 103 - "json / plugins / rules"
Cohesion: 0.33
Nodes (5): plugins, rules, react/only-export-components, react/rules-of-hooks, $schema

### Community 107 - "java / ImageFetchMode / BEST_EFFORT"
Cohesion: 0.33
Nodes (4): ImageFetchMode, BEST_EFFORT, MANDATORY, OFF

### Community 108 - "AuditController / AuditController() / index()"
Cohesion: 0.47
Nodes (3): AuditController, AuditView, AuditService

### Community 110 - "ApproveOutcome / ALREADY_CLEAR / CLEARED"
Cohesion: 0.40
Nodes (5): ApproveOutcome, ALREADY_CLEAR, CLEARED, EXPIRED, INVALID

## Knowledge Gaps
- **34 isolated node(s):** `com.listmailer:mailer`, `STARTED`, `ALREADY_RUNNING`, `SKIPPED`, `DRAFT` (+29 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 327 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **45 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ParametersPort` connect `java / any / anyboolean` to `java / deleteByMemberId() / deleteByCampaignId()`, `findByCampaignId() / TransactionCallback / countBySendQueueI`, `deleteByCampaignId() / ResponseStatus / getStatus()`, `java / authentication / bigdecimal`, `BlockView / ConnectionTestRequest / ConnectionTestResponse`, `java / unblock() / dataintegrityviolationexception`, `newGuid() / OpsLogRepository / deleteOlderThan()`, `blockDomain() / java / resolveDrop()`, `java / arraylist / atomicboolean`, `java / delete() / completablefuture`, `java / Modifying / Query`, `java / ApproveController() / ConsentService`, `java / jwsheader / jwtencoderparameters`, `findIdsByStatus() / BounceProcessController() / runNow()`, `from() / AuditLogEntity / AuditLogEntity()`, `java / assertdoesnotthrow / inetaddress`, `MfaController() / MfaService / confirmSetup()`, `Override / java / doFilterInternal()`, `java / clearIssue() / MtsAdminController()`, `put() / clearGlobal() / ParametersProbeController`, `ParametersProbeController() / Override / JpaParametersPort`, `java / SendLimits / allowed()`, `Authentication / CodeRequest / MfaController`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **What connects `com.listmailer:mailer`, `STARTED`, `ALREADY_RUNNING` to the rest of the system?**
  _34 weakly-connected nodes found - possible missing edges._
- **Should `java / any / anyboolean` be split into smaller, more focused modules?**
  _Cohesion score 0.05807232154859524 - nodes in this community are weakly interconnected._
- **Why does `SendQueueEntity` connect `notifyBouncesReceived() / deliver() / injectTracking()` to `java / any / anyboolean`, `java / deleteByMemberId() / deleteByCampaignId()`, `deleteByListId() / delete() / from()`, `findByCampaignId() / TransactionCallback / countBySendQueueI`, `java / unblock() / dataintegrityviolationexception`, `correlatePending() / getCorrelationSignal() / getMemberId()`, `textOf() / BounceCorrelationService / classify()`, `setClassification() / setMemberId() / setSendQueueId()`, `java / arraylist / atomicboolean`, `java / column / enumerated`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Should `java / deleteByMemberId() / deleteByCampaignId()` be split into smaller, more focused modules?**
  _Cohesion score 0.10613810741687979 - nodes in this community are weakly interconnected._
- **Why does `SecretStore` connect `testConnection() / ConnectionTestResponse / BouncePollServic` to `java / any / anyboolean`, `java / deleteByMemberId() / deleteByCampaignId()`, `usedBy() / from() / findByTargetTypeAndTargetId()`, `java / authentication / bigdecimal`, `BlockView / ConnectionTestRequest / ConnectionTestResponse`, `java / unblock() / dataintegrityviolationexception`, `java / arraylist / atomicboolean`, `java / authenticator / bytearrayinputstream`, `java / delete() / completablefuture`, `java / props() / autowired`, `Connection / BaseJavaMigration / Context`, `BouncePollService / breakLock() / finish()`, `MfaController() / MfaService / confirmSetup()`, `DisplayName / getId() / countByCampaignId()`, `java / clearIssue() / MtsAdminController()`, `SecretEntity / getCiphertext() / getCreatedAt()`, `countByKeyVersionLessThan() / status() / migrateSecretsOnSta`, `seedPath() / setUp() / BeforeEach`, `Multipart / UnsubscribeTracerTest / extractHtml()`?**
  _High betweenness centrality (0.022) - this node is a cross-community bridge._
- **Should `deleteByListId() / delete() / from()` be split into smaller, more focused modules?**
  _Cohesion score 0.07211538461538461 - nodes in this community are weakly interconnected._