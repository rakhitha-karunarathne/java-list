package com.listmailer.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SecretsConfigTest {

  private static MockEnvironment emptyEnv() {
    return new MockEnvironment();
  }

  @Test
  void buildsCryptoFromVersionedKeyList() {
    SecretsProperties props = new SecretsProperties();
    SecretsProperties.KeyEntry v1 = new SecretsProperties.KeyEntry();
    v1.setVersion(1);
    v1.setKey("dev-only-secrets-key-32chars-minimum!");
    SecretsProperties.KeyEntry v2 = new SecretsProperties.KeyEntry();
    v2.setVersion(2);
    v2.setKey("second-secrets-key-32chars-minimum!!!");
    props.setKeys(List.of(v1, v2));

    SecretsCrypto crypto = new SecretsConfig().secretsCrypto(props, emptyEnv());
    assertEquals(2, crypto.currentVersion());
    byte[] blob = crypto.encrypt(1, "legacy".getBytes());
    assertEquals("legacy", crypto.decryptUtf8(1, blob));
    assertEquals("new", crypto.decryptUtf8(2, crypto.encryptCurrent("new")));
  }

  @Test
  void buildsCryptoFromNumberedEnvVars() {
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("MAILER_SECRETS_KEY_1", "dev-only-secrets-key-32chars-minimum!")
            .withProperty("MAILER_SECRETS_KEY_2", "second-secrets-key-32chars-minimum!!!");
    SecretsProperties props = new SecretsProperties();
    props.setKeys(List.of());

    SecretsCrypto crypto = new SecretsConfig().secretsCrypto(props, env);
    assertEquals(2, crypto.currentVersion());
    assertEquals("legacy", crypto.decryptUtf8(1, crypto.encrypt(1, "legacy".getBytes())));
    assertEquals("new", crypto.decryptUtf8(2, crypto.encryptCurrent("new")));
  }

  @Test
  void activeKeyVersionOverridesHighest() {
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("MAILER_SECRETS_KEY_1", "dev-only-secrets-key-32chars-minimum!")
            .withProperty("MAILER_SECRETS_KEY_2", "second-secrets-key-32chars-minimum!!!")
            .withProperty("MAILER_SECRETS_KEY_3", "third-secrets-key-32chars-minimum!!!!")
            .withProperty("MAILER_SECRETS_ACTIVE_KEY_VERSION", "2");
    SecretsCrypto crypto = new SecretsConfig().secretsCrypto(new SecretsProperties(), env);
    assertEquals(2, crypto.currentVersion());
    assertEquals(3, crypto.maxConfiguredVersion());
    assertEquals("on-v2", crypto.decryptUtf8(2, crypto.encryptCurrent("on-v2")));
  }

  @Test
  void activeKeyVersionMissingKeyFails() {
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("MAILER_SECRETS_KEY_1", "dev-only-secrets-key-32chars-minimum!")
            .withProperty("MAILER_SECRETS_ACTIVE_KEY_VERSION", "9");
    assertThrows(
        IllegalStateException.class,
        () -> new SecretsConfig().secretsCrypto(new SecretsProperties(), env));
  }

  @Test
  void envKeysOverrideYamlFallback() {
    MockEnvironment env =
        new MockEnvironment()
            .withProperty("MAILER_SECRETS_KEY_1", "env-only-secrets-key-32chars-minimum!!");
    SecretsProperties props = new SecretsProperties();
    SecretsProperties.KeyEntry yaml = new SecretsProperties.KeyEntry();
    yaml.setVersion(1);
    yaml.setKey("yaml-only-secrets-key-32chars-minimum!");
    props.setKeys(List.of(yaml));

    SecretsCrypto crypto = new SecretsConfig().secretsCrypto(props, env);
    assertEquals(1, crypto.currentVersion());
    assertEquals("x", crypto.decryptUtf8(1, crypto.encryptCurrent("x")));
  }

  @Test
  void usesDevDefaultWhenNothingConfigured() {
    SecretsCrypto crypto = new SecretsConfig().secretsCrypto(new SecretsProperties(), emptyEnv());
    assertEquals(1, crypto.currentVersion());
    assertEquals("ok", crypto.decryptUtf8(1, crypto.encryptCurrent("ok")));
  }

  @Test
  void rejectsDuplicateVersions() {
    SecretsProperties props = new SecretsProperties();
    SecretsProperties.KeyEntry a = new SecretsProperties.KeyEntry();
    a.setVersion(1);
    a.setKey("dev-only-secrets-key-32chars-minimum!");
    SecretsProperties.KeyEntry b = new SecretsProperties.KeyEntry();
    b.setVersion(1);
    b.setKey("second-secrets-key-32chars-minimum!!!");
    props.setKeys(List.of(a, b));
    assertThrows(
        IllegalStateException.class, () -> new SecretsConfig().secretsCrypto(props, emptyEnv()));
  }

  @Test
  void migrateOnStartupSkippedWhenFalse() throws Exception {
    SecretsProperties props = new SecretsProperties();
    props.setMigrateOnStartup(false);
    SecretStore store = mock(SecretStore.class);
    new SecretsConfig().migrateSecretsOnStartup(props, store).run(null);
    verify(store, never()).rewrapAllToLatest();
  }

  @Test
  void migrateOnStartupRewrapsAndFailsHard() throws Exception {
    SecretsProperties props = new SecretsProperties();
    props.setMigrateOnStartup(true);
    SecretStore store = mock(SecretStore.class);
    when(store.rewrapAllToLatest())
        .thenReturn(new SecretStore.RewrapResult(2, 1, 0, 3));
    new SecretsConfig().migrateSecretsOnStartup(props, store).run(null);
    verify(store).rewrapAllToLatest();

    when(store.rewrapAllToLatest())
        .thenReturn(new SecretStore.RewrapResult(0, 0, 1, 3));
    assertThrows(
        IllegalStateException.class,
        () -> new SecretsConfig().migrateSecretsOnStartup(props, store).run(null));
  }

  @Test
  void activeKeyVersionFromPropertiesWhenEnvUnset() {
    SecretsProperties props = new SecretsProperties();
    SecretsProperties.KeyEntry v1 = new SecretsProperties.KeyEntry();
    v1.setVersion(1);
    v1.setKey("dev-only-secrets-key-32chars-minimum!");
    SecretsProperties.KeyEntry v2 = new SecretsProperties.KeyEntry();
    v2.setVersion(2);
    v2.setKey("second-secrets-key-32chars-minimum!!!");
    SecretsProperties.KeyEntry v3 = new SecretsProperties.KeyEntry();
    v3.setVersion(3);
    v3.setKey("third-secrets-key-32chars-minimum!!!!");
    props.setKeys(List.of(v1, v2, v3));
    props.setActiveKeyVersion(2);

    SecretsCrypto crypto = new SecretsConfig().secretsCrypto(props, emptyEnv());
    assertEquals(2, crypto.currentVersion());
    assertEquals(3, crypto.maxConfiguredVersion());
  }

  @Test
  void rejectsShortOrBlankKeyEntries() {
    SecretsProperties props = new SecretsProperties();
    SecretsProperties.KeyEntry shortKey = new SecretsProperties.KeyEntry();
    shortKey.setVersion(1);
    shortKey.setKey("too-short");
    props.setKeys(List.of(shortKey));
    assertThrows(
        IllegalStateException.class, () -> new SecretsConfig().secretsCrypto(props, emptyEnv()));
  }

  @Test
  void rejectsShortEnvKey() {
    MockEnvironment env = new MockEnvironment().withProperty("MAILER_SECRETS_KEY_1", "short");
    assertThrows(
        IllegalStateException.class,
        () -> new SecretsConfig().secretsCrypto(new SecretsProperties(), env));
  }
}
