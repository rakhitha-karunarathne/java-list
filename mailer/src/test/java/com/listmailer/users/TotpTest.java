package com.listmailer.users;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TotpTest {

  @Test
  void verifiesCurrentCode() {
    String secret = Totp.newSecret();
    long step = Instant.now().getEpochSecond() / 30;
    String code = Totp.generate(secret, step);
    assertTrue(Totp.verify(secret, code, Instant.now()));
    assertFalse(Totp.verify(secret, "000000", Instant.now()));
  }
}
