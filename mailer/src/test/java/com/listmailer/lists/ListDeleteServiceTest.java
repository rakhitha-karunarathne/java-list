package com.listmailer.lists;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignOpenRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberRepository;
import com.listmailer.mts.MtsSenderListGrantRepository;
import com.listmailer.users.UserListGrantRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ListDeleteServiceTest {

  @Mock ListRepository lists;
  @Mock ListMemberRepository listMembers;
  @Mock MemberRepository members;
  @Mock CampaignListRepository campaignLists;
  @Mock CampaignRepository campaigns;
  @Mock SendQueueRepository queue;
  @Mock CampaignOpenRepository opens;
  @Mock BounceEventRepository bounces;
  @Mock UserListGrantRepository grants;
  @Mock MtsSenderListGrantRepository mtsGrants;
  @Mock CatalogReferenceService references;

  ListDeleteService service;
  ListEntity list;

  @BeforeEach
  void setUp() {
    service =
        new ListDeleteService(
            lists,
            listMembers,
            members,
            campaignLists,
            campaigns,
            queue,
            opens,
            bounces,
            grants,
            mtsGrants,
            references);
    list =
        new ListEntity(
            "L1", "News", "path1", null, null, null, null, Instant.now());
  }

  @Test
  void rejectsWhenOngoingCampaignUsesList() {
    when(lists.findById("L1")).thenReturn(Optional.of(list));
    when(campaignLists.findByListId("L1"))
        .thenReturn(List.of(new CampaignListEntity("C1", "L1", 0)));
    CampaignEntity c =
        new CampaignEntity("C1", "sub", null, null, CampaignStatus.SENDING, Instant.now());
    when(campaigns.findById("C1")).thenReturn(Optional.of(c));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.delete("L1", false));
    assertEquals(409, ex.getStatusCode().value());
    verify(lists, never()).delete(any());
  }

  @Test
  void deletesListWithoutRemovingSharedMembers() {
    when(lists.findById("L1")).thenReturn(Optional.of(list));
    when(campaignLists.findByListId("L1")).thenReturn(List.of());
    when(listMembers.findByListId("L1"))
        .thenReturn(
            List.of(
                new ListMemberEntity("L1", "M1", false, Instant.now()),
                new ListMemberEntity("L1", "M2", false, Instant.now())));

    service.delete("L1", false);

    verify(listMembers).deleteByListId("L1");
    verify(campaignLists).deleteByListId("L1");
    verify(mtsGrants).deleteByListId("L1");
    verify(grants).deleteByListId("L1");
    verify(members, never()).deleteById(anyString());
    verify(references)
        .remove(eq(CatalogTargetType.SENDING_PATH), eq("path1"), eq("LIST"), eq("L1"));
    verify(lists).delete(list);
  }

  @Test
  void deleteOrphansRemovesOnlyMembersWithNoOtherLists() {
    when(lists.findById("L1")).thenReturn(Optional.of(list));
    when(campaignLists.findByListId("L1")).thenReturn(List.of());
    when(listMembers.findByListId("L1"))
        .thenReturn(
            List.of(
                new ListMemberEntity("L1", "orphan", false, Instant.now()),
                new ListMemberEntity("L1", "shared", false, Instant.now())));
    when(listMembers.countByMemberId("orphan")).thenReturn(0L);
    when(listMembers.countByMemberId("shared")).thenReturn(1L);
    when(queue.findByMemberId("orphan")).thenReturn(List.of());

    service.delete("L1", true);

    verify(members).deleteById("orphan");
    verify(members, never()).deleteById("shared");
    verify(opens).deleteByMemberId("orphan");
    verify(bounces).deleteByMemberId("orphan");
    verify(queue).deleteByMemberId("orphan");
  }
}
