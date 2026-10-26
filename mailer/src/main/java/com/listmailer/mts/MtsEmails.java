package com.listmailer.mts;

import java.util.Locale;
import org.springframework.util.StringUtils;

final class MtsEmails {

  private MtsEmails() {}

  static String extract(String from) {
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

  static String domain(String email) {
    String e = extract(email);
    int at = e.lastIndexOf('@');
    return at > 0 ? e.substring(at + 1) : "";
  }
}
