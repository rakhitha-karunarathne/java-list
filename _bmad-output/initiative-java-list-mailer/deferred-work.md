- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-multi-list-dedupe-and-content-compose-plan.md`
  summary: Compose does not resume Draft after refresh (no campaign picker / GET hydrate).
  evidence: App.jsx keeps campaignId only in React state; story AC allows save+reload via GET but UI omits load path.

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-multi-list-dedupe-and-content-compose-plan.md`
  summary: No hard caps on attachment count/size or encoded attachment filenames.
  evidence: Review noted memory/DB risk and non-ASCII setFileName; product ceilings belong with payload stories (5.7).

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-multi-list-dedupe-and-content-compose-plan.md`
  summary: No dedicated JavaMailSmtpAdapter MIME-structure unit test.
  evidence: Adapter untested beyond CampaignSendService forwarding attachments; GreenMail MIME assert would harden 5.10.

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-test-send-from-compose-plan.md`
  summary: Test send can partially deliver then return 502 on later recipient failure.
  evidence: Sequential loop; no transactional all-or-nothing SMTP.

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-test-send-from-compose-plan.md`
  summary: Queue-path SMTP failures do not yet emit OpsHooksPort (only test-send does).
  evidence: Intent caveat; epic-ops will complete persistence; consider wiring queue failures when Ops Logs land.

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-blob-storage-and-public-dl-plan.md`
  summary: HTML rewriter only covers quoted img src forms, not href/srcset/CSS urls.
  evidence: Review of CampaignHtmlRewriter; expand when compose image UX matures.

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-blob-storage-and-public-dl-plan.md`
  summary: Campaign DELETE is not re-checked against list send grants.
  evidence: Same pattern as queue; tighten when campaign ACL story exists.

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-image-fetch-modes-ssrf-and-payload-ceilings-plan.md`
  summary: Blob CDN TTL restart on Finished Requeue not tracked yet.
  evidence: R5 TTL clause; needs Finished timestamp + TTL field on blobs.

