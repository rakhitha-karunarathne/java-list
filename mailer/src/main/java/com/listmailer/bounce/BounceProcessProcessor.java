package com.listmailer.bounce;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BounceProcessProcessor {

  private final BounceProcessService process;

  public BounceProcessProcessor(BounceProcessService process) {
    this.process = process;
  }

  /** Default 02:00 UTC daily; override via mailer.bounce.process-cron. */
  @Scheduled(cron = "${mailer.bounce.process-cron:0 0 2 * * *}")
  public void tick() {
    process.processDaily();
  }
}
