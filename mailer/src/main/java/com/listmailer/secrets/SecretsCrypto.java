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

/** AES-GCM for secrets table. Highest key version is current. */
public final class SecretsCrypto {

  private static final int IV_LEN = 12;
  private static final int TAG_BITS = 128;

  private final TreeMap<Integer, byte[]> keys;
  private final SecureRandom random = new SecureRandom();

  public SecretsCrypto(Map<Integer, String> rawKeys) {
    if (rawKeys == null || rawKeys.isEmpty()) {
      throw new IllegalStateException("mailer.secrets.keys must define at least one version");
    }
    TreeMap<Integer, byte[]> map = new TreeMap<>();
    for (Map.Entry<Integer, String> e : rawKeys.entrySet()) {
      if (e.getKey() == null || e.getValue() == null) {
        continue;
      }
      byte[] raw = e.getValue().getBytes(StandardCharsets.UTF_8);
      if (raw.length < 32) {
        throw new IllegalStateException(
            "mailer.secrets.keys." + e.getKey() + " must be at least 32 bytes");
      }
      map.put(e.getKey(), Arrays.copyOf(raw, 32));
    }
    if (map.isEmpty()) {
      throw new IllegalStateException("mailer.secrets.keys must define at least one version");
    }
    this.keys = map;
  }

  public int currentVersion() {
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
