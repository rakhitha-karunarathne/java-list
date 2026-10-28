package com.listmailer.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SecretsCryptoTest {

  @Test
  void roundTripAndCurrentIsHighest() {
    SecretsCrypto crypto =
        new SecretsCrypto(
            Map.of(
                1, "dev-only-secrets-key-32chars-minimum!",
                2, "second-secrets-key-32chars-minimum!!!"));
    assertEquals(2, crypto.currentVersion());
    byte[] blob = crypto.encrypt(1, "hello".getBytes());
    assertEquals("hello", crypto.decryptUtf8(1, blob));
    byte[] cur = crypto.encryptCurrent("world");
    assertEquals("world", crypto.decryptUtf8(2, cur));
  }

  @Test
  void rejectsShortKey() {
    assertThrows(
        IllegalStateException.class, () -> new SecretsCrypto(Map.of(1, "too-short")));
  }

  @Test
  void unknownVersionFailsDecrypt() {
    SecretsCrypto crypto = new SecretsCrypto(Map.of(1, "dev-only-secrets-key-32chars-minimum!"));
    byte[] blob = crypto.encryptCurrent("x");
    SecretsCrypto onlyV2 =
        new SecretsCrypto(Map.of(2, "second-secrets-key-32chars-minimum!!!"));
    assertThrows(IllegalStateException.class, () -> onlyV2.decryptUtf8(1, blob));
    assertTrue(blob.length > 12);
  }
}
