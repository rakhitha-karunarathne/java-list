package com.listmailer.kernel.token;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AesGcmOpaqueTokenHelperTest {

  @Test
  void roundTripOpaque() {
    AesGcmOpaqueTokenHelper helper =
        new AesGcmOpaqueTokenHelper("dev-only-opaque-token-32chars-min!!");
    byte[] payload = "hello-payload".getBytes(StandardCharsets.UTF_8);
    String token = helper.seal(payload);
    assertTrue(token.length() > 20);
    assertTrue(!token.contains("hello"));
    assertArrayEquals(payload, helper.open(token));
  }

  @Test
  void tamperedFails() {
    AesGcmOpaqueTokenHelper helper =
        new AesGcmOpaqueTokenHelper("dev-only-opaque-token-32chars-min!!");
    String token = helper.seal("x".getBytes(StandardCharsets.UTF_8));
    String bad = token.substring(0, token.length() - 2) + "ab";
    assertThrows(IllegalArgumentException.class, () -> helper.open(bad));
  }
}
