- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-multi-list-dedupe-and-content-compose-plan.md`
  summary: Compose does not resume Draft after refresh (no campaign picker / GET hydrate).
  evidence: App.jsx keeps campaignId only in React state; story AC allows save+reload via GET but UI omits load path.

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-multi-list-dedupe-and-content-compose-plan.md`
  summary: No hard caps on attachment count/size or encoded attachment filenames.
  evidence: Review noted memory/DB risk and non-ASCII setFileName; product ceilings belong with payload stories (5.7).

- source_plan: `_bmad-output/initiative-java-list-mailer/tickets/epic-campaigns-send/tickets/story-multi-list-dedupe-and-content-compose-plan.md`
  summary: No dedicated JavaMailSmtpAdapter MIME-structure unit test.
  evidence: Adapter untested beyond CampaignSendService forwarding attachments; GreenMail MIME assert would harden 5.10.
