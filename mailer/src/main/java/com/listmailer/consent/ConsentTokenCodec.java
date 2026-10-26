package com.listmailer.consent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** approve|memberId|listIds,|expiresEpochMs */
public final class ConsentTokenCodec {

  public static final String PURPOSE = "approve";

  private ConsentTokenCodec() {}

  public record Payload(String memberId, List<String> listIds, long expiresEpochMs) {}

  public static byte[] encode(Payload p) {
    String raw =
        PURPOSE
            + "|"
            + p.memberId()
            + "|"
            + String.join(",", p.listIds())
            + "|"
            + p.expiresEpochMs();
    return raw.getBytes(StandardCharsets.UTF_8);
  }

  public static Payload decode(byte[] bytes) {
    String raw = new String(bytes, StandardCharsets.UTF_8);
    String[] parts = raw.split("\\|", -1);
    if (parts.length != 4 || !PURPOSE.equals(parts[0])) {
      throw new IllegalArgumentException("bad approve payload");
    }
    List<String> lists =
        parts[2].isEmpty() ? List.of() : Arrays.asList(parts[2].split(",", -1));
    return new Payload(parts[1], lists, Long.parseLong(parts[3]));
  }
}
