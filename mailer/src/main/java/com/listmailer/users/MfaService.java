package com.listmailer.users;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.secrets.SecretStore;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MfaService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final UserRepository users;
  private final UserBackupCodeRepository backupCodes;
  private final PasswordEncoder passwordEncoder;
  private final SecretStore secrets;

  public MfaService(
      UserRepository users,
      UserBackupCodeRepository backupCodes,
      PasswordEncoder passwordEncoder,
      SecretStore secrets) {
    this.users = users;
    this.backupCodes = backupCodes;
    this.passwordEncoder = passwordEncoder;
    this.secrets = secrets;
  }

  public Setup startSetup(UserEntity user) {
    if (user.isTotpEnabled()) {
      throw new IllegalStateException("MFA already enabled");
    }
    String secret = Totp.newSecret();
    String secretId = secrets.put(user.getTotpSecretId(), secret);
    user.setTotpSecretId(secretId);
    user.setTotpEnabled(false);
    users.save(user);
    return new Setup(secret, Totp.otpAuthUrl("List Mailer", user.getEmail(), secret));
  }

  @Transactional
  public List<String> confirmSetup(UserEntity user, String code) {
    if (!StringUtils.hasText(user.getTotpSecretId()) || user.isTotpEnabled()) {
      throw new IllegalStateException("MFA setup not started");
    }
    String totp = secrets.requirePlain(user.getTotpSecretId());
    if (!Totp.verify(totp, code, Instant.now())) {
      throw new IllegalArgumentException("Invalid TOTP code");
    }
    user.setTotpEnabled(true);
    users.save(user);
    backupCodes.deleteByUserId(user.getId());
    return issueBackupCodes(user.getId());
  }

  @Transactional
  public void unenroll(UserEntity user, String code) {
    if (!user.isTotpEnabled()) {
      throw new IllegalStateException("MFA not enabled");
    }
    if (!verifyCode(user, code)) {
      throw new IllegalArgumentException("Invalid MFA code");
    }
    user.setTotpEnabled(false);
    String secretId = user.getTotpSecretId();
    user.setTotpSecretId(null);
    users.save(user);
    secrets.delete(secretId);
    backupCodes.deleteByUserId(user.getId());
  }

  @Transactional
  public boolean verifyCode(UserEntity user, String code) {
    if (StringUtils.hasText(user.getTotpSecretId())
        && Totp.verify(secrets.requirePlain(user.getTotpSecretId()), code, Instant.now())) {
      return true;
    }
    if (code == null) {
      return false;
    }
    String normalized = code.replace("-", "").trim();
    for (UserBackupCodeEntity row : backupCodes.findByUserIdAndUsedAtIsNull(user.getId())) {
      if (passwordEncoder.matches(normalized, row.getCodeHash())
          && backupCodes.markUsedIfUnused(row.getId(), Instant.now()) == 1) {
        return true;
      }
    }
    return false;
  }

  private List<String> issueBackupCodes(String userId) {
    List<String> plain = new ArrayList<>(10);
    for (int i = 0; i < 10; i++) {
      String code = String.format("%08d", RANDOM.nextInt(100_000_000));
      plain.add(code);
      backupCodes.save(
          new UserBackupCodeEntity(Guids.newGuid(), userId, passwordEncoder.encode(code)));
    }
    return plain;
  }

  public record Setup(String secret, String otpAuthUrl) {}
}
