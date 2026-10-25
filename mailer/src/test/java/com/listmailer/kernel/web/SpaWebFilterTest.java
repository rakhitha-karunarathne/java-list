package com.listmailer.kernel.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpaWebFilterTest {

  @Test
  void forwardsClientRoutes() {
    assertTrue(SpaWebFilter.shouldForward("GET", "/campaigns"));
    assertTrue(SpaWebFilter.shouldForward("GET", "/lists/abc"));
  }

  @Test
  void doesNotForwardApiOrAssets() {
    assertFalse(SpaWebFilter.shouldForward("GET", "/api/ping"));
    assertFalse(SpaWebFilter.shouldForward("GET", "/actuator/health"));
    assertFalse(SpaWebFilter.shouldForward("GET", "/assets/index.js"));
    assertFalse(SpaWebFilter.shouldForward("POST", "/campaigns"));
  }
}
