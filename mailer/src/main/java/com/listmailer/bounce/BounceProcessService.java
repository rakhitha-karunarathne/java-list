package com.listmailer.bounce;

import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;

@Service
public class BounceProcessService {

  private final BounceEventRepository events;
  private final SendQueueRepository queue;
  private final MemberRepository members;
  private final ParametersPort parameters;
  private final OpsHooksPort ops;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public BounceProcessService(
      BounceEventRepository events,
      SendQueueRepository queue,
      MemberRepository members,
      ParametersPort parameters,
      OpsHooksPort ops) {
    this.events = events;
    this.queue = queue;
    this.members = members;
    this.parameters = parameters;
    this.ops = ops;
  }

  public int processDaily() {
    if (!running.compareAndSet(false, true)) {
      return 0;
    }
    try {
      Set<String> memberIds = new HashSet<>();
      for (BounceEventEntity ev : events.findByStatus("CORRELATED")) {
        if (ev.getMemberId() != null) {
          memberIds.add(ev.getMemberId());
        }
      }
      int disabled = 0;
      for (String memberId : memberIds) {
        if (processMember(memberId)) {
          disabled++;
        }
      }
      return disabled;
    } finally {
      running.set(false);
    }
  }

  /** @return true if member was disabled */
  boolean processMember(String memberId) {
    int hardLimit = intParam(ParameterKeys.BOUNCE_HARD_LIMIT, 3);
    int softLimit = intParam(ParameterKeys.BOUNCE_SOFT_LIMIT, 6);
    int holdbackMult = intParam(ParameterKeys.BOUNCE_HOLDBACK_MULTIPLIER, 2);

    List<SendQueueEntity> sends =
        new ArrayList<>(queue.findByMemberIdAndStatus(memberId, "SENT"));
    sends.sort(
        Comparator.comparing(
            SendQueueEntity::getSentAt, Comparator.nullsLast(Comparator.naturalOrder())));

    Map<String, BounceEventEntity> bySend = new HashMap<>();
    List<BounceEventEntity> memberBounces =
        events.findByStatusAndMemberId("CORRELATED", memberId);
    // AD-25: repository filters CORRELATED only — never UNCORRELATED
    for (BounceEventEntity ev : memberBounces) {
      if (ev.getSendQueueId() != null) {
        bySend.putIfAbsent(ev.getSendQueueId(), ev);
      }
    }

    int hard = 0;
    int soft = 0;
    String lastErr = "";
    for (SendQueueEntity send : sends) {
      BounceEventEntity b = bySend.get(send.getId());
      if (b == null) {
        hard = 0;
        soft = 0;
        continue;
      }
      if ("HARD".equals(b.getClassification())) {
        hard++;
        soft = 0;
        lastErr = "hard bounce";
      } else {
        soft++;
        hard = 0;
        lastErr = "soft bounce";
      }
      if (hard >= hardLimit || soft >= softLimit) {
        disable(memberId, hard >= hardLimit ? "HARD" : "SOFT", lastErr, hard, soft);
        for (BounceEventEntity ev : memberBounces) {
          ev.setStatus("PROCESSED");
          events.save(ev);
        }
        return true;
      }
    }

    // below limit: mark processable respecting holdback per class
    markWithHoldback(sends, bySend, hardLimit, holdbackMult, "HARD");
    markWithHoldback(sends, bySend, softLimit, holdbackMult, "SOFT");
    return false;
  }

  private void markWithHoldback(
      List<SendQueueEntity> sends,
      Map<String, BounceEventEntity> bySend,
      int limit,
      int holdbackMult,
      String clazz) {
    List<BounceEventEntity> classHits = new ArrayList<>();
    for (SendQueueEntity send : sends) {
      BounceEventEntity b = bySend.get(send.getId());
      if (b != null && clazz.equals(b.getClassification()) && "CORRELATED".equals(b.getStatus())) {
        classHits.add(b);
      }
    }
    int holdback = Math.max(0, holdbackMult * limit);
    int markable = Math.max(0, classHits.size() - holdback);
    for (int i = 0; i < markable; i++) {
      BounceEventEntity b = classHits.get(i);
      b.setStatus("PROCESSED");
      events.save(b);
    }
  }

  private void disable(String memberId, String clazz, String lastErr, int hard, int soft) {
    MemberEntity m = members.findById(memberId).orElse(null);
    if (m == null) {
      return;
    }
    // C3: do not touch list unsubscribe flags
    m.setEnabled(false);
    m.setDisableReason(
        "bounce " + clazz.toLowerCase() + " limit (hard=" + hard + " soft=" + soft + "): " + lastErr);
    members.save(m);
    ops.opsLog("bounce.disable", "member=" + memberId + " class=" + clazz);
    ops.audit("bounce.disable", "member=" + memberId + " class=" + clazz);
  }

  private int intParam(String key, int def) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                int n = Integer.parseInt(v.trim());
                return n > 0 ? n : def;
              } catch (NumberFormatException e) {
                return def;
              }
            })
        .orElse(def);
  }
}
