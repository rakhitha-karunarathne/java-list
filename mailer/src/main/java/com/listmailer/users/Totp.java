package com.listmailer.users;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** RFC 6238 TOTP (SHA1, 30s, 6 digits) with Base32 secrets. */
public final class Totp {

  private static final char[] BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
  private static final SecureRandom RANDOM = new SecureRandom();

  private Totp() {}

  public static String newSecret() {
    byte[] raw = new byte[20];
    RANDOM.nextBytes(raw);
    return encodeBase32(raw);
  }

  public static boolean verify(String base32Secret, String code, Instant now) {
    if (code == null || !code.matches("\\d{6}")) {
      return false;
    }
    long step = now.getEpochSecond() / 30;
    for (long drift = -1; drift <= 1; drift++) {
      if (code.equals(generate(base32Secret, step + drift))) {
        return true;
      }
    }
    return false;
  }

  public static String otpAuthUrl(String issuer, String account, String secret) {
    return "otpauth://totp/"
        + url(issuer)
        + ":"
        + url(account)
        + "?secret="
        + secret
        + "&issuer="
        + url(issuer)
        + "&digits=6&period=30";
  }

  static String generate(String base32Secret, long timeStep) {
    try {
      byte[] key = decodeBase32(base32Secret);
      byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(key, "HmacSHA1"));
      byte[] hash = mac.doFinal(data);
      int offset = hash[hash.length - 1] & 0x0f;
      int binary =
          ((hash[offset] & 0x7f) << 24)
              | ((hash[offset + 1] & 0xff) << 16)
              | ((hash[offset + 2] & 0xff) << 8)
              | (hash[offset + 3] & 0xff);
      return String.format("%06d", binary % 1_000_000);
    } catch (Exception e) {
      throw new IllegalStateException("TOTP generate failed", e);
    }
  }

  private static String encodeBase32(byte[] data) {
    StringBuilder out = new StringBuilder((data.length * 8 + 4) / 5);
    int buffer = 0;
    int bitsLeft = 0;
    for (byte b : data) {
      buffer = (buffer << 8) | (b & 0xff);
      bitsLeft += 8;
      while (bitsLeft >= 5) {
        out.append(BASE32[(buffer >> (bitsLeft - 5)) & 31]);
        bitsLeft -= 5;
      }
    }
    if (bitsLeft > 0) {
      out.append(BASE32[(buffer << (5 - bitsLeft)) & 31]);
    }
    return out.toString();
  }

  private static byte[] decodeBase32(String encoded) {
    String s = encoded.trim().replace("=", "").toUpperCase();
    ByteBuffer buf = ByteBuffer.allocate((s.length() * 5) / 8);
    int buffer = 0;
    int bitsLeft = 0;
    for (int i = 0; i < s.length(); i++) {
      int val = indexOfBase32(s.charAt(i));
      if (val < 0) {
        throw new IllegalArgumentException("invalid base32");
      }
      buffer = (buffer << 5) | val;
      bitsLeft += 5;
      if (bitsLeft >= 8) {
        buf.put((byte) ((buffer >> (bitsLeft - 8)) & 0xff));
        bitsLeft -= 8;
      }
    }
    byte[] out = new byte[buf.position()];
    buf.flip();
    buf.get(out);
    return out;
  }

  private static int indexOfBase32(char c) {
    if (c >= 'A' && c <= 'Z') {
      return c - 'A';
    }
    if (c >= '2' && c <= '7') {
      return 26 + (c - '2');
    }
    return -1;
  }

  private static String url(String value) {
    return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
  }
}
