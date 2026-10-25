package com.listmailer.kernel.web;

import com.listmailer.kernel.filter.FilterChainRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Runs the HTTP endpoint plugin chain (empty in V1) before controllers (AD-28). */
@Component
public class HttpFilterChainInterceptor implements HandlerInterceptor {

  public static final String CHAIN_HTTP = "http";

  private final FilterChainRegistry registry;

  public HttpFilterChainInterceptor(FilterChainRegistry registry) {
    this.registry = registry;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    registry.invoke(CHAIN_HTTP, request);
    return true;
  }
}
