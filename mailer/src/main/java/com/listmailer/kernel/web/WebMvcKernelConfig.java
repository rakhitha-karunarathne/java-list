package com.listmailer.kernel.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcKernelConfig implements WebMvcConfigurer {

  private final HttpFilterChainInterceptor httpFilterChainInterceptor;

  public WebMvcKernelConfig(HttpFilterChainInterceptor httpFilterChainInterceptor) {
    this.httpFilterChainInterceptor = httpFilterChainInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(httpFilterChainInterceptor).addPathPatterns("/api/**", "/t/**", "/dl/**", "/unsubscribe/**", "/approve/**");
  }
}
