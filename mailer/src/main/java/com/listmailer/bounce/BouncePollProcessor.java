package com.listmailer.bounce;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BouncePollProcessor {

  private final BouncePollService poll;

  public BouncePollProcessor(BouncePollService poll) {
    this.poll = poll;
  }

  @Scheduled(fixedDelayString = "${mailer.bounce.poll-tick-ms:60000}")
  public void tick() {
    poll.pollDue();
  }
}
