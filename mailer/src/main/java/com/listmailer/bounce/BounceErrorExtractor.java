package com.listmailer.bounce;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/** Pulls a short operator-facing error line from bounce headers/body. */
final class BounceErrorExtractor {

  private static final Pattern DIAGNOSTIC =
      Pattern.compile("(?im)^diagnostic-code:\\s*(?:smtp;\\s*)?(.+)$");
  private static final Pattern STATUS = Pattern.compile("(?i)status:\\s*([245]\\.\\d+\\.\\d+)");
  private static final Pattern ACTION = Pattern.compile("(?im)^action:\\s*(.+)$");

  private BounceErrorExtractor() {}

  static String extract(String blob, String subjectFallback) {
    if (!StringUtils.hasText(blob)) {
      return truncate(subjectFallback, "bounce");
    }
    Matcher diag = DIAGNOSTIC.matcher(blob);
    if (diag.find()) {
      return truncate(diag.group(1).trim(), "bounce");
    }
    Matcher status = STATUS.matcher(blob);
    if (status.find()) {
      String code = status.group(1);
      String lower = blob.toLowerCase(Locale.ROOT);
      if (lower.contains("mailbox full") || lower.contains("quota")) {
        return truncate(code + " mailbox full", "bounce");
      }
      if (lower.contains("user unknown") || lower.contains("does not exist")) {
        return truncate(code + " user unknown", "bounce");
      }
      return truncate(code, "bounce");
    }
    Matcher action = ACTION.matcher(blob);
    if (action.find()) {
      return truncate(action.group(1).trim(), "bounce");
    }
    String lower = blob.toLowerCase(Locale.ROOT);
    if (lower.contains("mailbox full")) {
      return "mailbox full";
    }
    if (lower.contains("user unknown") || lower.contains("mailbox not found")) {
      return "user unknown";
    }
    return truncate(subjectFallback, "bounce");
  }

  private static String truncate(String value, String fallback) {
    if (!StringUtils.hasText(value)) {
      return fallback;
    }
    String v = value.trim().replaceAll("\\s+", " ");
    return v.length() > 240 ? v.substring(0, 240) : v;
  }
}
