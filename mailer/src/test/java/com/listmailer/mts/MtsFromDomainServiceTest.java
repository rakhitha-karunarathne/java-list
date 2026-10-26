package com.listmailer.mts;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.catalogs.SendingPathEntity;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.noticemail.NoticeMailService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MtsFromDomainServiceTest {

  @Mock MtsFromDomainIssueRepository issues;
  @Mock ListRepository lists;
  @Mock SendingPathRepository paths;
  @Mock NoticeMailService noticeMail;
  @Mock OpsHooksPort ops;
  @InjectMocks MtsFromDomainService service;

  @Test
  void emptySubstituteOk() {
    var grant = new MtsSenderListGrantEntity("g", "i", "a@x.com", "L1", null, null, 0);
    org.junit.jupiter.api.Assertions.assertNull(
        service.validateOrOpenIssue(
            new MtsInboxEntity("i", "n", "h", 1, "u", "p", false, 1, Instant.now()),
            List.of(grant),
            "a@x.com",
            "s"));
    verify(issues, never()).save(any());
  }

  @Test
  void domainMismatchOpensIssueNoCampaignPath() {
    var grant = new MtsSenderListGrantEntity("g", "i", "a@x.com", "L1", "bad@evil.com", null, 0);
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "A", "p1", null, null, null, null, Instant.now())));
    when(paths.findById("p1"))
        .thenReturn(
            Optional.of(
                new SendingPathEntity(
                    "p1", "P", "h", 25, "u", "pw", "ok@good.com", "N", false, Instant.now())));
    when(issues.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var issue =
        service.validateOrOpenIssue(
            new MtsInboxEntity("i", "n", "h", 1, "u", "p", false, 1, Instant.now()),
            List.of(grant),
            "a@x.com",
            "s");
    org.junit.jupiter.api.Assertions.assertNotNull(issue);
    org.junit.jupiter.api.Assertions.assertEquals("DELETED_NOTIFY", issue.getResolution());
    org.junit.jupiter.api.Assertions.assertEquals("OPEN", issue.getStatus());
    verify(noticeMail).send(anyString(), anyString(), anyString(), anyString());
  }
}
