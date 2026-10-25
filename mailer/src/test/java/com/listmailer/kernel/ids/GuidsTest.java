package com.listmailer.kernel.ids;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GuidsTest {

  @Test
  void newGuidIsUuidV4Shaped() {
    String id = Guids.newGuid();
    assertTrue(id.matches(
        "[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"));
    assertNotEquals(id, Guids.newGuid());
  }
}
