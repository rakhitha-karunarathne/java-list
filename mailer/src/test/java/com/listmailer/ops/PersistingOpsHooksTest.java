package com.listmailer.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersistingOpsHooksTest {

  @Mock OpsLogRepository opsLogs;
  @Mock AttentionItemRepository attention;
  @InjectMocks PersistingOpsHooks hooks;

  @Test
  void sendFailureWritesOpsAndAttention() {
    when(opsLogs.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(attention.save(any())).thenAnswer(inv -> inv.getArgument(0));

    hooks.opsLog("send.test", "test send failed to x@y.com: refused");
    hooks.attentionAdmin("Test send failed", "Campaign c1 → x@y.com");

    ArgumentCaptor<OpsLogEntity> opsCap = ArgumentCaptor.forClass(OpsLogEntity.class);
    verify(opsLogs).save(opsCap.capture());
    assertEquals("send.test", opsCap.getValue().getCategory());
    assertEquals("ERROR", opsCap.getValue().getSeverity());

    ArgumentCaptor<AttentionItemEntity> attCap = ArgumentCaptor.forClass(AttentionItemEntity.class);
    verify(attention).save(attCap.capture());
    assertEquals("OPS_ERROR", attCap.getValue().getKind());
  }

  @Test
  void redactsPasswordFragments() {
    assertTrue(PersistingOpsHooks.safe("password=secret123 host=x").contains("password=***"));
  }
}
