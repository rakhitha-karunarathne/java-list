package com.listmailer.catalogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.mailbox.MailConnectionProbe;
import com.listmailer.mts.MtsFromDomainService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SendingPathReturnPathTest {

  @Mock SendingPathRepository paths;
  @Mock CatalogReferenceService references;
  @Mock MtsFromDomainService fromDomain;
  @Mock MailConnectionProbe probe;
  @InjectMocks SendingPathsController controller;

  @Test
  void createRejectsMismatchedReturnPathDomain() {
    SendingPathsController.PathRequest body =
        new SendingPathsController.PathRequest(
            "Main",
            "smtp.example",
            587,
            "u",
            "p",
            "news@list.example",
            "News",
            "bounce@other.example",
            true,
            false);

    assertThrows(ResponseStatusException.class, () -> controller.create(body));
  }

  @Test
  void createStoresMatchingReturnPath() {
    when(paths.save(any())).thenAnswer(inv -> inv.getArgument(0));

    SendingPathsController.PathRequest body =
        new SendingPathsController.PathRequest(
            "Main",
            "smtp.example",
            587,
            "u",
            "p",
            "news@list.example",
            "News",
            "bounce@list.example",
            true,
            false);

    SendingPathsController.PathDetail d = controller.create(body);

    assertEquals("bounce@list.example", d.returnPathEmail());
    ArgumentCaptor<SendingPathEntity> cap = ArgumentCaptor.forClass(SendingPathEntity.class);
    verify(paths).save(cap.capture());
    assertEquals("bounce@list.example", cap.getValue().getReturnPathEmail());
  }

  @Test
  void updateClearsReturnPathWhenBlank() {
    SendingPathEntity existing =
        new SendingPathEntity(
            "p1", "Main", "smtp.example", 587, "u", "p", "news@list.example", "News", true, java.time.Instant.now());
    existing.setReturnPathEmail("bounce@list.example");
    when(paths.findById("p1")).thenReturn(Optional.of(existing));
    when(paths.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(references.usedBy(CatalogTargetType.SENDING_PATH, "p1")).thenReturn(List.of());

    SendingPathsController.PathRequest body =
        new SendingPathsController.PathRequest(
            "Main",
            "smtp.example",
            587,
            "u",
            null,
            "news@list.example",
            "News",
            "  ",
            true,
            false);

    SendingPathsController.PathDetail d = controller.update("p1", body);

    assertNull(d.returnPathEmail());
  }
}
