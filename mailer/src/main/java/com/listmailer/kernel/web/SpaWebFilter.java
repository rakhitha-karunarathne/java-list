package com.listmailer.kernel.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Forwards browser deep links to the React index.html (AD-9). */
@Component
@Order(1)
public class SpaWebFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (shouldForward(request.getMethod(), path)) {
      request.getRequestDispatcher("/index.html").forward(request, response);
      return;
    }
    filterChain.doFilter(request, response);
  }

  static boolean shouldForward(String method, String path) {
    if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
      return false;
    }
    if (path.startsWith("/api")
        || path.startsWith("/actuator")
        || path.startsWith("/t/")
        || path.startsWith("/dl/")
        || path.startsWith("/unsubscribe")
        || path.startsWith("/approve")
        || path.equals("/index.html")) {
      return false;
    }
    // Static assets from the Vite build include a '.' (e.g. /assets/index-….js).
    int last = path.lastIndexOf('/');
    String lastSegment = last >= 0 ? path.substring(last + 1) : path;
    return !lastSegment.contains(".");
  }
}
