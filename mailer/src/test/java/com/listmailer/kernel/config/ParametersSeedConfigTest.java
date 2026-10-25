package com.listmailer.kernel.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.parameters.ParameterRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class ParametersSeedConfigTest {

  @Test
  void seedsPublicBaseUrlOnlyWhenAbsent() throws Exception {
    Map<String, String> store = new ConcurrentHashMap<>();
    ParametersPort port = mapPort(store);
    ParameterRepository repository = mock(ParameterRepository.class);
    when(repository.findById(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());
    var runner =
        new ParametersSeedConfig().seedParameters(port, repository, "https://lists.example.com");
    runner.run(new DefaultApplicationArguments());
    assertEquals("https://lists.example.com", store.get(ParameterKeys.PUBLIC_BASE_URL));

    store.put(ParameterKeys.PUBLIC_BASE_URL, "https://kept.example.com");
    runner.run(new DefaultApplicationArguments());
    assertEquals("https://kept.example.com", store.get(ParameterKeys.PUBLIC_BASE_URL));
  }

  @Test
  void rejectsNonHttpsPublicBaseUrl() {
    ParametersPort port = mapPort(new ConcurrentHashMap<>());
    var runner =
        new ParametersSeedConfig()
            .seedParameters(port, mock(ParameterRepository.class), "http://insecure.example.com");
    assertThrows(
        IllegalStateException.class, () -> runner.run(new DefaultApplicationArguments()));
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
