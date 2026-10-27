package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BounceErrorExtractorTest {

  @Test
  void prefersDiagnosticCode() {
    String blob = "Status: 5.2.2\nDiagnostic-Code: smtp; 552 mailbox full\n";
    assertEquals("552 mailbox full", BounceErrorExtractor.extract(blob, "Undelivered"));
  }

  @Test
  void fallsBackToSubject() {
    assertEquals("DSN fragment only", BounceErrorExtractor.extract("", "DSN fragment only"));
  }

  @Test
  void statusWithUserUnknown() {
    String out =
        BounceErrorExtractor.extract("Status: 5.1.1\nuser unknown at host\n", "bounce");
    assertTrue(out.contains("5.1.1"));
    assertTrue(out.toLowerCase().contains("user unknown"));
  }
}
