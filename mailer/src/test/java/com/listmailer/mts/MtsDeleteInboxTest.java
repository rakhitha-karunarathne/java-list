package com.listmailer.mts;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.catalogs.SendingPathRepository;
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
class MtsDeleteInboxTest {

  @Mock MtsInboxRepository inboxes;
  @Mock MtsAuthorizedSenderRepository senders;
  @Mock MtsSenderListGrantRepository grants;
  @Mock ListRepository lists;
  @Mock SendingPathRepository sendingPaths;
  @Mock MtsFromDomainService fromDomain;
  @Mock MtsDropService drops;
  @Mock CatalogReferenceService references;
  @Mock MailConnectionProbe probe;
  @InjectMocks MtsAdminController controller;

  @Test
  void deleteInboxClearsGrantRefsAndDropsThenDeletesInbox() {
    MtsInboxEntity box =
        new MtsInboxEntity("i1", "Ops", "h", 143, "u", "p", false, 5, Instant.now());
    when(inboxes.findById("i1")).thenReturn(Optional.of(box));
    when(senders.findByInboxId("i1"))
        .thenReturn(List.of(new MtsAuthorizedSenderEntity("i1", "a@x.com")));
    when(grants.findByInboxIdAndSenderEmailOrderByPositionAsc("i1", "a@x.com"))
        .thenReturn(
            List.of(
                new MtsSenderListGrantEntity(
                    "g1", "i1", "a@x.com", "L1", "footer-1", 0)));
    when(senders.findByInboxIdAndEmail("i1", "a@x.com"))
        .thenReturn(Optional.of(new MtsAuthorizedSenderEntity("i1", "a@x.com")));
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "A", "path-1", null, null, null, null, Instant.now())));

    controller.deleteInbox("i1");

    verify(references)
        .remove(eq(CatalogTargetType.SENDING_PATH), eq("path-1"), eq("MTS_SENDER_GRANT"), eq("g1"));
    verify(references)
        .remove(eq(CatalogTargetType.FOOTER), eq("footer-1"), eq("MTS_SENDER_GRANT"), eq("g1"));
    verify(drops).deleteDropsForInbox("i1");
    verify(inboxes).delete(box);
    verify(grants, never()).deleteByInboxIdAndSenderEmail(eq("i1"), eq("a@x.com"));
  }
}
