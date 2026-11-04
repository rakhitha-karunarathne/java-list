package com.listmailer.secrets;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-GCM for secrets table. {@link #currentVersion()} is the active encrypt/migrate target
 * (explicit or highest configured).
 */
public final class SecretsCrypto {

  private static final int IV_LEN = 12;
  private static final int TAG_BITS = 128;

  private final TreeMap<Integer, byte[]> keys;
  private final int activeVersion;
  private final SecureRandom random = new SecureRandom();

  public SecretsCrypto(Map<Integer, String> rawKeys) {
    this(rawKeys, null);
  }

  /**
   * @param activeVersionOrNull when null, active = highest configured version
   */
  public SecretsCrypto(Map<Integer, String> rawKeys, Integer activeVersionOrNull) {
    if (rawKeys == null || rawKeys.isEmpty()) {
      throw new IllegalStateException(
          "At least one secrets master key is required (MAILER_SECRETS_KEY_<n>)");
    }
    TreeMap<Integer, byte[]> map = new TreeMap<>();
    for (Map.Entry<Integer, String> e : rawKeys.entrySet()) {
      if (e.getKey() == null || e.getValue() == null) {
        continue;
      }
      int version = e.getKey();
      if (version < 1 || version > SecretsKeyEnv.MAX_VERSION) {
        throw new IllegalStateException(
            "MAILER_SECRETS_KEY version must be 1.."
                + SecretsKeyEnv.MAX_VERSION
                + " (got "
                + version
                + ")");
      }
      byte[] raw = e.getValue().getBytes(StandardCharsets.UTF_8);
      if (raw.length < 32) {
        throw new IllegalStateException(
            "MAILER_SECRETS_KEY_" + version + " must be at least 32 bytes");
      }
      map.put(version, Arrays.copyOf(raw, 32));
    }
    if (map.isEmpty()) {
      throw new IllegalStateException(
          "At least one secrets master key is required (MAILER_SECRETS_KEY_<n>)");
    }
    this.keys = map;
    if (activeVersionOrNull == null) {
      this.activeVersion = map.lastKey();
    } else {
      if (!map.containsKey(activeVersionOrNull)) {
        throw new IllegalStateException(
            "MAILER_SECRETS_ACTIVE_KEY_VERSION="
                + activeVersionOrNull
                + " requires MAILER_SECRETS_KEY_"
                + activeVersionOrNull
                + " to be set");
      }
      this.activeVersion = activeVersionOrNull;
    }
  }

  /** Version used for new encryption and rewrap target. */
  public int currentVersion() {
    return activeVersion;
  }

  /** Highest configured key index (for detecting rows that need a restored key). */
  public int maxConfiguredVersion() {
    return keys.lastKey();
  }

  public byte[] encrypt(int version, byte[] plain) {
    byte[] key = keys.get(version);
    if (key == null) {
      throw new IllegalStateException("unknown secrets key version " + version);
    }
    try {
      byte[] iv = new byte[IV_LEN];
      random.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      byte[] cipherText = cipher.doFinal(plain);
      ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherText.length);
      buf.put(iv);
      buf.put(cipherText);
      return buf.array();
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("secret encrypt failed", ex);
    }
  }

  public byte[] decrypt(int version, byte[] blob) {
    byte[] key = keys.get(version);
    if (key == null) {
      throw new IllegalStateException("unknown secrets key version " + version);
    }
    if (blob == null || blob.length <= IV_LEN) {
      throw new IllegalArgumentException("ciphertext too short");
    }
    try {
      byte[] iv = Arrays.copyOfRange(blob, 0, IV_LEN);
      byte[] cipherText = Arrays.copyOfRange(blob, IV_LEN, blob.length);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      return cipher.doFinal(cipherText);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("secret decrypt failed", ex);
    }
  }

  public byte[] encryptCurrent(String plain) {
    return encrypt(currentVersion(), plain.getBytes(StandardCharsets.UTF_8));
  }

  public String decryptUtf8(int version, byte[] blob) {
    return new String(decrypt(version, blob), StandardCharsets.UTF_8);
  }
}
