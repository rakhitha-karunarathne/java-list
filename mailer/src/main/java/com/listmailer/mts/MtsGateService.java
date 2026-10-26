package com.listmailer.mts;

import com.listmailer.kernel.ports.MailboxPort;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Auth/spam/From-domain gates. Story 8.1: authorized senders ACCEPT; strangers DROP_COUNTED.
 * Stories 2–4 refine matrix behavior.
 */
@Service
public class MtsGateService {

  private final MtsAuthorizedSenderRepository authorized;

  public MtsGateService(MtsAuthorizedSenderRepository authorized) {
    this.authorized = authorized;
  }

  public MtsGateResult evaluate(String inboxId, MailboxPort.RawMessage raw) {
    String from = extractEmail(raw.from());
    if (!StringUtils.hasText(from)) {
      return MtsGateResult.DROP_SILENT;
    }
    if (authorized.existsByInboxIdAndEmail(inboxId, from)) {
      // stub: auth/spam pass until stories 2–3; From-domain checked in ingest
      return MtsGateResult.ACCEPT;
    }
    if (authorized.existsByEmail(from)) {
      return MtsGateResult.NOTIFY_CROSS_INBOX;
    }
    return MtsGateResult.DROP_COUNTED;
  }

  static String extractEmail(String from) {
    if (!StringUtils.hasText(from)) {
      return "";
    }
    String s = from.trim().toLowerCase(Locale.ROOT);
    int lt = s.indexOf('<');
    int gt = s.indexOf('>');
    if (lt >= 0 && gt > lt) {
      return s.substring(lt + 1, gt).trim();
    }
    return s;
  }
}
