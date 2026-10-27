package com.listmailer.campaigns;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Cached pause flags for the send loop. Cleared in this process when a value is set to paused.
 * Multi-instance clear is V2+.
 */
@Component
public class SendPauseCache {

  private final ParametersPort parameters;
  private final CampaignRepository campaigns;
  private final ListRepository lists;

  private volatile Boolean globalPaused;
  private final Map<String, Boolean> campaignPaused = new ConcurrentHashMap<>();
  private final Map<String, Boolean> listPaused = new ConcurrentHashMap<>();

  public SendPauseCache(
      ParametersPort parameters, CampaignRepository campaigns, ListRepository lists) {
    this.parameters = parameters;
    this.campaigns = campaigns;
    this.lists = lists;
  }

  public void clear() {
    globalPaused = null;
    campaignPaused.clear();
    listPaused.clear();
  }

  public void clearGlobal() {
    globalPaused = null;
  }

  public void clearCampaign(String id) {
    campaignPaused.remove(id);
  }

  public void clearList(String id) {
    listPaused.remove(id);
  }

  public boolean globalPaused() {
    Boolean cached = globalPaused;
    if (cached != null) {
      return cached;
    }
    boolean v =
        parameters
            .get(ParameterKeys.SEND_PAUSED)
            .map(s -> "true".equalsIgnoreCase(s.trim()))
            .orElse(false);
    globalPaused = v;
    return v;
  }

  public boolean campaignPaused(String campaignId) {
    return campaignPaused.computeIfAbsent(
        campaignId,
        id ->
            campaigns
                .findById(id)
                .map(c -> c.getStatus() == CampaignStatus.PAUSED)
                .orElse(false));
  }

  public boolean listPaused(String listId) {
    if (listId == null || listId.isBlank()) {
      return false;
    }
    return listPaused.computeIfAbsent(
        listId, id -> lists.findById(id).map(ListEntity::isPaused).orElse(false));
  }
}
