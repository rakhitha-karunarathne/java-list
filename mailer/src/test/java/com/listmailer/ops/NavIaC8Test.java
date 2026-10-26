package com.listmailer.ops;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Regression: primary sidebar must not list detail entities (C8 / R4). */
class NavIaC8Test {

  @Test
  @DisplayName("App shell primary buttons are indexes/tools only")
  void sidebarHasNoDetailDestinations() throws Exception {
    Path nav = Path.of("frontend/src/navConfig.js");
    if (!Files.exists(nav)) {
      nav = Path.of("../frontend/src/navConfig.js");
    }
    String src = Files.readString(nav);

    List<String> forbiddenLabels =
        List.of(
            "Campaign detail",
            "List detail",
            "Member detail",
            "User detail",
            "Sending path detail",
            "Bounce mailbox detail",
            "Ops log detail",
            "Inbox detail");
    for (String label : forbiddenLabels) {
      assertFalse(src.contains("'" + label + "'") || src.contains("\"" + label + "\""), label);
    }

    assertTrue(src.contains("Compose"));
    assertTrue(src.contains("Ops Logs"));
    assertTrue(src.contains("Attention"));
    assertTrue(src.contains("Inboxes"));
  }
}
