package com.listmailer.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecretStoreTest {

  private static final String KEY1 = "dev-only-secrets-key-32chars-minimum!";
  private static final String KEY2 = "second-secrets-key-32chars-minimum!!!";
  private static final String KEY3 = "third-secrets-key-32chars-minimum!!!!";

  private final Map<String, SecretEntity> db = new LinkedHashMap<>();
  private SecretStore store;
  private SecretsCrypto crypto;

  @BeforeEach
  void setUp() {
    crypto =
        new SecretsCrypto(
            Map.of(
                1, KEY1,
                2, KEY2));
    store = newStore(crypto);
  }

  private SecretStore newStore(SecretsCrypto c) {
    db.clear();
    SecretRepository secrets = mock(SecretRepository.class);
    when(secrets.save(any()))
        .thenAnswer(
            inv -> {
              SecretEntity e = inv.getArgument(0);
              db.put(e.getId(), e);
              return e;
            });
    when(secrets.findById(anyString()))
        .thenAnswer(inv -> Optional.ofNullable(db.get(inv.getArgument(0))));
    when(secrets.existsById(anyString())).thenAnswer(inv -> db.containsKey(inv.getArgument(0)));
    when(secrets.count()).thenAnswer(inv -> (long) db.size());
    when(secrets.countByKeyVersionGreaterThan(anyInt()))
        .thenAnswer(
            inv ->
                db.values().stream()
                    .filter(e -> e.getKeyVersion() > (int) inv.getArgument(0))
                    .count());
    when(secrets.countByKeyVersionNot(anyInt()))
        .thenAnswer(
            inv ->
                db.values().stream()
                    .filter(e -> e.getKeyVersion() != (int) inv.getArgument(0))
                    .count());
    when(secrets.findByKeyVersionNot(anyInt()))
        .thenAnswer(
            inv ->
                db.values().stream()
                    .filter(e -> e.getKeyVersion() != (int) inv.getArgument(0))
                    .collect(Collectors.toList()));
    return new SecretStore(secrets, c);
  }

  @Test
  @DisplayName("multi-key: v1 rows decrypt while current is v2; rewrap migrates them")
  void rewrapMigratesOlderKeyVersions() {
    assertEquals(2, store.currentKeyVersion());

    String legacyId = "legacy-secret-id-000000000001";
    db.put(
        legacyId,
        new SecretEntity(
            legacyId, 1, crypto.encrypt(1, "smtp-pass".getBytes()), Instant.now()));
    String currentId = store.create("fresh-pass");

    assertEquals("smtp-pass", store.requirePlain(legacyId));
    assertEquals("fresh-pass", store.requirePlain(currentId));
    assertEquals(1, store.outdatedCount());
    assertEquals(2, db.get(currentId).getKeyVersion());

    SecretStore.RewrapResult r = store.rewrapAllToLatest();
    assertEquals(1, r.migrated());
    assertEquals(1, r.alreadyCurrent());
    assertEquals(0, r.failed());
    assertEquals(2, r.currentKeyVersion());
    assertEquals(0, store.outdatedCount());
    assertEquals(2, db.get(legacyId).getKeyVersion());
    assertEquals("smtp-pass", store.requirePlain(legacyId));
    assertTrue(db.get(legacyId).getCiphertext().length > 12);
  }

  @Test
  @DisplayName("active key mid-range: rewrap moves higher-version rows down to active")
  void rewrapMigratesDownToActiveKey() {
    crypto =
        new SecretsCrypto(
            Map.of(
                1, KEY1,
                2, KEY2,
                3, KEY3),
            2);
    store = newStore(crypto);

    String highId = "high-secret-id-00000000000001";
    db.put(
        highId,
        new SecretEntity(highId, 3, crypto.encrypt(3, "was-v3".getBytes()), Instant.now()));
    String activeId = store.create("on-active");

    assertEquals(2, store.currentKeyVersion());
    assertEquals(1, store.outdatedCount());
    assertEquals(2, db.get(activeId).getKeyVersion());
    assertEquals(0, store.countWithKeyVersionAboveMaxConfigured());

    SecretStore.RewrapResult r = store.rewrapAllToLatest();
    assertEquals(1, r.migrated());
    assertEquals(0, r.failed());
    assertEquals(2, db.get(highId).getKeyVersion());
    assertEquals("was-v3", store.requirePlain(highId));
  }

  @Test
  void rewrapRejectsRowsAboveMaxConfiguredKey() {
    store = newStore(crypto);
    String orphan = "orphan-secret-id-000000000001";
    db.put(
        orphan,
        new SecretEntity(orphan, 9, new byte[24], Instant.now()));
    assertEquals(1, store.countWithKeyVersionAboveMaxConfigured());
    assertThrows(IllegalStateException.class, () -> store.rewrapAllToLatest());
  }
}
