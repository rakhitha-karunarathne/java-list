package com.listmailer.kernel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/** Guards V45 hot-path indexes against accidental deletion. */
class HotPathIndexesMigrationTest {

  @Test
  void v45DefinesBounceAndJoinIndexes() throws Exception {
    String sql =
        new ClassPathResource("db/migration/V45__hot_path_indexes.sql")
            .getContentAsString(StandardCharsets.UTF_8);
    for (String needle :
        List.of(
            "uq_send_queue_verp_rcpt",
            "uq_send_queue_smtp_message_id",
            "idx_send_queue_status_subject",
            "idx_send_queue_member_status",
            "idx_bounce_events_send_queue",
            "idx_bounce_events_member_status",
            "idx_bounce_events_mailbox_status",
            "idx_bounce_events_received",
            "idx_campaigns_status",
            "idx_campaign_lists_list",
            "idx_list_members_member",
            "idx_user_list_grants_list",
            "idx_mts_sender_list_grants_list",
            "idx_mts_authorized_senders_email",
            "idx_attention_open_list",
            "idx_secrets_key_version",
            "SET verp_rcpt = NULL",
            "SET smtp_message_id = NULL")) {
      assertTrue(sql.contains(needle), needle);
    }
  }
}
