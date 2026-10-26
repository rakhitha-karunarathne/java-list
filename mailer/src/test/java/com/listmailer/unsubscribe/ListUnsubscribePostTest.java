package com.listmailer.unsubscribe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ListUnsubscribePostTest {

  @Mock UnsubscribeService unsub;
  @InjectMocks UnsubscribeController controller;

  @Test
  void oneClickPostAppliesSameAsGet() {
    when(unsub.apply("tok")).thenReturn(UnsubscribeService.Outcome.UNSUBSCRIBED);
    ResponseEntity<String> res = controller.post("tok", "List-Unsubscribe=One-Click");
    assertEquals(200, res.getStatusCode().value());
    assertTrue(res.getBody().contains("Unsubscribed"));
    verify(unsub).apply("tok");
  }

  @Test
  void nonCompliantBodyDoesNotApply() {
    ResponseEntity<String> res = controller.post("tok", "something-else");
    assertTrue(res.getBody().contains("Unable to unsubscribe"));
    verify(unsub, never()).apply(anyString());
  }

  @Test
  void isOneClickBodyStrict() {
    assertTrue(UnsubscribeController.isOneClickBody("List-Unsubscribe=One-Click"));
    assertTrue(UnsubscribeController.isOneClickBody("list-unsubscribe=one-click\n"));
    assertFalse(UnsubscribeController.isOneClickBody(null));
    assertFalse(UnsubscribeController.isOneClickBody(""));
    assertFalse(UnsubscribeController.isOneClickBody("List-Unsubscribe=One-Click&extra=1"));
  }
}
