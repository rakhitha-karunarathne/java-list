package com.listmailer.unsubscribe;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Compact unsub payload: unsub|memberId|campaignId|listId,...|queueId */
public final class UnsubTokenCodec {

  public static final String PURPOSE = "unsub";

  private UnsubTokenCodec() {}

  public record Payload(String memberId, String campaignId, List<String> listIds, String queueId) {}

  public static byte[] encode(Payload p) {
    String lists = String.join(",", p.listIds());
    String raw =
        PURPOSE
            + "|"
            + p.memberId()
            + "|"
            + p.campaignId()
            + "|"
            + lists
            + "|"
            + p.queueId();
    return raw.getBytes(StandardCharsets.UTF_8);
  }

  public static Payload decode(byte[] bytes) {
    String raw = new String(bytes, StandardCharsets.UTF_8);
    String[] parts = raw.split("\\|", -1);
    if (parts.length != 5 || !PURPOSE.equals(parts[0])) {
      throw new IllegalArgumentException("bad unsub payload");
    }
    List<String> lists =
        parts[3].isEmpty() ? List.of() : Arrays.asList(parts[3].split(",", -1));
    return new Payload(parts[1], parts[2], lists, parts[4]);
  }
}
