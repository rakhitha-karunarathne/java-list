package com.listmailer.parameters;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import org.springframework.util.StringUtils;

/** Resolves the operator-facing site title Parameter. */
public final class SiteTitles {

  public static final String DEFAULT = "List Mailer";
  public static final int MAX_LENGTH = 64;

  private SiteTitles() {}

  public static String resolve(ParametersPort parameters) {
    if (parameters == null) {
      return DEFAULT;
    }
    return parameters
        .get(ParameterKeys.SITE_TITLE)
        .map(String::trim)
        .filter(StringUtils::hasText)
        .orElse(DEFAULT);
  }
}
