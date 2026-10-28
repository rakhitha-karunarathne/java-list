package com.listmailer.secrets;

import com.listmailer.kernel.ids.Guids;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SecretStore {

  private static final Logger log = LoggerFactory.getLogger(SecretStore.class);

  private final SecretRepository secrets;
  private final SecretsCrypto crypto;

  public SecretStore(SecretRepository secrets, SecretsCrypto crypto) {
    this.secrets = secrets;
    this.crypto = crypto;
  }

  public int currentKeyVersion() {
    return crypto.currentVersion();
  }

  public long outdatedCount() {
    return secrets.countByKeyVersionLessThan(crypto.currentVersion());
  }

  /** Rows encrypted with a key version higher than any configured key (config was rolled back). */
  public long countWithKeyVersionAboveCurrent() {
    return secrets.countByKeyVersionGreaterThan(crypto.currentVersion());
  }

  public String requirePlain(String secretId) {
    if (!StringUtils.hasText(secretId)) {
      throw new IllegalStateException("missing secret id");
    }
    SecretEntity row =
        secrets
            .findById(secretId)
            .orElseThrow(() -> new IllegalStateException("secret not found: " + secretId));
    return crypto.decryptUtf8(row.getKeyVersion(), row.getCiphertext());
  }

  @Transactional
  public String create(String plain) {
    if (!StringUtils.hasText(plain)) {
      throw new IllegalArgumentException("secret value required");
    }
    String id = Guids.newGuid();
    secrets.save(
        new SecretEntity(id, crypto.currentVersion(), crypto.encryptCurrent(plain), Instant.now()));
    return id;
  }

  @Transactional
  public void update(String secretId, String plain) {
    if (!StringUtils.hasText(secretId)) {
      throw new IllegalArgumentException("secret id required");
    }
    if (!StringUtils.hasText(plain)) {
      throw new IllegalArgumentException("secret value required");
    }
    SecretEntity row =
        secrets
            .findById(secretId)
            .orElseThrow(() -> new IllegalStateException("secret not found: " + secretId));
    row.setKeyVersion(crypto.currentVersion());
    row.setCiphertext(crypto.encryptCurrent(plain));
    secrets.save(row);
  }

  /** Create or replace. Returns secret id (new or existing). */
  @Transactional
  public String put(String existingSecretId, String plain) {
    if (StringUtils.hasText(existingSecretId) && secrets.existsById(existingSecretId)) {
      update(existingSecretId, plain);
      return existingSecretId;
    }
    return create(plain);
  }

  @Transactional
  public void delete(String secretId) {
    if (StringUtils.hasText(secretId)) {
      secrets.deleteById(secretId);
    }
  }

  @Transactional
  public RewrapResult rewrapAllToLatest() {
    int current = crypto.currentVersion();
    int alreadyCurrent = (int) (secrets.count() - secrets.countByKeyVersionLessThan(current));
    int migrated = 0;
    int failed = 0;
    for (SecretEntity row : secrets.findByKeyVersionLessThan(current)) {
      int oldVer = row.getKeyVersion();
      byte[] oldCipher = row.getCiphertext();
      try {
        // Encrypt before mutating the entity so a failed encrypt cannot leave
        // key_version bumped with the old ciphertext (undecryptable on flush).
        String plain = crypto.decryptUtf8(oldVer, oldCipher);
        byte[] next = crypto.encryptCurrent(plain);
        row.setKeyVersion(current);
        row.setCiphertext(next);
        secrets.save(row);
        migrated++;
      } catch (RuntimeException ex) {
        row.setKeyVersion(oldVer);
        row.setCiphertext(oldCipher);
        failed++;
        log.warn(
            "secret rewrap failed id={} fromKeyVersion={} toKeyVersion={}",
            row.getId(),
            oldVer,
            current,
            ex);
      }
    }
    return new RewrapResult(migrated, alreadyCurrent, failed, current);
  }

  public record RewrapResult(int migrated, int alreadyCurrent, int failed, int currentKeyVersion) {}
}
