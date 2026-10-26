package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SendLimitsTest {

  @Test
  void capsBatchByHourlyRoom() {
    assertEquals(600, SendLimits.allowed(600, 3600, 0));
    assertEquals(50, SendLimits.allowed(600, 3600, 3550));
    assertEquals(0, SendLimits.allowed(600, 3600, 3600));
    assertEquals(0, SendLimits.allowed(600, 3600, 4000));
    assertEquals(0, SendLimits.allowed(0, 3600, 0));
  }
}
