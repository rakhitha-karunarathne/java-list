package com.listmailer.lists;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignOpenRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberRepository;
import com.listmailer.mts.MtsSenderListGrantRepository;
import com.listmailer.users.UserListGrantRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListDeleteService {

  private static final Set<CampaignStatus> ONGOING =
      EnumSet.of(CampaignStatus.QUEUED, CampaignStatus.SENDING, CampaignStatus.PAUSED);

  private final ListRepository lists;
  private final ListMemberRepository listMembers;
  private final MemberRepository members;
  private final CampaignListRepository campaignLists;
  private final CampaignRepository campaigns;
  private final SendQueueRepository queue;
  private final CampaignOpenRepository opens;
  private final BounceEventRepository bounces;
  private final UserListGrantRepository grants;
  private final MtsSenderListGrantRepository mtsGrants;
  private final CatalogReferenceService references;

  public ListDeleteService(
      ListRepository lists,
      ListMemberRepository listMembers,
      MemberRepository members,
      CampaignListRepository campaignLists,
      CampaignRepository campaigns,
      SendQueueRepository queue,
      CampaignOpenRepository opens,
      BounceEventRepository bounces,
      UserListGrantRepository grants,
      MtsSenderListGrantRepository mtsGrants,
      CatalogReferenceService references) {
    this.lists = lists;
    this.listMembers = listMembers;
    this.members = members;
    this.campaignLists = campaignLists;
    this.campaigns = campaigns;
    this.queue = queue;
    this.opens = opens;
    this.bounces = bounces;
    this.grants = grants;
    this.mtsGrants = mtsGrants;
    this.references = references;
  }

  @Transactional
  public void delete(String listId, boolean deleteOrphanMembers) {
    ListEntity list =
        lists
            .findById(listId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "list not found"));

    List<CampaignListEntity> linked = campaignLists.findByListId(listId);
    for (CampaignListEntity cl : linked) {
      CampaignEntity campaign =
          campaigns
              .findById(cl.getCampaignId())
              .orElse(null);
      if (campaign != null && ONGOING.contains(campaign.getStatus())) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "cannot delete list while a Queued, Sending, or Paused campaign uses it");
      }
    }

    List<ListMemberEntity> memberships = listMembers.findByListId(listId);
    List<String> memberIds = memberships.stream().map(ListMemberEntity::getMemberId).toList();

    campaignLists.deleteByListId(listId);
    mtsGrants.deleteByListId(listId);
    grants.deleteByListId(listId);
    listMembers.deleteByListId(listId);

    if (deleteOrphanMembers) {
      for (String memberId : memberIds) {
        if (listMembers.countByMemberId(memberId) == 0) {
          deleteMemberData(memberId);
        }
      }
    }

    references.remove(CatalogTargetType.SENDING_PATH, list.getSendingPathId(), "LIST", listId);
    if (list.getFooterId() != null) {
      references.remove(CatalogTargetType.FOOTER, list.getFooterId(), "LIST", listId);
    }
    lists.delete(list);
  }

  private void deleteMemberData(String memberId) {
    List<SendQueueEntity> rows = queue.findByMemberId(memberId);
    List<String> queueIds = rows.stream().map(SendQueueEntity::getId).toList();
    if (!queueIds.isEmpty()) {
      bounces.deleteBySendQueueIdIn(queueIds);
    }
    bounces.deleteByMemberId(memberId);
    opens.deleteByMemberId(memberId);
    queue.deleteByMemberId(memberId);
    members.deleteById(memberId);
  }
}
