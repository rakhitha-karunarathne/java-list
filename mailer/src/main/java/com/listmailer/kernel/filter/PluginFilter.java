package com.listmailer.kernel.filter;

/** One step in a kernel/HTTP plugin filter chain (AD-28). */
@FunctionalInterface
public interface PluginFilter<T> {

  T apply(T input, ChainContinuation<T> next);

  @FunctionalInterface
  interface ChainContinuation<T> {
    T proceed(T input);
  }
}
