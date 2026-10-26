package com.listmailer.mts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.noticemail.NoticeMailService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MtsDropServiceTest {

  @Mock MtsDropLogRepository drops;
  @Mock MtsBlockRepository blocks;
  @Mock ParametersPort parameters;
  @Mock NoticeMailService noticeMail;
  @Mock OpsHooksPort ops;
  @InjectMocks MtsDropService service;

  @Test
  void countedDropsAutoBlockAtN() {
    when(parameters.get("mts.auto.block.per.day")).thenReturn(Optional.of("2"));
    when(drops.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(drops.countBySenderEmailAndCountedForAutoBlockTrueAndCreatedAtAfter(
            eq("bad@x.com"), any(Instant.class)))
        .thenReturn(2L);
    when(blocks.existsByKindAndValue("SENDER", "bad@x.com")).thenReturn(false);
    when(blocks.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.record("i1", "bad@x.com", "stranger_unapproved", true);

    verify(blocks).save(any());
    verify(ops).opsLog(eq("mts.autoblock"), org.mockito.ArgumentMatchers.contains("bad@x.com"));
  }
}
