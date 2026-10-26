package com.listmailer.mts;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.mailbox.MailConnectionProbe;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MtsCatalogReferenceTest {

  @Mock MtsInboxRepository inboxes;
  @Mock MtsAuthorizedSenderRepository senders;
  @Mock MtsMappingRepository mappings;
  @Mock MtsMappingListRepository mappingLists;
  @Mock ListRepository lists;
  @Mock MtsFromDomainService fromDomain;
  @Mock MtsDropService drops;
  @Mock CatalogReferenceService references;
  @Mock MailConnectionProbe probe;
  @InjectMocks MtsAdminController controller;

  @Test
  void createMappingRegistersSendingPathAndFooterUsedBy() {
    when(inboxes.findById("i1"))
        .thenReturn(
            Optional.of(new MtsInboxEntity("i1", "n", "h", 1, "u", "p", false, 1, Instant.now())));
    when(lists.existsById("L1")).thenReturn(true);
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "A", "path-1", null, null, null, null, Instant.now())));
    when(mappings.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(
            inv -> {
              MtsMappingEntity m = inv.getArgument(0);
              return m;
            });
    when(mappingLists.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(inv -> inv.getArgument(0));

    controller.createMapping(
        "i1",
        new MtsAdminController.MappingBody("map", List.of("L1"), null, "footer-1"));

    verify(references)
        .add(
            eq(CatalogTargetType.SENDING_PATH),
            eq("path-1"),
            eq("MTS_MAPPING"),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.contains("MTS mapping"));
    verify(references)
        .add(
            eq(CatalogTargetType.FOOTER),
            eq("footer-1"),
            eq("MTS_MAPPING"),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.contains("MTS mapping"));
  }
}
