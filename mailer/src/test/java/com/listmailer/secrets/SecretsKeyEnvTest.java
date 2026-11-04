package com.listmailer.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SecretsKeyEnvTest {

  @Test
  void readsNumberedKeysAndSkipsBlank() {
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("MAILER_SECRETS_KEY_1", "  one-key-at-least-thirty-two-bytes!!  ")
            .withProperty("MAILER_SECRETS_KEY_2", " ")
            .withProperty("MAILER_SECRETS_KEY_3", "three-key-at-least-thirty-two-bytes!");
    Map<Integer, String> keys = SecretsKeyEnv.read(env);
    assertEquals(2, keys.size());
    assertEquals("one-key-at-least-thirty-two-bytes!!", keys.get(1));
    assertEquals("three-key-at-least-thirty-two-bytes!", keys.get(3));
    assertTrue(!keys.containsKey(2));
  }

  @Test
  void emptyWhenNoneSet() {
    assertTrue(SecretsKeyEnv.read(new MockEnvironment()).isEmpty());
  }
}
