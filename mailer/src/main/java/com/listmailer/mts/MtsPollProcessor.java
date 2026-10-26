package com.listmailer.mts;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MtsPollProcessor {

  private final MtsIngestService ingest;

  public MtsPollProcessor(MtsIngestService ingest) {
    this.ingest = ingest;
  }

  @Scheduled(fixedDelayString = "${mailer.mts.poll-tick-ms:60000}")
  public void tick() {
    ingest.pollDue();
  }
}
