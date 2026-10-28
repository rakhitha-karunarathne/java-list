package db.migration;

import com.listmailer.secrets.SecretsCrypto;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Encrypt legacy plaintext password / totp_secret columns into secrets, then drop them.
 * Key from MAILER_SECRETS_KEY_1 or the same default as application.yml.
 */
public class V41__MigratePlaintextSecrets extends BaseJavaMigration {

  private static final String DEFAULT_KEY = "dev-only-secrets-key-32chars-minimum!";

  @Override
  public void migrate(Context context) throws Exception {
    Connection c = context.getConnection();
    Map<Integer, String> keys = new LinkedHashMap<>();
    String key1 = System.getenv("MAILER_SECRETS_KEY_1");
    if (key1 == null || key1.isBlank()) {
      // Flyway has no Spring YAML binding; same default as application.yml keys[0].key
      key1 = DEFAULT_KEY;
    }
    keys.put(1, key1);
    SecretsCrypto crypto = new SecretsCrypto(keys);
    int ver = crypto.currentVersion();

    migratePasswordTable(c, crypto, ver, "sending_paths");
    migratePasswordTable(c, crypto, ver, "bounce_mailboxes");
    migratePasswordTable(c, crypto, ver, "mts_inboxes");
    migrateTotp(c, crypto, ver);

    try (Statement st = c.createStatement()) {
      st.execute("ALTER TABLE sending_paths DROP COLUMN password");
      st.execute("ALTER TABLE bounce_mailboxes DROP COLUMN password");
      st.execute("ALTER TABLE mts_inboxes DROP COLUMN password");
      st.execute("ALTER TABLE users DROP COLUMN totp_secret");
      st.execute(
          "ALTER TABLE sending_paths MODIFY password_secret_id CHAR(36) NOT NULL");
      st.execute(
          "ALTER TABLE bounce_mailboxes MODIFY password_secret_id CHAR(36) NOT NULL");
      st.execute("ALTER TABLE mts_inboxes MODIFY password_secret_id CHAR(36) NOT NULL");
    }
  }

  private static void migratePasswordTable(
      Connection c, SecretsCrypto crypto, int ver, String table) throws Exception {
    String select =
        "SELECT id, password FROM " + table + " WHERE password_secret_id IS NULL";
    try (Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(select);
        PreparedStatement insert =
            c.prepareStatement(
                "INSERT INTO secrets (id, key_version, ciphertext, created_at) VALUES (?,?,?,CURRENT_TIMESTAMP(3))");
        PreparedStatement update =
            c.prepareStatement(
                "UPDATE " + table + " SET password_secret_id=? WHERE id=?")) {
      while (rs.next()) {
        String rowId = rs.getString(1);
        String password = rs.getString(2);
        if (password == null) {
          password = "";
        }
        String secretId = UUID.randomUUID().toString();
        byte[] cipher = crypto.encrypt(ver, password.getBytes(StandardCharsets.UTF_8));
        insert.setString(1, secretId);
        insert.setInt(2, ver);
        insert.setBytes(3, cipher);
        insert.executeUpdate();
        update.setString(1, secretId);
        update.setString(2, rowId);
        update.executeUpdate();
      }
    }
  }

  private static void migrateTotp(Connection c, SecretsCrypto crypto, int ver) throws Exception {
    String select =
        "SELECT id, totp_secret FROM users WHERE totp_secret IS NOT NULL AND totp_secret_id IS NULL";
    try (Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(select);
        PreparedStatement insert =
            c.prepareStatement(
                "INSERT INTO secrets (id, key_version, ciphertext, created_at) VALUES (?,?,?,CURRENT_TIMESTAMP(3))");
        PreparedStatement update =
            c.prepareStatement("UPDATE users SET totp_secret_id=? WHERE id=?")) {
      while (rs.next()) {
        String rowId = rs.getString(1);
        String totp = rs.getString(2);
        if (totp == null || totp.isBlank()) {
          continue;
        }
        String secretId = UUID.randomUUID().toString();
        byte[] cipher = crypto.encrypt(ver, totp.getBytes(StandardCharsets.UTF_8));
        insert.setString(1, secretId);
        insert.setInt(2, ver);
        insert.setBytes(3, cipher);
        insert.executeUpdate();
        update.setString(1, secretId);
        update.setString(2, rowId);
        update.executeUpdate();
      }
    }
  }
}
