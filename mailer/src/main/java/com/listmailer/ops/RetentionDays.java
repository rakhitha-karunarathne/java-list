package com.listmailer.ops;

import com.listmailer.kernel.ports.ParametersPort;

public final class RetentionDays {

  private RetentionDays() {}

  public static int read(ParametersPort parameters, String key, int defaultDays) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                int x = Integer.parseInt(v.trim());
                return x > 0 ? x : defaultDays;
              } catch (NumberFormatException e) {
                return defaultDays;
              }
            })
        .orElse(defaultDays);
  }
}
