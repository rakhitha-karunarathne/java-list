package com.listmailer.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

class SiteTitlesTest {

  @Test
  void siteTitleIsFirstDeclaredParameter() {
    assertSame(
        ParameterKeys.SITE_TITLE,
        ParameterDefinitions.all().keySet().iterator().next());
    assertEquals("List Mailer", ParameterDefinitions.require(ParameterKeys.SITE_TITLE).defaultValue());
  }

  @Test
  void resolveFallsBackWhenMissingOrBlank() {
    Map<String, String> store = new ConcurrentHashMap<>();
    ParametersPort port = mapPort(store);
    assertEquals(SiteTitles.DEFAULT, SiteTitles.resolve(port));
    store.put(ParameterKeys.SITE_TITLE, "  ");
    assertEquals(SiteTitles.DEFAULT, SiteTitles.resolve(port));
    store.put(ParameterKeys.SITE_TITLE, " Acme Lists ");
    assertEquals("Acme Lists", SiteTitles.resolve(port));
  }

  private static ParametersPort mapPort(Map<String, String> store) {
    return new ParametersPort() {
      @Override
      public Optional<String> get(String key) {
        return Optional.ofNullable(store.get(key));
      }

      @Override
      public void set(String key, String value) {
        store.put(key, value);
      }
    };
  }
}
