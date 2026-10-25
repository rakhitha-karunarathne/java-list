package com.listmailer.kernel.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Empty-by-default ordered filter chains for kernel port attachments and HTTP endpoints (AD-28).
 * Plugin packs register here later; V1 ships registration only.
 */
@Component
public class FilterChainRegistry {

  private final Map<String, List<PluginFilter<?>>> chains = new ConcurrentHashMap<>();

  public <T> void register(String chainId, PluginFilter<T> filter) {
    chains.computeIfAbsent(chainId, ignored -> new ArrayList<>()).add(filter);
  }

  @SuppressWarnings("unchecked")
  public <T> T invoke(String chainId, T input) {
    List<PluginFilter<?>> filters = chains.getOrDefault(chainId, List.of());
    return invokeAt(filters, 0, input);
  }

  public int size(String chainId) {
    return chains.getOrDefault(chainId, List.of()).size();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> T invokeAt(List<PluginFilter<?>> filters, int index, T input) {
    if (index >= filters.size()) {
      return input;
    }
    PluginFilter filter = filters.get(index);
    return (T) filter.apply(input, next -> invokeAt(filters, index + 1, (T) next));
  }
}
