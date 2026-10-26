package com.listmailer.consent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApproveEdgeControllerTest {

  @Mock ConsentService consent;
  @InjectMocks ApproveController controller;

  @Test
  void expiredPageExplainsExpiry() {
    when(consent.approve("t")).thenReturn(ConsentService.ApproveOutcome.EXPIRED);
    String html = controller.get("t").getBody();
    assertTrue(html.contains("expired"));
  }

  @Test
  void invalidPageIsSafe() {
    when(consent.approve("bad")).thenReturn(ConsentService.ApproveOutcome.INVALID);
    String html = controller.get("bad").getBody();
    assertTrue(html.contains("Unable to confirm"));
  }
}
