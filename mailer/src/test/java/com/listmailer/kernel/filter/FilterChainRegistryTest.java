package com.listmailer.kernel.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FilterChainRegistryTest {

  @Test
  void emptyChainReturnsInput() {
    FilterChainRegistry registry = new FilterChainRegistry();
    assertEquals("x", registry.invoke("http", "x"));
    assertEquals(0, registry.size("http"));
  }

  @Test
  void registeredFiltersRunInOrder() {
    FilterChainRegistry registry = new FilterChainRegistry();
    registry.register("smtp", (String in, PluginFilter.ChainContinuation<String> next) -> next.proceed(in + "a"));
    registry.register("smtp", (String in, PluginFilter.ChainContinuation<String> next) -> next.proceed(in + "b"));
    assertEquals("zab", registry.invoke("smtp", "z"));
  }
}
