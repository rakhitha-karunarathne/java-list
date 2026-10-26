package com.listmailer.bounce;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BounceCorrelationProcessor {

  private final BounceCorrelationService correlation;

  public BounceCorrelationProcessor(BounceCorrelationService correlation) {
    this.correlation = correlation;
  }

  @Scheduled(fixedDelayString = "${mailer.bounce.correlate-tick-ms:60000}")
  public void tick() {
    correlation.correlatePending();
  }
}
