package com.listmailer.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class SecretsConfigTest {

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

    SecretsCrypto crypto = new SecretsConfig().secretsCrypto(props);
    assertEquals(2, crypto.currentVersion());
    byte[] blob = crypto.encrypt(1, "legacy".getBytes());
    assertEquals("legacy", crypto.decryptUtf8(1, blob));
    assertEquals("new", crypto.decryptUtf8(2, crypto.encryptCurrent("new")));
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
    assertThrows(IllegalStateException.class, () -> new SecretsConfig().secretsCrypto(props));
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
    when(store.countWithKeyVersionAboveCurrent()).thenReturn(0L);
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
  void migrateOnStartupRejectsRowsAboveConfiguredKeys() {
    SecretsProperties props = new SecretsProperties();
    props.setMigrateOnStartup(true);
    SecretStore store = mock(SecretStore.class);
    when(store.countWithKeyVersionAboveCurrent()).thenReturn(2L);
    assertThrows(
        IllegalStateException.class,
        () -> new SecretsConfig().migrateSecretsOnStartup(props, store).run(null));
    verify(store, never()).rewrapAllToLatest();
  }

  @Test
  void rejectsShortOrBlankKeyEntries() {
    SecretsProperties props = new SecretsProperties();
    SecretsProperties.KeyEntry shortKey = new SecretsProperties.KeyEntry();
    shortKey.setVersion(1);
    shortKey.setKey("too-short");
    props.setKeys(List.of(shortKey));
    assertThrows(IllegalStateException.class, () -> new SecretsConfig().secretsCrypto(props));
  }
}
