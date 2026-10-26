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
  @Mock MtsSenderListGrantRepository grants;
  @Mock ListRepository lists;
  @Mock MtsFromDomainService fromDomain;
  @Mock MtsDropService drops;
  @Mock CatalogReferenceService references;
  @Mock MailConnectionProbe probe;
  @InjectMocks MtsAdminController controller;

  @Test
  void putSenderGrantsRegistersSendingPathAndFooterUsedBy() {
    when(inboxes.findById("i1"))
        .thenReturn(
            Optional.of(new MtsInboxEntity("i1", "n", "h", 1, "u", "p", false, 1, Instant.now())));
    when(senders.existsByInboxIdAndEmail("i1", "a@x.com")).thenReturn(true);
    when(lists.existsById("L1")).thenReturn(true);
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "A", "path-1", null, null, null, null, Instant.now())));
    when(lists.findAll())
        .thenReturn(List.of(new ListEntity("L1", "A", "path-1", null, null, null, null, Instant.now())));
    when(grants.findByInboxIdAndSenderEmailOrderByPositionAsc("i1", "a@x.com"))
        .thenReturn(List.of())
        .thenReturn(
            List.of(
                new MtsSenderListGrantEntity(
                    "g1", "i1", "a@x.com", "L1", null, "footer-1", 0)));
    when(grants.save(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(
            inv -> {
              MtsSenderListGrantEntity g = inv.getArgument(0);
              return g;
            });

    controller.putSenderGrants(
        "i1",
        "a@x.com",
        new MtsAdminController.GrantsBody(
            List.of(new MtsAdminController.GrantRow("L1", true, null, "footer-1"))));

    verify(references)
        .add(
            eq(CatalogTargetType.SENDING_PATH),
            eq("path-1"),
            eq("MTS_SENDER_GRANT"),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.contains("MTS sender"));
    verify(references)
        .add(
            eq(CatalogTargetType.FOOTER),
            eq("footer-1"),
            eq("MTS_SENDER_GRANT"),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.contains("MTS sender"));
  }
}
