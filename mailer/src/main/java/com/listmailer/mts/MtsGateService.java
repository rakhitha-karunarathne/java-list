package com.listmailer.mts;

import com.listmailer.kernel.ports.MailboxPort;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Auth → spam-header-if-present → authorization matrix (R3). DMARC fail = reject treatment.
 */
@Service
public class MtsGateService {

  private static final Pattern AUTH_RESULTS =
      Pattern.compile("(?i)(dmarc|spf|dkim)=(\\w+)");
  private static final Pattern SPAM_SCORE =
      Pattern.compile("(?i)(?:X-Spam-Score|X-Spam-Status):\\s*([-+]?\\d+(?:\\.\\d+)?)");

  private final MtsAuthorizedSenderRepository authorized;
  private final MtsDropService drops;

  public MtsGateService(MtsAuthorizedSenderRepository authorized, MtsDropService drops) {
    this.authorized = authorized;
    this.drops = drops;
  }

  public record Decision(MtsGateResult result, String reason, boolean countAutoBlock) {}

  public Decision evaluate(String inboxId, String inboxName, MailboxPort.RawMessage raw) {
    String from = MtsEmails.extract(raw.from());
    if (!StringUtils.hasText(from)) {
      return new Decision(MtsGateResult.DROP_SILENT, "missing_from", false);
    }
    if (drops.isBlocked(from)) {
      return new Decision(MtsGateResult.DROP_SILENT, "blocked", false);
    }

    boolean authFail = authFails(raw);
    Double spam = spamScore(raw);
    boolean spamFail = spam != null && spam >= spamThreshold();

    boolean onThis = authorized.existsByInboxIdAndEmail(inboxId, from);
    boolean onAny = authorized.existsByEmail(from);

    if (authFail || spamFail) {
      String reason = authFail ? (dmarcFail(raw) ? "dmarc_fail" : "auth_fail") : "spam_fail";
      if (onThis) {
        return new Decision(MtsGateResult.DROP_SILENT, reason, false);
      }
      return new Decision(MtsGateResult.DROP_COUNTED, reason + "_stranger", true);
    }

    if (onThis) {
      return new Decision(MtsGateResult.ACCEPT, "authorized", false);
    }
    if (onAny) {
      return new Decision(MtsGateResult.NOTIFY_CROSS_INBOX, "cross_inbox", false);
    }
    return new Decision(MtsGateResult.DROP_COUNTED, "stranger_unapproved", true);
  }

  private boolean authFails(MailboxPort.RawMessage raw) {
    if (dmarcFail(raw)) {
      return true;
    }
    String ar = header(raw, "Authentication-Results");
    if (!StringUtils.hasText(ar)) {
      return false; // no header → skip auth fail (V1 soft)
    }
    Matcher m = AUTH_RESULTS.matcher(ar);
    while (m.find()) {
      String mech = m.group(1).toLowerCase(Locale.ROOT);
      String res = m.group(2).toLowerCase(Locale.ROOT);
      if ("dmarc".equals(mech) && "fail".equals(res)) {
        return true;
      }
      if (("spf".equals(mech) || "dkim".equals(mech)) && "fail".equals(res)) {
        return true;
      }
    }
    return false;
  }

  private boolean dmarcFail(MailboxPort.RawMessage raw) {
    String ar = header(raw, "Authentication-Results");
    if (!StringUtils.hasText(ar)) {
      return false;
    }
    Matcher m = AUTH_RESULTS.matcher(ar);
    while (m.find()) {
      if ("dmarc".equalsIgnoreCase(m.group(1)) && "fail".equalsIgnoreCase(m.group(2))) {
        return true;
      }
    }
    return false;
  }

  private Double spamScore(MailboxPort.RawMessage raw) {
    for (Map.Entry<String, String> e : raw.headers().entrySet()) {
      if (e.getKey() != null
          && (e.getKey().equalsIgnoreCase("X-Spam-Score")
              || e.getKey().equalsIgnoreCase("X-Spam-Status"))) {
        Matcher m = SPAM_SCORE.matcher(e.getKey() + ": " + e.getValue());
        if (m.find()) {
          return Double.parseDouble(m.group(1));
        }
        try {
          return Double.parseDouble(e.getValue().trim().split("\\s+")[0]);
        } catch (NumberFormatException ignored) {
          return null;
        }
      }
    }
    return null; // absent → skip
  }

  private double spamThreshold() {
    return 5.0; // global default; per-list strictest deferred
  }

  private static String header(MailboxPort.RawMessage raw, String name) {
    if (raw.headers() == null) {
      return null;
    }
    for (Map.Entry<String, String> e : raw.headers().entrySet()) {
      if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
        return e.getValue();
      }
    }
    return null;
  }

  /** @deprecated use {@link MtsEmails#extract} */
  static String extractEmail(String from) {
    return MtsEmails.extract(from);
  }
}
