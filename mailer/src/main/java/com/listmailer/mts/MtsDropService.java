package com.listmailer.mts;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.noticemail.NoticeMailService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MtsDropService {

  private final MtsDropLogRepository drops;
  private final MtsBlockRepository blocks;
  private final ParametersPort parameters;
  private final NoticeMailService noticeMail;
  private final OpsHooksPort ops;

  public MtsDropService(
      MtsDropLogRepository drops,
      MtsBlockRepository blocks,
      ParametersPort parameters,
      NoticeMailService noticeMail,
      OpsHooksPort ops) {
    this.drops = drops;
    this.blocks = blocks;
    this.parameters = parameters;
    this.noticeMail = noticeMail;
    this.ops = ops;
  }

  @Transactional
  public void record(
      String inboxId, String senderEmail, String reason, boolean countForAutoBlock) {
    drops.save(
        new MtsDropLogEntity(
            Guids.newGuid(), inboxId, senderEmail, reason, countForAutoBlock, Instant.now()));
    if (countForAutoBlock && senderEmail != null) {
      maybeAutoBlock(senderEmail.toLowerCase(Locale.ROOT));
    }
  }

  @Transactional
  public void notifyCrossInbox(String senderEmail, String inboxName) {
    try {
      noticeMail.send(
          senderEmail,
          "Mail-to-send: wrong inbox",
          "Your message arrived on inbox " + inboxName + " where you are not authorized.",
          "<p>Your message arrived on inbox <strong>"
              + inboxName
              + "</strong> where you are not authorized.</p>");
    } catch (RuntimeException ex) {
      ops.opsLog("mts.drop", "cross-inbox notify failed: " + ex.getMessage());
    }
  }

  public boolean isBlocked(String senderEmail) {
    if (senderEmail == null) {
      return false;
    }
    String email = senderEmail.toLowerCase(Locale.ROOT);
    if (blocks.existsByKindAndValue("SENDER", email)) {
      return true;
    }
    String domain = MtsEmails.domain(email);
    return blocks.existsByKindAndValue("DOMAIN", domain);
  }

  @Transactional
  public void blockSender(String email, String source) {
    String v = email.trim().toLowerCase(Locale.ROOT);
    if (!blocks.existsByKindAndValue("SENDER", v)) {
      blocks.save(new MtsBlockEntity(Guids.newGuid(), "SENDER", v, source, Instant.now()));
    }
  }

  @Transactional
  public void blockDomain(String domain, String source) {
    String v = domain.trim().toLowerCase(Locale.ROOT);
    if (!blocks.existsByKindAndValue("DOMAIN", v)) {
      blocks.save(new MtsBlockEntity(Guids.newGuid(), "DOMAIN", v, source, Instant.now()));
    }
  }

  @Transactional
  public void unblock(String id) {
    blocks.deleteById(id);
  }

  public List<MtsDropLogEntity> list(String inboxId) {
    if (inboxId == null || inboxId.isBlank()) {
      return drops.findAllByOrderByCreatedAtDesc();
    }
    return drops.findByInboxIdOrderByCreatedAtDesc(inboxId);
  }

  public List<MtsBlockEntity> allBlocks() {
    return blocks.findAll();
  }

  @Transactional
  public void resolve(String dropId) {
    MtsDropLogEntity d =
        drops.findById(dropId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    d.setResolved(true);
    drops.save(d);
  }

  @Transactional
  public void deleteDropsForInbox(String inboxId) {
    drops.deleteByInboxId(inboxId);
  }

  private void maybeAutoBlock(String senderEmail) {
    int n = intParam("mts.auto.block.per.day", 10);
    Instant since = Instant.now().minus(1, ChronoUnit.DAYS);
    long count =
        drops.countBySenderEmailAndCountedForAutoBlockTrueAndCreatedAtAfter(senderEmail, since);
    if (count >= n) {
      blockSender(senderEmail, "AUTO");
      ops.opsLog("mts.autoblock", "sender=" + senderEmail + " count=" + count);
    }
  }

  private int intParam(String key, int def) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                int x = Integer.parseInt(v.trim());
                return x > 0 ? x : def;
              } catch (NumberFormatException e) {
                return def;
              }
            })
        .orElse(def);
  }
}
