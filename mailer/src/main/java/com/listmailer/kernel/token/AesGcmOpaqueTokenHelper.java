package com.listmailer.kernel.token;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** AES-GCM opaque member tokens (AD-18). Secret from env — never Parameters. */
@Component
@Primary
public class AesGcmOpaqueTokenHelper implements OpaqueTokenHelper {

  private static final int IV_LEN = 12;
  private static final int TAG_BITS = 128;

  private final byte[] key;
  private final SecureRandom random = new SecureRandom();

  public AesGcmOpaqueTokenHelper(@Value("${mailer.opaque-token.secret}") String secret) {
    byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
    if (raw.length < 32) {
      throw new IllegalStateException("mailer.opaque-token.secret must be at least 32 bytes");
    }
    this.key = Arrays.copyOf(raw, 32);
  }

  @Override
  public String seal(byte[] payload) {
    try {
      byte[] iv = new byte[IV_LEN];
      random.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      byte[] cipherText = cipher.doFinal(payload);
      ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherText.length);
      buf.put(iv);
      buf.put(cipherText);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(buf.array());
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("token seal failed", ex);
    }
  }

  @Override
  public byte[] open(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("empty token");
    }
    try {
      byte[] all = Base64.getUrlDecoder().decode(token);
      if (all.length <= IV_LEN) {
        throw new IllegalArgumentException("token too short");
      }
      byte[] iv = Arrays.copyOfRange(all, 0, IV_LEN);
      byte[] cipherText = Arrays.copyOfRange(all, IV_LEN, all.length);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
      return cipher.doFinal(cipherText);
    } catch (IllegalArgumentException | GeneralSecurityException ex) {
      throw new IllegalArgumentException("invalid token", ex);
    }
  }
}
