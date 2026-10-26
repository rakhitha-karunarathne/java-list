package com.listmailer.bounce;

import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BounceCorrelationService {

  private static final Pattern TOKEN =
      Pattern.compile("(?i)(?:X-JavaList-Token|bounce\\+)[:\\s]*([0-9a-f-]{36})");
  private static final Pattern MSG_ID = Pattern.compile("<[^>]+@[^>]+>");
  private static final Pattern STATUS = Pattern.compile("(?i)status:\\s*([245])\\.\\d+\\.\\d+");

  private final BounceEventRepository events;
  private final SendQueueRepository queue;
  private final ParametersPort parameters;
  private final OpsHooksPort ops;

  public BounceCorrelationService(
      BounceEventRepository events,
      SendQueueRepository queue,
      ParametersPort parameters,
      OpsHooksPort ops) {
    this.events = events;
    this.queue = queue;
    this.parameters = parameters;
    this.ops = ops;
  }

  /** Correlate all INGESTED events. Never disables Members (story 4). */
  public int correlatePending() {
    int n = 0;
    for (BounceEventEntity ev : events.findByStatus("INGESTED")) {
      correlateOne(ev);
      n++;
    }
    return n;
  }

  void correlateOne(BounceEventEntity ev) {
    String blob = textOf(ev);
    Optional<Match> match = ladder(ev, blob);
    String clazz = classify(blob);
    if (match.isPresent()) {
      Match m = match.get();
      ev.setStatus("CORRELATED");
      ev.setMemberId(m.row().getMemberId());
      ev.setSendQueueId(m.row().getId());
      ev.setCorrelationSignal(m.signal());
      ev.setClassification(clazz);
      events.save(ev);
      return;
    }
    ev.setStatus("UNCORRELATED");
    ev.setClassification(clazz);
    events.save(ev);
    ops.opsLog(
        "bounce.uncorrelated",
        "id=" + ev.getId() + " subject=" + safe(ev.getSubject()) + " msgId=" + safe(ev.getMessageId()));
    ops.attentionAdmin(
        "Uncorrelated bounce",
        "Bounce " + ev.getId() + " could not be matched to a send (AD-25).");
  }

  private Optional<Match> ladder(BounceEventEntity ev, String blob) {
    if (verpOn()) {
      Optional<Match> verp = matchVerp(blob);
      if (verp.isPresent()) {
        return verp;
      }
    }
    Optional<Match> token = matchToken(blob);
    if (token.isPresent()) {
      return token;
    }
    Optional<Match> mid = matchMessageId(ev, blob);
    if (mid.isPresent()) {
      return mid;
    }
    return matchSubjectEmail(ev, blob);
  }

  private Optional<Match> matchVerp(String blob) {
    Matcher m = TOKEN.matcher(blob);
    while (m.find()) {
      String id = m.group(1);
      Optional<SendQueueEntity> byToken = queue.findByCorrelationToken(id);
      if (byToken.isPresent()) {
        return Optional.of(new Match(byToken.get(), "VERP"));
      }
    }
    return Optional.empty();
  }

  private Optional<Match> matchToken(String blob) {
    Matcher m = TOKEN.matcher(blob);
    while (m.find()) {
      Optional<SendQueueEntity> row = queue.findByCorrelationToken(m.group(1));
      if (row.isPresent()) {
        return Optional.of(new Match(row.get(), "TOKEN"));
      }
    }
    return Optional.empty();
  }

  private Optional<Match> matchMessageId(BounceEventEntity ev, String blob) {
    if (StringUtils.hasText(ev.getMessageId())) {
      Optional<SendQueueEntity> row = queue.findBySmtpMessageId(ev.getMessageId().trim());
      if (row.isPresent()) {
        return Optional.of(new Match(row.get(), "MESSAGE_ID"));
      }
    }
    Matcher m = MSG_ID.matcher(blob);
    while (m.find()) {
      Optional<SendQueueEntity> row = queue.findBySmtpMessageId(m.group());
      if (row.isPresent()) {
        return Optional.of(new Match(row.get(), "MESSAGE_ID"));
      }
    }
    return Optional.empty();
  }

  private Optional<Match> matchSubjectEmail(BounceEventEntity ev, String blob) {
    String subject = ev.getSubject() == null ? "" : ev.getSubject().trim();
    if (!StringUtils.hasText(subject)) {
      return Optional.empty();
    }
    // strip common bounce prefixes
    String sub =
        subject.replaceFirst("(?i)^(Undelivered Mail Returned to Sender|Delivery Status Notification).*?:\\s*", "");
    List<SendQueueEntity> hits =
        queue.findByStatusAndSubjectIgnoreCase("SENT", sub).stream()
            .filter(
                r ->
                    blob.toLowerCase(Locale.ROOT)
                        .contains(r.getEmail().toLowerCase(Locale.ROOT)))
            .toList();
    if (hits.size() == 1) {
      return Optional.of(new Match(hits.get(0), "SUBJECT_EMAIL"));
    }
    return Optional.empty();
  }

  static String classify(String blob) {
    Matcher m = STATUS.matcher(blob);
    if (m.find()) {
      return m.group(1).startsWith("5") ? "HARD" : "SOFT";
    }
    String lower = blob.toLowerCase(Locale.ROOT);
    if (lower.contains("user unknown")
        || lower.contains("mailbox not found")
        || lower.contains("does not exist")) {
      return "HARD";
    }
    return "SOFT";
  }

  private boolean verpOn() {
    return parameters
        .get(ParameterKeys.VERP_ENABLED)
        .map(v -> "true".equalsIgnoreCase(v.trim()))
        .orElse(false);
  }

  private static String textOf(BounceEventEntity ev) {
    StringBuilder sb = new StringBuilder();
    if (ev.getHeadersJson() != null) {
      sb.append(ev.getHeadersJson()).append('\n');
    }
    if (ev.getSubject() != null) {
      sb.append(ev.getSubject()).append('\n');
    }
    if (ev.getRawBytes() != null) {
      sb.append(new String(ev.getRawBytes(), StandardCharsets.UTF_8));
    }
    return sb.toString();
  }

  private static String safe(String s) {
    return s == null ? "" : s.substring(0, Math.min(80, s.length()));
  }

  private record Match(SendQueueEntity row, String signal) {}
}
