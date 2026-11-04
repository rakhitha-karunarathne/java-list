package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.lists.ListEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RemoteImageFetchServiceTest {

  @Mock CampaignAttachmentRepository attachments;
  @Mock SsrfGuard ssrf;
  @Mock ParametersPort parameters;
  @InjectMocks RemoteImageFetchService service;

  @Test
  void payloadCeilingUsesKbParameter() {
    when(parameters.get(ParameterKeys.CAMPAIGN_PAYLOAD_MAX_KB)).thenReturn(Optional.of("1"));
    when(parameters.get(ParameterKeys.CAMPAIGN_IMAGE_MAX_KB)).thenReturn(Optional.of("500"));
    String html = "x".repeat(1025);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.ingest("c1", html, ImageFetchMode.OFF, List.of()));

    assertEquals(true, ex.getReason().contains("1 KB"));
  }

  @Test
  void listPayloadOverrideIsKb() {
    when(parameters.get(ParameterKeys.CAMPAIGN_PAYLOAD_MAX_KB)).thenReturn(Optional.of("2000"));
    when(parameters.get(ParameterKeys.CAMPAIGN_IMAGE_MAX_KB)).thenReturn(Optional.of("500"));
    ListEntity list =
        new ListEntity("L1", "News", "sp", null, null, "1", null, Instant.now());
    String html = "x".repeat(1025);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> service.ingest("c1", html, ImageFetchMode.OFF, List.of(list)));

    assertEquals(true, ex.getReason().contains("1 KB"));
  }
}
