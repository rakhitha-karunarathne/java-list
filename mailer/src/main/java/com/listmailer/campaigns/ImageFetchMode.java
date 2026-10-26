package com.listmailer.campaigns;

public enum ImageFetchMode {
  OFF,
  BEST_EFFORT,
  MANDATORY;

  public static ImageFetchMode from(String raw) {
    if (raw == null || raw.isBlank()) {
      return OFF;
    }
    return switch (raw.trim().toUpperCase().replace('-', '_').replace(' ', '_')) {
      case "BEST_EFFORT", "BESTEFFORT" -> BEST_EFFORT;
      case "MANDATORY" -> MANDATORY;
      default -> OFF;
    };
  }
}
