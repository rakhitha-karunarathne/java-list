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
    Path app = Path.of("frontend/src/App.jsx");
    if (!Files.exists(app)) {
      app = Path.of("../frontend/src/App.jsx");
    }
    String src = Files.readString(app);
    // primary setView buttons in signed-in chrome
    int chromeEnd = src.indexOf("{view === 'home'");
    String chrome = chromeEnd > 0 ? src.substring(0, chromeEnd) : src;

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
      assertFalse(chrome.contains(">" + label + "<"), "sidebar must not contain " + label);
    }

    assertTrue(chrome.contains("Compose") || chrome.contains("setView('compose')"));
    assertTrue(chrome.contains("Ops Logs") || chrome.contains("setView('ops')"));
    assertTrue(chrome.contains("Attention") || chrome.contains("setView('attention')"));
    assertTrue(chrome.contains("Inboxes") || chrome.contains("setView('mts')"));
  }
}
